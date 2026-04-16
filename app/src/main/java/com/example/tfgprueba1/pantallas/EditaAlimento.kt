package com.example.tfgprueba1.pantallas

import android.annotation.SuppressLint
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
import kotlinx.coroutines.launch

class EditaAlimentoScreen(val alimento: DatosAlimento) : Screen {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val dataStore = remember { UserDataStore(context) }

        var cantidadEditada by remember { mutableStateOf(alimento.cantidadAlimento.toInt().toString()) }
        val entradaUsuario = cantidadEditada.toDoubleOrNull() ?: 0.0

        val cantidadOriginal = if (alimento.cantidadMedida <= 0.0) 1.0 else alimento.cantidadMedida

        val carbsCalculados = (alimento.carbohidratos / cantidadOriginal) * entradaUsuario
        val proteCalculadas = (alimento.proteinas / cantidadOriginal) * entradaUsuario
        val grasasCalculadas = (alimento.grasas / cantidadOriginal) * entradaUsuario
        val kcalCalculadas = (alimento.calorias / cantidadOriginal) * entradaUsuario

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(alimento.nombre, color = BlancoPuro, fontWeight = FontWeight.Bold) },
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(alimento.nombre, color = NegroPuro, style = MaterialTheme.typography.titleLarge)
                        Text(alimento.marca, color = NegroPuro, style = MaterialTheme.typography.titleMedium)
                    }
                    MacroPieChart(carbsCalculados, proteCalculadas, grasasCalculadas, kcalCalculadas)
                }

                // Visualización de Macros Actualizados
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    MacroDato("Carbos", carbsCalculados, RosaCarbos)
                    MacroDato("Proteínas", proteCalculadas, AzulProte)
                    MacroDato("Grasas", grasasCalculadas, AmarilloGrasas)
                }

                Spacer(modifier = Modifier.height(10.dp))

                CustomTextField(
                    value = cantidadEditada,
                    onValueChange = { cantidadEditada = it },
                    label = "Cantidad en " + alimento.medida,
                    keyboardType = KeyboardType.Number
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    
                    Button(
                        onClick = {
                            listaComidasCaloryPage.listaAlimentosLog.remove(alimento)
                            coroutineScope.launch {
                                dataStore.saveListaAlimentos(listaComidasCaloryPage.listaAlimentosLog.toList())
                            }
                            navigator.popUntilRoot()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Eliminar")
                    }

                    Button(
                        onClick = {
                            val index = listaComidasCaloryPage.listaAlimentosLog.indexOfFirst { it.id == alimento.id }

                            if (index != -1) {
                                val alimentoActualizado = alimento.copy(
                                    cantidadAlimento = entradaUsuario,
                                )
                                
                                listaComidasCaloryPage.listaAlimentosLog[index] = alimentoActualizado

                                
                                coroutineScope.launch {
                                    dataStore.saveListaAlimentos(listaComidasCaloryPage.listaAlimentosLog.toList())
                                }
                                navigator.popUntilRoot()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulOscuroApp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }

    @Composable
    fun MacroDato(label: String, value: Double, color: Color) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(String.format("%.1f", value), fontWeight = FontWeight.Bold, color = color, fontSize = 18.sp)
            Text(label, fontSize = 12.sp, color = GrisSuave)
        }
    }

    @Composable
    fun MacroPieChart(carbs: Double, proteins: Double, fats: Double, kcal: Double) {
        val total = (carbs + proteins + fats).toFloat()
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                if (total <= 0f) {
                    drawCircle(color = Color.LightGray, style = Stroke(width = 8f))
                } else {
                    val sweepCarbs = (carbs.toFloat() / total) * 360f
                    val sweepProteins = (proteins.toFloat() / total) * 360f
                    val sweepFats = (fats.toFloat() / total) * 360f
                    drawArc(RosaCarbos, -90f, sweepCarbs, false, style = Stroke(30f, cap = StrokeCap.Square))
                    drawArc(AzulProte, -90f + sweepCarbs, sweepProteins, false, style = Stroke(30f, cap = StrokeCap.Square))
                    drawArc(AmarilloGrasas, -90f + sweepCarbs + sweepProteins, sweepFats, false, style = Stroke(30f, cap = StrokeCap.Square))
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(kcal.toInt().toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NegroPuro)
                Text("Kcal", fontSize = 12.sp, color = GrisSuave)
            }
        }
    }

    @Composable
    fun CustomTextField(value: String, onValueChange: (String) -> Unit, label: String, keyboardType: KeyboardType) {
        OutlinedTextField(
            value = value, onValueChange = onValueChange, label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AzulOscuroApp, unfocusedBorderColor = GrisSuave),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done)
        )
    }
}
