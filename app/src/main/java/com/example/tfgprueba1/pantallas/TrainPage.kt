package com.example.tfgprueba1.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.example.tfgprueba1.ui.theme.AzulCalorias
import com.example.tfgprueba1.ui.theme.GrisFuerte
import com.example.tfgprueba1.ui.theme.GrisSuave
import com.example.tfgprueba1.ui.theme.NegroPuro

object TrainingPageScreen: Tab {
    override val options: TabOptions
        @Composable
        get(){
            val iconoEntrenamiento = rememberVectorPainter(Icons.Default.FitnessCenter)
            return remember() {
                TabOptions(
                    index = 0u,
                    title = "Entrenamiento",
                    icon = iconoEntrenamiento
                )
            }
        }

    @Composable
    override fun Content() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
        ) {

            Spacer(modifier = Modifier.weight(0.15f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0F0F0))
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text("Proximamente...", color = NegroPuro)
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    @Composable
    fun AlimentoLogItem(alimento: DatosAlimento, onClick: () -> Unit) {
        val base = if (alimento.cantidadMedida <= 0.0) 1.0 else alimento.cantidadMedida
        val kcal = ((alimento.calorias * alimento.cantidadAlimento) / base).toInt().toString()
        val carbs = ((alimento.carbohidratos * alimento.cantidadAlimento) / base).toInt().toString()
        val prote = ((alimento.proteinas * alimento.cantidadAlimento) / base).toInt().toString()
        val grasas = ((alimento.grasas * alimento.cantidadAlimento) / base).toInt().toString()



        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
            .height(50.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(alimento.nombre, color = NegroPuro, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("${alimento.cantidadAlimento.toInt()} ${alimento.medida} • ${alimento.marca}", color = GrisFuerte, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(horizontal = 10.dp)) {
                Text("$kcal kcal", color = AzulCalorias, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row {
                    MacroMiniLog("C", carbs, Color(0xFFE57373))
                    Spacer(Modifier.width(4.dp))
                    MacroMiniLog("P", prote, Color(0xFF64B5F6))
                    Spacer(Modifier.width(4.dp))
                    MacroMiniLog("G", grasas, Color(0xFFFFB74D))
                }
            }
        }
    }

    @Composable
    fun MacroMiniLog(label: String, value: String, color: Color) {
        Row {
            Text("$label:", color = GrisSuave, fontSize = 10.sp)
            Text(value, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }

}