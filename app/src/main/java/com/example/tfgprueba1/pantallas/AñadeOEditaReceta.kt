package com.example.tfgprueba1.pantallas

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.tfgprueba1.ui.theme.AzulCalorias
import com.example.tfgprueba1.ui.theme.AzulOscuroApp
import com.example.tfgprueba1.ui.theme.AzulProte
import com.example.tfgprueba1.ui.theme.BlancoPuro
import com.example.tfgprueba1.ui.theme.GrisFuerte
import com.example.tfgprueba1.ui.theme.GrisSuave
import com.example.tfgprueba1.ui.theme.NegroPuro
import com.example.tfgprueba1.ui.theme.RosaCarbos
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class AñadeOEditaRecetaScreen(val receta: DatosRecetas) : Screen {
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
        var cargandoAlimentos by remember { mutableStateOf(false) }


        var listaAlimentosMostrados = remember { mutableStateListOf<Alimento>() }


        DisposableEffect(receta.listaIngredientes, currentUser) {
            val emailUsuario = currentUser?.email


            if (emailUsuario == null || receta.listaIngredientes.isEmpty()) {
                onDispose { }
            } else {

                val idsLimpios = receta.listaIngredientes.map { it.trim() }

                val registration = db.collection("MisAlimentos")
                    .whereEqualTo("email", emailUsuario)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.e("FirestoreError", "Error: ${e.message}")
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {

                            val nuevosAlimentos = mutableListOf<Alimento>()

                            for (doc in snapshot.documents) {

                                if (idsLimpios.contains(doc.id.trim())) {
                                    val alimento = Alimento(
                                        id             = doc.id,
                                        email          = doc.getString("email") ?: "",
                                        NombreAlimento = doc.getString("NombreAlimento") ?: "Sin nombre",
                                        Cantidad       = doc.getString("Cantidad") ?: "100",
                                        Calorias       = doc.getString("Calorias") ?: "0",
                                        Carbohidratos  = doc.getString("Carbohidratos") ?: "0",
                                        Proteinas      = doc.getString("Proteinas") ?: "0",
                                        Grasas         = doc.getString("Grasas") ?: "0",
                                        Marca          = doc.getString("Marca") ?: "",
                                        Medida         = doc.getString("Medida") ?: "g",
                                    )
                                    nuevosAlimentos.add(alimento)
                                }
                            }
                            listaAlimentosMostrados.clear()
                            listaAlimentosMostrados.addAll(nuevosAlimentos)
                        }
                    }

                onDispose {
                    registration.remove()
                }
            }
        }


        val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
        var cantidadAlimento by remember {
            mutableStateOf(
                receta.cantidadMedida.toInt().toString()
            )
        }
        val entradaUsuario = cantidadAlimento.toDoubleOrNull() ?: 0.0


        val carbsCalculados = (receta.carbohidratos * entradaUsuario) / receta.cantidadMedida
        val proteCalculadas = (receta.proteinas * entradaUsuario) / receta.cantidadMedida
        val grasasCalculadas = (receta.grasas * entradaUsuario) / receta.cantidadMedida
        val kcalCalculadas = (receta.calorias * entradaUsuario) / receta.cantidadMedida

        Scaffold(

            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            receta.nombre,
                            color = BlancoPuro,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = BlancoPuro)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuroApp)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
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
                    ) {
                        Text(
                            receta.nombre,
                            color = NegroPuro,
                            style = MaterialTheme.typography.titleLarge
                        )

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
                ) {
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
                        label = "Cantidad " + receta.medida,
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
                            navigator.push(EditarRecetaScreen(receta))
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        Text("Editar")
                    }
                    Button(
                        onClick = {
                            val newAlimentoLista = DatosAlimento(
                                id = receta.id,
                                nombre = receta.nombre,
                                cantidadMedida = receta.cantidadMedida,
                                cantidadAlimento = entradaUsuario,
                                calorias = receta.calorias,
                                carbohidratos = receta.carbohidratos,
                                proteinas = receta.proteinas,
                                grasas = receta.grasas,
                                fecha = fechaAlimentos.fechaSeleccionada.toString(),
                                momentoComida = comida.momentoComida,
                                medida = receta.medida
                            )

                            // 1. Añadimos a la lista visual reactiva
                            listaComidasCaloryPage.listaAlimentosLog.add(newAlimentoLista)

                            // 2. Guardamos la lista completa en el DataStore (JSON)
                            coroutineScope.launch {
                                dataStore.saveListaAlimentos(listaComidasCaloryPage.listaAlimentosLog.toList())
                            }
                            Toast.makeText(context, "Receta añadida correctamente", Toast.LENGTH_SHORT).show()
                            navigator.popUntilRoot()


                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulOscuroApp)
                    ) {
                        Text("Añadir")
                    }
                    Button(
                        onClick = {
                            val userEmail = currentUser?.email
                            val docId = receta.id

                            if (userEmail != null && docId.isNotEmpty()) {
                                db.collection("Recetas")
                                    .document(docId)
                                    .delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Receta eliminada correctamente", Toast.LENGTH_SHORT).show()
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


                HorizontalDivider()


                Column(modifier = Modifier.fillMaxWidth()) {
                    // Encabezado Azul
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(AzulOscuroApp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Alimentos en receta",
                            color = BlancoPuro,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Dibujamos los alimentos filtrados
                    if (listaAlimentosMostrados.isEmpty()) {
                        Text(
                            "No hay ingredientes seleccionados",
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = GrisSuave
                        )
                    } else {
                        listaAlimentosMostrados.forEach { alimento ->
                            val cantidadPersonalizada = receta.CantidadesReceta[alimento.id] ?: alimento.Cantidad

                            val cantOriginal = alimento.Cantidad.toDoubleOrNull() ?: 1.0
                            val cantNueva = cantidadPersonalizada.toDoubleOrNull() ?: 0.0
                            val prop = if (cantOriginal > 0) cantNueva / cantOriginal else 0.0

                            // 2. Calculamos los macros para esa cantidad específica
                            val kcal = ((alimento.Calorias.toDoubleOrNull() ?: 0.0) * prop).toString()
                            val carbs = ((alimento.Carbohidratos.toDoubleOrNull() ?: 0.0) * prop).toString()
                            val prote = ((alimento.Proteinas.toDoubleOrNull() ?: 0.0) * prop).toString()
                            val grasas = ((alimento.Grasas.toDoubleOrNull() ?: 0.0) * prop).toString()

                            // 3. Creamos un "clon" del alimento con los datos actualizados para pasarlo al UI
                            val alimentoAdaptado = alimento.copy(
                                Cantidad = cantidadPersonalizada,
                                Calorias = kcal,
                                Carbohidratos = carbs,
                                Proteinas = prote,
                                Grasas = grasas
                            )

                            AlimentoItem(alimento = alimentoAdaptado, onClick = { /* Tu acción */ })
                            HorizontalDivider(color = GrisSuave.copy(alpha = 0.3f))
                        }
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
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
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

    @Composable
    fun AlimentoItem(alimento: Alimento, onClick: () -> Unit) {
        fun safeToInt(valor: String): String =
            valor.replace(",", ".").toDoubleOrNull()?.toInt()?.toString() ?: "0"

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(alimento.NombreAlimento, color = NegroPuro, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("${alimento.Cantidad} ${alimento.Medida}", color = GrisFuerte, fontSize = 13.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${safeToInt(alimento.Calorias)} kcal", color = AzulCalorias, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Row {
                    MacroMiniText("C", safeToInt(alimento.Carbohidratos), RosaCarbos)
                    Spacer(Modifier.width(6.dp))
                    MacroMiniText("P", safeToInt(alimento.Proteinas), AzulProte)
                    Spacer(Modifier.width(6.dp))
                    MacroMiniText("G", safeToInt(alimento.Grasas), AmarilloGrasas)
                }
            }
        }
    }

    @Composable
    fun MacroMiniText(label: String, value: String, color: Color) {
        Row {
            Text("$label: ", color = GrisSuave, fontSize = 11.sp)
            Text(value, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

