package com.example.tfgprueba1.pantallas

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.tfgprueba1.ui.theme.AmarilloGrasas
import com.example.tfgprueba1.ui.theme.AzulOscuroApp
import com.example.tfgprueba1.ui.theme.AzulProte
import com.example.tfgprueba1.ui.theme.BlancoPuro
import com.example.tfgprueba1.ui.theme.NegroPuro
import com.example.tfgprueba1.ui.theme.RosaCarbos
import kotlinx.coroutines.launch

object DistribucionDeNutrientes : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val userDataStore = remember { UserDataStore(context) }
        val coroutineScope = rememberCoroutineScope()

        val savedCaloriasObjetivo by userDataStore.caloriasObjetivoFlow.collectAsState(initial = null)
        val caloriasBase = savedCaloriasObjetivo?.calorias ?: 2000

        var selectedOption by remember { mutableStateOf("Estándar") }
        var customHC by remember { mutableStateOf("") }
        var customProt by remember { mutableStateOf("") }
        var customFat by remember { mutableStateOf("") }

        // Cargar valores guardados al iniciar
        LaunchedEffect(savedCaloriasObjetivo) {
            savedCaloriasObjetivo?.let {
                selectedOption = it.nombreDieta
                customHC = it.customHC
                customProt = it.customProt
                customFat = it.customFat
            }
        }

        val dietaOptions = listOf(
            "Estándar" to listOf(0.50, 0.20, 0.30), // Carbos, Prote, Grasas
            "Equilibrada" to listOf(0.40, 0.30, 0.30),
            "Bajo en grasas" to listOf(0.60, 0.25, 0.15),
            "Alta en proteínas" to listOf(0.30, 0.40, 0.30),
            "Cetogénica" to listOf(0.05, 0.20, 0.75),
        )

        
        val colorHC = RosaCarbos  // Azul
        val colorProt = AzulProte // Rojo
        val colorFat = AmarilloGrasas  // Ámbar/Amarillo

        
        val currentPercs = if (selectedOption == "Personalizada") {
            val hc = (customHC.toDoubleOrNull() ?: 0.0) / 100.0
            val prot = (customProt.toDoubleOrNull() ?: 0.0) / 100.0
            val fat = (customFat.toDoubleOrNull() ?: 0.0) / 100.0
            Triple(hc, prot, fat)
        } else {
            val list = dietaOptions.find { it.first == selectedOption }?.second ?: listOf(0.5, 0.2, 0.3)
            Triple(list[0], list[1], list[2])
        }

        val gramosHC = (caloriasBase * currentPercs.first) / 4
        val gramosProt = (caloriasBase * currentPercs.second) / 4
        val gramosFat = (caloriasBase * currentPercs.third) / 9

        fun guardarConfiguracion() {
            if (selectedOption == "Personalizada") {
                val totalInt = (customHC.toIntOrNull() ?: 0) + (customProt.toIntOrNull() ?: 0) + (customFat.toIntOrNull() ?: 0)
                if (totalInt != 100) {
                    Toast.makeText(context, "Los porcentajes deben sumar exactamente 100%", Toast.LENGTH_SHORT).show()
                    return
                }
            }

            val nuevoObjetivo = CaloriasObjetivo(
                calorias = caloriasBase,
                carbohidratos = gramosHC,
                proteinas = gramosProt,
                grasas = gramosFat,
                nombreDieta = selectedOption,
                customHC = customHC,
                customProt = customProt,
                customFat = customFat
            )

            coroutineScope.launch {
                userDataStore.saveCaloriasObjetivo(nuevoObjetivo)
                Toast.makeText(context, "Distribución guardada con éxito", Toast.LENGTH_SHORT).show()
                navigator.pop()
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Distribución de Nutrientes", color = BlancoPuro, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = BlancoPuro)
                        }
                    },
                    actions = {
                        IconButton(onClick = { guardarConfiguracion() }) {
                            Icon(Icons.Filled.Save, "Guardar", tint = BlancoPuro)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuroApp)
                )
            }
        ) { paddingValues ->
            Column(
                Modifier
                    .fillMaxSize()
                    .background(BlancoPuro)
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(Modifier.selectableGroup()) {
                    dietaOptions.forEach { (titulo, porcentajes) ->
                        val desc = "${(porcentajes[0] * 100).toInt()}% HC | ${(porcentajes[1] * 100).toInt()}% Prot | ${(porcentajes[2] * 100).toInt()}% Gr"
                        DietOptionRow(
                            titulo = titulo,
                            desc = desc,
                            selected = (selectedOption == titulo),
                            onSelect = { selectedOption = titulo }
                        )
                    }

                    DietOptionRow(
                        titulo = "Personalizada",
                        desc = "Define tus propios porcentajes",
                        selected = (selectedOption == "Personalizada"),
                        onSelect = { selectedOption = "Personalizada" }
                    )
                }

                if (selectedOption == "Personalizada") {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, start = 44.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CustomNutrientField(
                            value = customHC,
                            label = "% HC",
                            onValueChange = { if (it.length <= 3) customHC = it },
                            modifier = Modifier.weight(1f)
                        )
                        CustomNutrientField(
                            value = customProt,
                            label = "% Prot",
                            onValueChange = { if (it.length <= 3) customProt = it },
                            modifier = Modifier.weight(1f)
                        )
                        CustomNutrientField(
                            value = customFat,
                            label = "% Gr",
                            onValueChange = { if (it.length <= 3) customFat = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    val total = (customHC.toIntOrNull() ?: 0) + (customProt.toIntOrNull() ?: 0) + (customFat.toIntOrNull() ?: 0)
                    if (customHC.isNotEmpty() || customProt.isNotEmpty() || customFat.isNotEmpty()) {
                        Text(
                            text = "Total: $total% (debe sumar 100%)",
                            color = if (total == 100) Color(0xFF4CAF50) else if (total > 100) Color.Red else Color.Gray,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 44.dp, top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))

                
                Text(
                    text = "Previsualización de la Dieta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NegroPuro,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Gráfica Circular (Pie Chart)
                    Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(150.dp)) {
                            val sum = currentPercs.first + currentPercs.second + currentPercs.third
                            if (sum > 0) {
                                val hcSweep = (currentPercs.first / sum).toFloat() * 360f
                                val protSweep = (currentPercs.second / sum).toFloat() * 360f
                                val fatSweep = (currentPercs.third / sum).toFloat() * 360f

                                drawArc(color = colorHC, startAngle = -90f, sweepAngle = hcSweep, useCenter = true)
                                drawArc(color = colorProt, startAngle = -90f + hcSweep, sweepAngle = protSweep, useCenter = true)
                                drawArc(color = colorFat, startAngle = -90f + hcSweep + protSweep, sweepAngle = fatSweep, useCenter = true)
                            } else {
                                drawArc(color = Color.LightGray, startAngle = 0f, sweepAngle = 360f, useCenter = true)
                            }
                        }
                    }

                    // Información de Gramos
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MacroInfoRow(colorHC, "HC", "${gramosHC.toInt()}g", "${(currentPercs.first * 100).toInt()}%")
                        MacroInfoRow(colorProt, "Prot", "${gramosProt.toInt()}g", "${(currentPercs.second * 100).toInt()}%")
                        MacroInfoRow(colorFat, "Grasa", "${gramosFat.toInt()}g", "${(currentPercs.third * 100).toInt()}%")
                        
                        Text(
                            text = "Total: $caloriasBase kcal",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    @Composable
    private fun MacroInfoRow(color: Color, label: String, grams: String, percentage: String) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(12.dp)) {
                drawCircle(color = color)
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$grams ($percentage)",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
        }
    }

    @Composable
    private fun DietOptionRow(
        titulo: String,
        desc: String,
        selected: Boolean,
        onSelect: () -> Unit
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .selectable(
                    selected = selected,
                    onClick = onSelect,
                    role = Role.RadioButton
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = null)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = titulo, fontWeight = FontWeight.SemiBold, color = NegroPuro, fontSize = 16.sp)
                Text(text = desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }

    @Composable
    private fun CustomNutrientField(
        value: String,
        label: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                    onValueChange(it)
                }
            },
            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
            modifier = modifier,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
}
