package com.example.tfgprueba1.pantallas

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.example.tfgprueba1.ui.theme.AmarilloGrasas
import com.example.tfgprueba1.ui.theme.AzulBarraBusqueda
import com.example.tfgprueba1.ui.theme.AzulCalorias
import com.example.tfgprueba1.ui.theme.AzulOscuroApp
import com.example.tfgprueba1.ui.theme.AzulProte
import com.example.tfgprueba1.ui.theme.BlancoPuro
import com.example.tfgprueba1.ui.theme.FondoCalculoCalorias
import com.example.tfgprueba1.ui.theme.GrisFuerte
import com.example.tfgprueba1.ui.theme.GrisSuave
import com.example.tfgprueba1.ui.theme.NegroPuro
import com.example.tfgprueba1.ui.theme.RosaCarbos
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


object CaloryPageScreen: Tab {
    override val options: TabOptions
        @Composable
        get(){
            val iconoCalorias = rememberVectorPainter(Icons.Default.Restaurant)
            return remember() {
                TabOptions(
                    index = 1u,
                    title = "Alimentación",
                    icon = iconoCalorias
                )
            }
        }


    @SuppressLint("SuspiciousIndentation")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val userDataStore = remember { UserDataStore(context) }
        val coroutineScope = rememberCoroutineScope()
        val dataStore = remember { UserDataStore(context) }
        val savedCaloriasObjetivo by userDataStore.caloriasObjetivoFlow.collectAsState(initial = null)

        var currentDate by remember { mutableStateOf(fechaAlimentos.fechaSeleccionada) }

        // CARGA DE DATOS
        LaunchedEffect(Unit) {
            val listaGuardada = dataStore.getListaAlimentos()
            if (listaComidasCaloryPage.listaAlimentosLog.isEmpty() && listaGuardada.isNotEmpty()) {
                listaComidasCaloryPage.listaAlimentosLog.addAll(listaGuardada)
            }
        }


        //CÁLCULOS TOTALES
        val alimentosHoy = listaComidasCaloryPage.listaAlimentosLog.filter { it.fecha == currentDate.toString() }
        val totalKcalDia = alimentosHoy.sumOf { (it.calorias * it.cantidadAlimento) / (if(it.cantidadMedida <= 0.0) 1.0 else it.cantidadMedida) }
        val totalCarbsDia = alimentosHoy.sumOf { (it.carbohidratos * it.cantidadAlimento) / (if(it.cantidadMedida <= 0.0) 1.0 else it.cantidadMedida) }
        val totalProteDia = alimentosHoy.sumOf { (it.proteinas * it.cantidadAlimento) / (if(it.cantidadMedida <= 0.0) 1.0 else it.cantidadMedida) }
        val totalGrasasDia = alimentosHoy.sumOf { (it.grasas * it.cantidadAlimento) / (if(it.cantidadMedida <= 0.0) 1.0 else it.cantidadMedida) }

        val objetivoKcal = savedCaloriasObjetivo?.calorias?.toDouble() ?:2000.0
        val objetivoCarbs = savedCaloriasObjetivo?.carbohidratos?.toDouble() ?:200.0
        val objetivoProte = savedCaloriasObjetivo?.proteinas?.toDouble() ?:100.0
        val objetivoGrasas = savedCaloriasObjetivo?.grasas?.toDouble() ?:100.0

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconButton(
                onClick = {
                    navigator.push(AjustesCaloryPage)
                },
                modifier = Modifier.align (Alignment.End)
            ) {
                Icon(Icons.Default.Settings,"Ajustes" ,tint = NegroPuro,)
            }

            Row(
                modifier = Modifier.fillMaxWidth(0.9f).padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroProgressBar("Carbohidratos", totalCarbsDia, objetivoCarbs, RosaCarbos)
                MacroProgressBar("Proteínas", totalProteDia, objetivoProte, AzulProte)
                MacroProgressBar("Grasas", totalGrasasDia, objetivoGrasas, AmarilloGrasas)
            }




            CaloryGaugeChart(actual = totalKcalDia, objetivo = objetivoKcal)

