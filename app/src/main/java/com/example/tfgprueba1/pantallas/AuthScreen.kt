package com.example.tfgprueba1.pantallas

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.tfgprueba1.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await


class GoogleSignInHelper(private val context: Context, private val auth: FirebaseAuth) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun signInWithGoogle(): Result<FirebaseUser> {
        return try {
            val webClientId = context.getString(R.string.default_web_client_id)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            val credentialResult = credentialManager.getCredential(context, request)
            val credential = credentialResult.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                Result.success(authResult.user!!)
            } else {
                Result.failure(Exception("Error en credenciales"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


fun guardarUsuarioEnFirestore(user: FirebaseUser) {
    val db = FirebaseFirestore.getInstance()

    val datosUsuario = hashMapOf(
        "uid"       to user.uid,
        "email"     to (user.email ?: ""),
        "name"      to (user.displayName ?: ""),
        "createdAt" to Timestamp.now()
    )


    db.collection("IniciosDeSesion")
        .document(user.uid)
        .set(datosUsuario, SetOptions.merge())
        .addOnFailureListener { e ->
            // Silencioso en producción; útil para depuración
            e.printStackTrace()
        }
}


// PANTALLA DE LOGIN

class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context   = LocalContext.current
        val auth      = remember { FirebaseAuth.getInstance() }

        LaunchedEffect(Unit) {
            if (auth.currentUser != null) {
                navigator.replace(BottomBarScreen())
            }
        }

        var email    by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Iniciar Sesión", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                isLoading = false
                                auth.currentUser?.let { guardarUsuarioEnFirestore(it) }


                                navigator.replaceAll(BottomBarScreen())
                            } else {
                                Toast.makeText(
                                    context,
                                    "Error: ${task.exception?.message ?: "Credenciales incorrectas"}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) { Text("Entrar") }

            Text(
                "¿No tienes cuenta? Regístrate",
                modifier = Modifier.padding(top = 16.dp).clickable { navigator.push(RegisterScreen()) },
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


// PANTALLA DE REGISTRO
class RegisterScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context   = LocalContext.current
        val auth      = remember { FirebaseAuth.getInstance() }

        var email           by remember { mutableStateOf("") }
        var password        by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var isLoading       by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Crear Cuenta", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword, onValueChange = { confirmPassword = it },
                label = { Text("Confirmar contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                // Muestra borde rojo si las contraseñas no coinciden
                isError = confirmPassword.isNotEmpty() && password != confirmPassword
            )
            Spacer(Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    when {
                        email.isBlank() || password.isBlank() ->
                            Toast.makeText(context, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                        password != confirmPassword ->
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                        password.length < 6 ->
                            Toast.makeText(context, "Contraseña mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                        else -> {
                            isLoading = true

                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        isLoading = false
                                        auth.currentUser?.let { guardarUsuarioEnFirestore(it) }

                                        // NAVEGAMOS A LA APP PRINCIPAL
                                        navigator.replaceAll(BottomBarScreen())
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Error: ${task.exception?.message ?: "No se pudo crear la cuenta"}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) { Text("Registrarse") }

            Text(
                "¿Ya tienes cuenta? Inicia sesión",
                modifier = Modifier.padding(top = 16.dp).clickable { navigator.pop() },
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator   = LocalNavigator.currentOrThrow
        val auth        = remember { FirebaseAuth.getInstance() }
        val db          = remember { FirebaseFirestore.getInstance() }
        val currentUser = auth.currentUser
        var userName by remember { mutableStateOf(currentUser?.displayName ?: "") }

        LaunchedEffect(Unit) {
            currentUser?.uid?.let { uid ->
                db.collection("IniciosDeSesion").document(uid).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) userName = doc.getString("name") ?: userName
                    }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("¡Bienvenido!", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Text("Email: ${currentUser?.email}", fontSize = 16.sp)
        }
    }
}