package com.example.tfgprueba1.pantallas

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.tfgprueba1.ui.theme.AmarilloGrasas
import com.example.tfgprueba1.ui.theme.AzulBarraBusqueda
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


data class EditarRecetaScreen(val recetaAEditar: DatosRecetas) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val focusManager = LocalFocusManager.current
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val coroutineScope = rememberCoroutineScope()

        var nombre by remember { mutableStateOf(recetaAEditar.nombre) }
        var descripcion by remember { mutableStateOf(recetaAEditar.descripcion) }

        var aguaAgregada by remember { mutableStateOf("0") }

        val ingredientesSeleccionados = remember { mutableStateListOf<IngredienteReceta>() }
        val misAlimentosDB = remember { mutableStateListOf<Alimento>() }
        var mostrarDialogo by remember { mutableStateOf(false) }

        
        LaunchedEffect(Unit) {
            val emailUsuario = currentUser?.email ?: ""
            db.collection("MisAlimentos")
                .whereEqualTo("email", emailUsuario)
                .get()
                .addOnSuccessListener { snapshot ->
                    val todosAlimentos = snapshot.documents.mapNotNull { doc ->
                        Alimento(
                            id = doc.id,
                            NombreAlimento = doc.getString("NombreAlimento") ?: "",
                            Cantidad = doc.getString("Cantidad") ?: "100",
                            Calorias = doc.getString("Calorias") ?: "0",
                            Carbohidratos = doc.getString("Carbohidratos") ?: "0",
                            Proteinas = doc.getString("Proteinas") ?: "0",
                            Grasas = doc.getString("Grasas") ?: "0",
                            Medida = doc.getString("Medida") ?: "g"
                        )
                    }
                    misAlimentosDB.addAll(todosAlimentos)

                    
                    recetaAEditar.listaIngredientes.forEach { id ->
                        val alimentoBase = todosAlimentos.find { it.id == id }
                        if (alimentoBase != null) {
                            
                            val cantPersonalizada = recetaAEditar.CantidadesReceta[id] ?: alimentoBase.Cantidad
                            ingredientesSeleccionados.add(IngredienteReceta(alimentoBase, cantPersonalizada))
                        }
                    }
                }
        }

        
        var totalKcal = 0.0
        var totalCarbos = 0.0
        var totalProteinas = 0.0
        var totalGrasas = 0.0
        var pesoIngredientes = 0.0

        ingredientesSeleccionados.forEach { item ->
            val cantOriginal = item.alimento.Cantidad.toDoubleOrNull() ?: 1.0
            val cantNueva = item.cantidadPersonalizada.toDoubleOrNull() ?: 0.0

            val proporcion = if (cantOriginal > 0) cantNueva / cantOriginal else 0.0

            totalKcal += (item.alimento.Calorias.toDoubleOrNull() ?: 0.0) * proporcion
            totalCarbos += (item.alimento.Carbohidratos.toDoubleOrNull() ?: 0.0) * proporcion
            totalProteinas += (item.alimento.Proteinas.toDoubleOrNull() ?: 0.0) * proporcion
            totalGrasas += (item.alimento.Grasas.toDoubleOrNull() ?: 0.0) * proporcion
            pesoIngredientes += cantNueva
        }

        val pesoAgua = aguaAgregada.toDoubleOrNull() ?: 0.0
        val pesoTotal = pesoIngredientes + pesoAgua

        Scaffold(
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Editar Receta", color = BlancoPuro, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = BlancoPuro)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AzulOscuroApp)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        if (nombre.isBlank() || ingredientesSeleccionados.isEmpty()) {
                            Toast.makeText(context, "Añade un nombre y al menos un ingrediente", Toast.LENGTH_SHORT).show()
                            return@FloatingActionButton
                        }

                        
                        val listaIds = ingredientesSeleccionados.map { it.alimento.id }
                        val mapaCantidades = ingredientesSeleccionados.associate { it.alimento.id to it.cantidadPersonalizada }

                        
                        val updateReceta = mapOf(
                            "NombreReceta" to nombre,
                            "Descripcion" to descripcion,
                            "CantidadMedidaReceta" to pesoTotal.toString(),
                            "CantidadTotalReceta" to pesoTotal.toString(),
                            "CaloriasReceta" to totalKcal.toString(),
                            "ProteinasReceta" to totalProteinas.toString(),
                            "CarbohidratosReceta" to totalCarbos.toString(),
                            "GrasasReceta" to totalGrasas.toString(),
                            "AlimentosReceta" to listaIds,
                            "CantidadesReceta" to mapaCantidades
                        )

                        db.collection("Recetas").document(recetaAEditar.id).update(updateReceta)
                            .addOnSuccessListener {
                                coroutineScope.launch {
                                    Toast.makeText(context, "Receta actualizada con éxito", Toast.LENGTH_SHORT).show()
                                    navigator.pop()
                                }
                            }
                    },
                    containerColor = AzulOscuroApp,
                    contentColor = BlancoPuro
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Guardar Cambios")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BlancoPuro)
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre de la receta") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        )
                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        )
                    }

                    MacroPieChartRecetaEdicion(totalCarbos, totalProteinas, totalGrasas, totalKcal)
                }

                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MacroMiniTextRecetaEdicion("Carbos", totalCarbos.toInt().toString() + "g", RosaCarbos)
                    MacroMiniTextRecetaEdicion("Prote", totalProteinas.toInt().toString() + "g", AzulProte)
                    MacroMiniTextRecetaEdicion("Grasas", totalGrasas.toInt().toString() + "g", AmarilloGrasas)
                }

                HorizontalDivider(color = Color.LightGray)

                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ingredientes", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NegroPuro)
                    Button(
                        onClick = { mostrarDialogo = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AzulOscuroApp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Añadir")
                    }
                }

                if (ingredientesSeleccionados.isEmpty()) {
                    Text("Aún no has añadido ingredientes.", color = GrisSuave, fontSize = 14.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ingredientesSeleccionados.forEachIndexed { index, item ->
                            val cantOriginal = item.alimento.Cantidad.toDoubleOrNull() ?: 1.0
                            val cantNueva = item.cantidadPersonalizada.toDoubleOrNull() ?: 0.0
                            val prop = if (cantOriginal > 0) cantNueva / cantOriginal else 0.0

                            val kCalItem = (item.alimento.Calorias.toDoubleOrNull() ?: 0.0) * prop
                            val pItem = (item.alimento.Proteinas.toDoubleOrNull() ?: 0.0) * prop
                            val cItem = (item.alimento.Carbohidratos.toDoubleOrNull() ?: 0.0) * prop
                            val gItem = (item.alimento.Grasas.toDoubleOrNull() ?: 0.0) * prop

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.alimento.NombreAlimento, fontWeight = FontWeight.Bold, color = NegroPuro)
                                    Text("${kCalItem.toInt()} kcal | C:${cItem.toInt()} P:${pItem.toInt()} G:${gItem.toInt()}",
                                        fontSize = 12.sp, color = GrisFuerte)
                                }

                                
                                OutlinedTextField(
                                    value = item.cantidadPersonalizada,
                                    onValueChange = { newValue ->
                                        ingredientesSeleccionados[index] = item.copy(cantidadPersonalizada = newValue)
                                    },
                                    modifier = Modifier.width(80.dp).height(50.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                )
                                Text(item.alimento.Medida, modifier = Modifier.padding(start = 4.dp), fontSize = 12.sp)

                                IconButton(onClick = { ingredientesSeleccionados.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.LightGray)

                Text("Ajuste de líquidos (Agua, caldos zero...)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NegroPuro)

                OutlinedTextField(
                    value = aguaAgregada,
                    onValueChange = { aguaAgregada = it },
                    label = { Text("Agua extra (g o ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = AzulBarraBusqueda),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Peso total estimado", color = GrisFuerte, fontSize = 14.sp)
                        Text("${String.format(java.util.Locale.US, "%.1f", pesoTotal)} g", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = AzulOscuroApp)
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        
        if (mostrarDialogo) {
            Dialog(onDismissRequest = { mostrarDialogo = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BlancoPuro,
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Selecciona un ingrediente", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 12.dp))

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(misAlimentosDB) { alimento ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            ingredientesSeleccionados.add(IngredienteReceta(alimento, alimento.Cantidad))
                                            mostrarDialogo = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(alimento.NombreAlimento, fontWeight = FontWeight.Bold, color = NegroPuro)
                                        Text("Base: ${alimento.Cantidad} ${alimento.Medida} | ${alimento.Calorias} kcal", fontSize = 12.sp, color = GrisFuerte)
                                    }
                                }
                                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { mostrarDialogo = false },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Cancelar", color = BlancoPuro)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun MacroPieChartRecetaEdicion(carbs: Double, proteins: Double, fats: Double, kcal: Double) {
        val total = (carbs + proteins + fats).toFloat()
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    if (total <= 0f) {
                        drawCircle(color = Color.LightGray, style = Stroke(width = 8f))
                    } else {
                        val sweepCarbs = ((carbs.toFloat() / total) * 360f)
                        val sweepProteins = ((proteins.toFloat() / total) * 360f)
                        val sweepFats = ((fats.toFloat() / total) * 360f)
                        drawArc(color = RosaCarbos, startAngle = -90f, sweepAngle = sweepCarbs, useCenter = false, style = Stroke(width = 25f, cap = StrokeCap.Square))
                        drawArc(color = AzulProte, startAngle = -90f + sweepCarbs, sweepAngle = sweepProteins, useCenter = false, style = Stroke(width = 25f, cap = StrokeCap.Square))
                        drawArc(color = AmarilloGrasas, startAngle = -90f + sweepCarbs + sweepProteins, sweepAngle = sweepFats, useCenter = false, style = Stroke(width = 25f, cap = StrokeCap.Square))
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = kcal.toInt().toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NegroPuro)
                    Text(text = "Kcal", fontSize = 10.sp, color = GrisSuave)
                }
            }
        }
    }

    @Composable
    fun MacroMiniTextRecetaEdicion(label: String, value: String, color: Color) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(label, color = GrisSuave, fontSize = 12.sp)
        }
    }
}