            val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)}
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .background(AzulBarraBusqueda, shape = RoundedCornerShape(20.dp))
                    .border(width = 2.dp, color = AzulOscuroApp, shape = RoundedCornerShape(20.dp)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                IconButton(onClick = {
                    currentDate = currentDate.minusDays(1)
                    fechaAlimentos.fechaSeleccionada = currentDate
                }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") }

                Text(text = currentDate.format(formatter).replaceFirstChar { it.uppercase() },
                    fontSize = 18.sp, fontWeight = FontWeight.Bold)

                IconButton(onClick = {
                    currentDate = currentDate.plusDays(1)
                    fechaAlimentos.fechaSeleccionada = currentDate
                }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "Siguiente") }
            }

            // ... CONTENEDORES DE COMIDAS ...
            Column(
                modifier = Modifier.padding(top = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                momentoComidas.momentoComidasLog.forEach { nombreComida ->
                    ContenedorAlimentosItem(
                        nombreSeccion = nombreComida,
                        currentDate = currentDate,
                        listaDeComidas = listaComidasCaloryPage.listaAlimentosLog
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    @Composable
    fun CaloryGaugeChart(actual: Double, objetivo: Double) {
        val sweepTotal = 240f // Más pronunciada que una semicircular (180)
        val startAngle = 150f
        val ratio = (actual / objetivo).toFloat().coerceIn(0f, 1f)
        val progressSweep = ratio * sweepTotal
        val color = if (actual > objetivo) Color.Red else AzulCalorias

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Arco de fondo
                drawArc(color = Color.LightGray.copy(alpha = 0.2f), startAngle = startAngle, sweepAngle = sweepTotal,
                    useCenter = false, style = Stroke(width = 28f, cap = StrokeCap.Round))
                // Arco de progreso
                drawArc(color = color, startAngle = startAngle, sweepAngle = progressSweep,
                    useCenter = false, style = Stroke(width = 28f, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "${actual.toInt()}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = if(actual > objetivo) Color.Red else NegroPuro)
                Text(text = "kcal consumidas", fontSize = 12.sp, color = GrisSuave)
                Text(text = "de ${objetivo.toInt()}", fontSize = 11.sp, color = GrisFuerte)
            }
        }
    }

    @Composable
    fun MacroProgressBar(label: String, actual: Double, objetivo: Double, color: Color) {
        val progress = (actual / objetivo).toFloat().coerceIn(0f, 1f)
        val barColor = if (actual > objetivo) Color.Red else color

        Column(modifier = Modifier.width(105.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier
                .height(10.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(5.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))) {
                Box(modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(5.dp))
                    .background(barColor))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "${actual.toInt()} / ${objetivo.toInt()}g", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NegroPuro)
            Text(text = label, fontSize = 11.sp, color = GrisSuave)
        }
    }

    @Composable
    fun ContenedorAlimentosItem(nombreSeccion: String, currentDate: java.time.LocalDate, listaDeComidas: List<DatosAlimento>) {
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow
        val filtrados = listaDeComidas.filter { it.momentoComida == nombreSeccion && it.fecha == currentDate.toString() }

        val totalKcal = filtrados.sumOf { (it.calorias * it.cantidadAlimento) / (if(it.cantidadMedida <= 0.0) 1.0 else it.cantidadMedida) }.toInt()
        val totalCarbs = filtrados.sumOf { (it.carbohidratos * it.cantidadAlimento) / (if(it.cantidadMedida <= 0.0) 1.0 else it.cantidadMedida) }.toInt()
        val totalProte = filtrados.sumOf { (it.proteinas * it.cantidadAlimento) / (if(it.cantidadMedida <= 0.0) 1.0 else it.cantidadMedida) }.toInt()
        val totalGrasas = filtrados.sumOf { (it.grasas * it.cantidadAlimento) / (if(it.cantidadMedida <= 0.0) 1.0 else it.cantidadMedida) }.toInt()

        Box(modifier = Modifier
            .fillMaxWidth(0.95f)
            .heightIn(min = 100.dp)
            .border(1.dp, NegroPuro, RoundedCornerShape(15.dp))
            ){
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))) {
                    Row(modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth()
                        .background(AzulOscuroApp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = nombreSeccion, color = BlancoPuro, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    }
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (filtrados.isEmpty()) {
                            Text("No hay alimentos", color = Color.Gray, modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp), textAlign = TextAlign.Center, fontSize = 12.sp)
                        } else {
                            filtrados.forEach { alimento ->
                                AlimentoLogItem(alimento = alimento, onClick = { navigator.push(EditaAlimentoScreen(alimento)) })
                                HorizontalDivider(color = GrisSuave, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                    Row(modifier = Modifier
                        .height(65.dp)
                        .fillMaxWidth()
                        .background(FondoCalculoCalorias)
                        .border(width = 1.dp, color = GrisSuave)
                        .padding(horizontal = 15.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        MacroTotalCol("Carbohidratos", totalCarbs, RosaCarbos)
                        MacroTotalCol("Proteinas", totalProte, AzulProte)
                        MacroTotalCol("Grasas", totalGrasas, AmarilloGrasas)
                        MacroTotalCol("Kcal", totalKcal, AzulCalorias)
                    }
                    Row(modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth()
                        .background(AzulOscuroApp)
                        .clickable {
                            comida.momentoComida = nombreSeccion; navigator.push(MisAlimentosScreen)
                        },
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Add, "Añadir alimento", tint = BlancoPuro)
                    }
                }
            }
    }

    @Composable
    fun MacroTotalCol(label: String, value: Int, color: Color) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = label, fontSize = 10.sp, color = color)
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

