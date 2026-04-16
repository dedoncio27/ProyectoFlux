package com.example.tfgprueba1.pantallas

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.tfgprueba1.ui.theme.AmarilloGrasas
import com.example.tfgprueba1.ui.theme.AzulBarraBusqueda
import com.example.tfgprueba1.ui.theme.AzulOscuroApp
import com.example.tfgprueba1.ui.theme.AzulProte
import com.example.tfgprueba1.ui.theme.BlancoPuro
import com.example.tfgprueba1.ui.theme.NegroPuro
import com.example.tfgprueba1.ui.theme.RosaCarbos
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
object AjustesCaloryPage : Screen{

    @SuppressLint("CoroutineCreationDuringComposition")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val userDataStore = remember { UserDataStore(context) }
        val coroutineScope = rememberCoroutineScope()

        // DATOS RECOGIDOS EN PRINCIPAL PAGE
        val savedAltura by userDataStore.alturaFlow.collectAsState("")
        val savedPeso by userDataStore.pesoFlow.collectAsState("")
        val savedEdad by userDataStore.edadFlow.collectAsState("")
        val savedSexo by userDataStore.sexoFlow.collectAsState("")
        val savedActividad by userDataStore.actividadFlow.collectAsState("")
        val savedObjetivo by userDataStore.objetivoFlow.collectAsState("Mantener peso")
        val savedCaloriasObjetivo by userDataStore.caloriasObjetivoFlow.collectAsState(initial = null)
        val savedAutoCalcular by userDataStore.autoCalcularFlow.collectAsState(initial = true)

        
        var autoCalcular by remember { mutableStateOf(true) }
        var caloriasManual by remember { mutableStateOf("") }
        var mostrarCampoAdd by remember { mutableStateOf(false) }
        var nuevoMomento by remember { mutableStateOf("") }

        // Cargar ajustes previos
        LaunchedEffect(Unit) {
            val comidasGuardadas = userDataStore.getComidasCantidad()
            momentoComidas.momentoComidasLog.clear()
            momentoComidas.momentoComidasLog.addAll(comidasGuardadas)
        }

        LaunchedEffect(savedCaloriasObjetivo, savedAutoCalcular) {
            savedCaloriasObjetivo?.let {
                caloriasManual = it.calorias.toString()
            }
            autoCalcular = savedAutoCalcular
        }

        
        fun calcularCaloriasAutomaticas(): Int {
            val pesoVal = savedPeso.toDoubleOrNull() ?: 0.0
            val alturaVal = savedAltura.toDoubleOrNull() ?: 0.0
            val edadVal = savedEdad.toIntOrNull() ?: 0
            if (pesoVal == 0.0 || alturaVal == 0.0 || edadVal == 0) return 2000

            val tmb = if (savedSexo == "Hombre") {
                88.36 + (13.4 * pesoVal) + (4.8 * alturaVal) - (5.7 * edadVal)
            } else {
                447.6 + (9.2 * pesoVal) + (3.1 * alturaVal) - (4.3 * edadVal)
            }

            val factorActividad = when (savedActividad) {
                "Casi nada de actividad" -> 1.2
                "Poca actividad" -> 1.375
                "Actividad moderada" -> 1.55
                "Actividad alta" -> 1.725
                else -> 1.9
            }

            var total = tmb * factorActividad

            total += when (savedObjetivo) {
                "Bajar de peso rápido" -> -500.0
                "Bajar de peso lentamente" -> -250.0
                "Subir de peso lentamente" -> 250.0
                "Subir de peso rápido" -> 500.0
                else -> 0.0
            }

            return total.toInt()
        }

        val caloriasFinales=calcularCaloriasAutomaticas()


