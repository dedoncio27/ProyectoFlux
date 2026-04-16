package com.example.tfgprueba1.pantallas

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.tfgprueba1.ui.theme.AmarilloGrasas
import com.example.tfgprueba1.ui.theme.AzulOscuroApp
import com.example.tfgprueba1.ui.theme.AzulProte
import com.example.tfgprueba1.ui.theme.BlancoPuro
import com.example.tfgprueba1.ui.theme.GrisSuave
import com.example.tfgprueba1.ui.theme.NegroPuro
import com.example.tfgprueba1.ui.theme.RosaCarbos
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class AñadeOEditaScreen(val alimento: DatosAlimento, val comprueba: Boolean) : Screen{
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val dataStore = remember { UserDataStore(context) }
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser


        val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)}
        var cantidadAlimento by remember{ mutableStateOf(alimento.cantidadMedida.toInt().toString()) }
        val entradaUsuario = cantidadAlimento.toDoubleOrNull() ?: 0.0


        val carbsCalculados = (alimento.carbohidratos * entradaUsuario)/alimento.cantidadMedida
        val proteCalculadas = (alimento.proteinas * entradaUsuario)/alimento.cantidadMedida
        val grasasCalculadas = (alimento.grasas * entradaUsuario)/alimento.cantidadMedida
        val kcalCalculadas = (alimento.calorias * entradaUsuario)/alimento.cantidadMedida

        Scaffold(

            topBar = {
                CenterAlignedTopAppBar(
                    title = {Text(alimento.nombre, color = BlancoPuro, fontWeight = FontWeight.Bold)},
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = BlancoPuro)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuroApp)
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier
                .fillMaxSize()
                .background(BlancoPuro)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)

            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ){
                        Text(alimento.nombre,  color = NegroPuro, style = MaterialTheme.typography.titleLarge)
                        Text(alimento.marca,  color = NegroPuro, style = MaterialTheme.typography.titleMedium)
                    }
                    MacroPieChart(
                        carbs = carbsCalculados,
                        proteins = proteCalculadas,
                        fats = grasasCalculadas,
                        kcal = kcalCalculadas
                    )

                }
                Row(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Column(

                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = carbsCalculados.toInt().toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = RosaCarbos
                        )
                        Text(
                            text = "Carbohidratos",
                            fontSize = 12.sp,
                            color = GrisSuave
                        )
                    }
                    Column(

                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = proteCalculadas.toInt().toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzulProte
                        )
                        Text(
                            text = "Proteinas",
                            fontSize = 12.sp,
                            color = GrisSuave
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = grasasCalculadas.toInt().toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmarilloGrasas
                        )
                        Text(
                            text = "Grasas",
                            fontSize = 12.sp,
                            color = GrisSuave
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    CustomTextField(
                        value = cantidadAlimento,
                        onValueChange = { cantidadAlimento = it },
                        label = "Cantidad " + alimento.medida,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = {
                            val newAlimentoLista =DatosAlimento(
                                id = alimento.id,
                                nombre = alimento.nombre,
                                cantidadMedida= alimento.cantidadMedida,
                                calorias = alimento.calorias,
                                carbohidratos = alimento.carbohidratos,
                                proteinas = alimento.proteinas,
                                grasas = alimento.grasas,
                                marca = alimento.marca,
                                medida = alimento.medida
                            )
                            navigator.push(ActualizarAlimentoScreen(newAlimentoLista))
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        Text("Editar")
                    }
                    Button(
                        onClick = {
                            val newAlimentoLista =DatosAlimento(
                                id = alimento.id,
                                nombre = alimento.nombre,
                                cantidadMedida= alimento.cantidadMedida,
                                cantidadAlimento = entradaUsuario,
                                calorias = alimento.calorias,
                                carbohidratos = alimento.carbohidratos,
                                proteinas = alimento.proteinas,
                                grasas = alimento.grasas,
                                fecha = fechaAlimentos.fechaSeleccionada.toString(),
                                momentoComida = comida.momentoComida,
                                marca = alimento.marca,
                                medida = alimento.medida
                            )
                            
                            // 1. Añadimos a la lista visual reactiva
                            listaComidasCaloryPage.listaAlimentosLog.add(newAlimentoLista)
                            
                            // 2. Guardamos la lista completa en el DataStore (JSON)
                            coroutineScope.launch {
                                dataStore.saveListaAlimentos(listaComidasCaloryPage.listaAlimentosLog.toList())
                            }
                            if (comprueba==true){
                                val docRef = db.collection("MisAlimentos").document()
                                val idUnico = docRef.id
                                var nombre = alimento.nombre
                                var descripcion = ""
                                var calorias = alimento.calorias.toInt().toString()
                                var proteinas = alimento.proteinas.toInt().toString()
                                var carbohidratos = alimento.carbohidratos.toInt().toString()
                                var grasas = alimento.grasas.toInt().toString()
                                var cantidad = alimento.cantidadMedida.toInt().toString()
                                var marca = alimento.marca
                                var medida = alimento.medida

                                val nuevoAlimento = hashMapOf(
                                    "id" to idUnico,
                                    "email" to auth.currentUser?.email,
                                    "NombreAlimento" to nombre,
                                    "Descripcion" to descripcion,
                                    "Cantidad" to cantidad,
                                    "Marca" to marca,
                                    "Medida" to medida,
                                    "Calorias" to calorias,
                                    "Proteinas" to proteinas,
                                    "Carbohidratos" to carbohidratos,
                                    "Grasas" to grasas
                                )
                                db.collection("MisAlimentos").add(nuevoAlimento)
                                    .addOnSuccessListener {
                                        coroutineScope.launch {
                                            Toast.makeText(context, "Alimento añadido a Mis alimentos", Toast.LENGTH_SHORT).show()
                                            navigator.popUntilRoot()
                                        }
                                    }

                            }else{
                                navigator.popUntilRoot()
                            }

                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulOscuroApp)
                    ) {
                        Text("Añadir")
                    }
                    Button(
                        onClick = {
                            val userEmail = currentUser?.email
                            val docId = alimento.id

                            if (userEmail != null && docId.isNotEmpty()) {
                                db.collection("MisAlimentos")
                                    .document(docId)
                                    .delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Alimento eliminado correctamente", Toast.LENGTH_SHORT).show()
                                        // Volvemos a la pantalla anterior de forma segura
                                        navigator.pop()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("FirestoreError", "Fallo al borrar: ${e.message}")
                                    }
                            } else {
                                Log.e("FirestoreError", "ID vacío o email nulo")
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Eliminar")
                    }
                }

            }
        }
    }

    @Composable
    fun MacroPieChart(carbs: Double, proteins: Double, fats: Double, kcal: Double) {
        val total = (carbs + proteins + fats).toFloat()

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)) {
                    if (total <= 0f) {
                        drawCircle(color = Color.LightGray, style = Stroke(width = 8f))
                    } else {
                        val sweepCarbs = ((carbs.toFloat() / total) * 360f)
                        val sweepProteins = ((proteins.toFloat() / total) * 360f)
                        val sweepFats = ((fats.toFloat() / total) * 360f)

                        drawArc(
                            color = RosaCarbos,
                            startAngle = -90f,
                            sweepAngle = sweepCarbs,
                            useCenter = false,
                            style = Stroke(width = 30f, cap = StrokeCap.Square)
                        )
                        drawArc(
                            color = AzulProte,
                            startAngle = -90f + sweepCarbs,
                            sweepAngle = sweepProteins,
                            useCenter = false,
                            style = Stroke(width = 30f, cap = StrokeCap.Square)
                        )
                        drawArc(
                            color = AmarilloGrasas,
                            startAngle = -90f + sweepCarbs + sweepProteins,
                            sweepAngle = sweepFats,
                            useCenter = false,
                            style = Stroke(width = 30f, cap = StrokeCap.Square)
                        )
                    }
                }

                // SOLUCIÓN: El texto se pone AQUÍ (dentro del Box, fuera del Canvas)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = kcal.toInt().toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NegroPuro
                    )
                    Text(
                        text = "Kcal",
                        fontSize = 12.sp,
                        color = GrisSuave
                    )
                }
            }
        }
    }
    @Composable
    fun CustomTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        keyboardType: KeyboardType = KeyboardType.Text,
        modifier: Modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = modifier,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = NegroPuro,
                unfocusedTextColor = NegroPuro,
                focusedBorderColor = AzulOscuroApp,
                unfocusedBorderColor = GrisSuave,
                focusedLabelColor = AzulOscuroApp,
                unfocusedLabelColor = GrisSuave
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            )
        )
    }


}