        fun guardarConfiguracion() {
            val cals = if (autoCalcular) caloriasFinales else caloriasManual.toIntOrNull() ?: 2000
            val actual = savedCaloriasObjetivo ?: CaloriasObjetivo()
            
            val nuevoObjetivo = actual.copy(calorias = cals)
            
            coroutineScope.launch {
                userDataStore.saveCaloriasObjetivo(nuevoObjetivo)
                userDataStore.saveAutoCalcular(autoCalcular)
                Toast.makeText(context, "Ajustes guardados con éxito", Toast.LENGTH_SHORT).show()
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {Text("Configuración de Dieta", color = BlancoPuro, fontWeight = FontWeight.Bold)},
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = BlancoPuro)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            guardarConfiguracion()
                            navigator.pop()
                        }) {
                            Icon(Icons.Filled.Save, "Guardar", tint = BlancoPuro)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuroApp)
                )
            }
        ){ paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BlancoPuro)
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                Text("Cálculo de Calorías", fontWeight = FontWeight.Bold, color = NegroPuro, fontSize = 18.sp)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cálculo automático", color = NegroPuro)
                        Text("Basado en tus datos físicos y objetivo", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = autoCalcular,
                        onCheckedChange = { isAuto ->
                            autoCalcular = isAuto

                            // ¡ESTA ES LA CLAVE! Al mover el switch, calculamos qué calorías tocan
                            // y guardamos el objeto completo inmediatamente.
                            val cals = if (isAuto) caloriasFinales else caloriasManual.toIntOrNull() ?: 2000
                            val actual = savedCaloriasObjetivo ?: CaloriasObjetivo()
                            val nuevoObjetivo = actual.copy(calorias = cals)

                            coroutineScope.launch {
                                // Guardamos las calorías actualizadas
                                userDataStore.saveCaloriasObjetivo(nuevoObjetivo)
                                // Guardamos la posición del switch
                                userDataStore.saveAutoCalcular(isAuto)
                            }
                        }
                    )
                }

                if (autoCalcular) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "$caloriasFinales", color = AzulOscuroApp, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                        Text(text = "kcal", color = AzulOscuroApp, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "$caloriasManual", color = AzulOscuroApp, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                        Text(text = "kcal", color = AzulOscuroApp, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = caloriasManual,
                            onValueChange = { if(it.all { c -> c.isDigit() }) caloriasManual = it },
                            label = { Text("Introduce tus calorías diarias") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Button(
                            onClick = { guardarConfiguracion() },
                            colors = ButtonDefaults.buttonColors(containerColor = AzulOscuroApp.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(10.dp)
                        ){
                            Text(text = "Guardar", color = BlancoPuro)
                        }
                    }
                }

                HorizontalDivider()
                
                Text("Distribución de Macronutrientes", fontWeight = FontWeight.Bold, color = NegroPuro, fontSize = 18.sp)

                // --- BOX DE DISTRIBUCIÓN CLICABLE ---
                val currentTarget = savedCaloriasObjetivo ?: CaloriasObjetivo()
                val totalCalDouble = currentTarget.calorias.toDouble().coerceAtLeast(1.0)
                val pProt = (currentTarget.proteinas * 4) / totalCalDouble
                val pCarb = (currentTarget.carbohidratos * 4) / totalCalDouble
                val pFat = (currentTarget.grasas * 9) / totalCalDouble

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { navigator.push(DistribucionDeNutrientes) }
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        
                        Box(modifier = Modifier.size(50.dp)) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                if (pProt + pCarb + pFat > 0) {
                                    val sweepProt = (pProt * 360f).toFloat()
                                    val sweepCarb = (pCarb * 360f).toFloat()
                                    val sweepFat = (pFat * 360f).toFloat()

                                    drawArc(AzulProte, -90f, sweepProt, true)
                                    drawArc(RosaCarbos , -90f + sweepProt, sweepCarb, true)
                                    drawArc(AmarilloGrasas, -90f + sweepProt + sweepCarb, sweepFat, true)
                                } else {
                                    drawArc(Color.LightGray, 0f, 360f, true)
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Distribución Actual", fontWeight = FontWeight.Bold, color = NegroPuro)
                            Text(
                                text = "${(pCarb*100).toInt()}% HC | ${(pProt*100).toInt()}% Prot | ${(pFat*100).toInt()}% Gr",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                    }
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AzulBarraBusqueda, RoundedCornerShape(15.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Momentos de Comida",
                        color = BlancoPuro,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    // Cabecera interactiva para añadir
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mostrarCampoAdd = !mostrarCampoAdd }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = BlancoPuro)
                        Text(
                            text = "Añadir momento",
                            color = BlancoPuro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    if (mostrarCampoAdd) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = nuevoMomento,
                                onValueChange = { nuevoMomento = it },
                                label = { Text("Nombre", color = BlancoPuro.copy(alpha = 0.7f)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = BlancoPuro,
                                    unfocusedTextColor = BlancoPuro,
                                    focusedBorderColor = BlancoPuro,
                                    unfocusedBorderColor = BlancoPuro.copy(alpha = 0.5f),
                                    cursorColor = BlancoPuro
                                )
                            )
                            Button(
                                onClick = {
                                    if (nuevoMomento.isNotBlank()) {
                                        momentoComidas.momentoComidasLog.add(nuevoMomento)
                                        coroutineScope.launch {
                                            userDataStore.saveComidasCantidad(momentoComidas.momentoComidasLog.toList())
                                        }
                                        nuevoMomento = ""
                                        mostrarCampoAdd = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BlancoPuro),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("OK", color = AzulOscuroApp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        momentoComidas.momentoComidasLog.forEach { nombreComida ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BlancoPuro, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = nombreComida, color = NegroPuro, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                IconButton(onClick = {
                                    momentoComidas.momentoComidasLog.remove(nombreComida)
                                    coroutineScope.launch {
                                        userDataStore.saveComidasCantidad(momentoComidas.momentoComidasLog.toList())
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                }
                
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
