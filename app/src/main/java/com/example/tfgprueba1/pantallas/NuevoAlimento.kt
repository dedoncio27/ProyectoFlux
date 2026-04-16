package com.example.tfgprueba1.pantallas

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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


object NuevoAlimentoScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val focusManager = LocalFocusManager.current
        val navigator = LocalNavigator.currentOrThrow
        val context = LocalContext.current
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        
        var nombre by remember { mutableStateOf("") }
        var descripcion by remember { mutableStateOf("") }
        var cantidad by remember { mutableStateOf("") }
        var marca by remember {mutableStateOf("")}
        var medida by remember { mutableStateOf("g") }
        var expandedMedida by remember { mutableStateOf(false) }
        val opcionesMedida = listOf("g", "ml")

        var calorias by remember { mutableStateOf("") }
        var proteinas by remember { mutableStateOf("") }
        var carbohidratos by remember { mutableStateOf("") }
        var grasas by remember { mutableStateOf("") }
        val coroutineScope = rememberCoroutineScope()

        Scaffold(
            modifier = Modifier.pointerInput(Unit){
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Nuevo Alimento", color = BlancoPuro) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = BlancoPuro)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuroApp)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        calorias= redondear(calorias)
                        proteinas= redondear(proteinas)
                        carbohidratos= redondear(carbohidratos)
                        grasas= redondear(grasas)

                        if (nombre.isBlank() || cantidad.isBlank() || calorias.isBlank() || carbohidratos.isBlank() || proteinas.isBlank() || grasas.isBlank()) {
                            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                            return@FloatingActionButton
                        }else if (cantidad.toFloatOrNull()==null || calorias.toFloatOrNull()==null || carbohidratos.toFloatOrNull()==null || proteinas.toFloatOrNull()==null || grasas.toFloatOrNull()==null ){
                            Toast.makeText(context, "Completa con datos numéricos", Toast.LENGTH_SHORT).show()
                            return@FloatingActionButton
                        }

                        // Genera un ID único usando Firestore
                        val docRef = db.collection("MisAlimentos").document()
                        val idUnico = docRef.id
                        val emailUsuario = currentUser?.email?: "anonimo"

                        val nuevoAlimento = hashMapOf(
                            "id" to idUnico,
                            "email" to emailUsuario,
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
                                    Toast.makeText(context, "Alimento guardado con éxito", Toast.LENGTH_SHORT).show()
                                    navigator.pop()
                                }
                            }



                    },
                    containerColor = AzulOscuroApp,
                    contentColor = BlancoPuro
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Guardar")
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Información del alimento",
                    color = NegroPuro,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CustomTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = "Nombre del alimento",
                            keyboardType = KeyboardType.Text
                        )
                        CustomTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = "Descripción",
                            keyboardType = KeyboardType.Text
                        )
                        CustomTextField(
                            value = marca,
                            onValueChange = { marca = it },
                            label = "Marca",
                            keyboardType = KeyboardType.Text
                        )
                    }

                    MacroPieChart(
                        carbs = carbohidratos,
                        proteins = proteinas,
                        fats = grasas
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        CustomTextField(
                            value = cantidad,
                            onValueChange = { cantidad = it },
                            label = "Tamaño de porción",
                            keyboardType = KeyboardType.Number
                        )
                    }

                    ExposedDropdownMenuBox(
                        expanded = expandedMedida,
                        onExpandedChange = { expandedMedida = !expandedMedida },
                        modifier = Modifier.width(110.dp)
                    ) {
                        OutlinedTextField(
                            value = medida,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unidad") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMedida) },
                            modifier = Modifier.menuAnchor(),
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = NegroPuro,
                                unfocusedTextColor = NegroPuro,
                                focusedBorderColor = AzulOscuroApp,
                                unfocusedBorderColor = GrisSuave,
                                focusedLabelColor = AzulOscuroApp,
                                unfocusedLabelColor = GrisSuave
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMedida,
                            onDismissRequest = { expandedMedida = false }
                        ) {
                            opcionesMedida.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = {
                                        medida = opcion
                                        expandedMedida = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))

                Text("Valores nutricionales (por cantidad)", color = GrisSuave, fontWeight = FontWeight.SemiBold)

                CustomTextField(value = calorias, onValueChange = { calorias = it }, label = "Calorías (kcal)", keyboardType = KeyboardType.Number)
                CustomTextField(value = carbohidratos, onValueChange = { carbohidratos = it }, label = "Carbohidratos ", keyboardType = KeyboardType.Number)
                CustomTextField(value = proteinas, onValueChange = { proteinas = it }, label = "Proteínas ", keyboardType = KeyboardType.Number)
                CustomTextField(value = grasas, onValueChange = { grasas = it }, label = "Grasas ", keyboardType = KeyboardType.Number)

                Spacer(modifier = Modifier.height(80.dp)) // Espacio para no tapar con el FAB
            }
        }
    }

    @Composable
    fun MacroPieChart(carbs: String, proteins: String, fats: String) {
        val c = carbs.toFloatOrNull() ?: 0f
        val p = proteins.toFloatOrNull() ?: 0f
        val f = fats.toFloatOrNull() ?: 0f
        val total = c + p + f

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    if (total <= 0f) {
                        drawCircle(color = Color.LightGray, style = Stroke(width = 8f))
                    } else {
                        val sweepCarbs = (c / total) * 360f
                        val sweepProteins = (p / total) * 360f
                        val sweepFats = (f / total) * 360f

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
            }
            Text("Carbohidratos", fontSize = 12.sp, color = RosaCarbos, fontWeight = FontWeight.SemiBold)
            Text("Proteinas", fontSize = 12.sp, color = AzulProte, fontWeight = FontWeight.SemiBold)
            Text("Grasas", fontSize = 12.sp, color = AmarilloGrasas, fontWeight = FontWeight.SemiBold)
        }
    }

    @Composable
    fun CustomTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        keyboardType: KeyboardType = KeyboardType.Text
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = NegroPuro,
                unfocusedTextColor = NegroPuro,
                focusedBorderColor = AzulOscuroApp,
                unfocusedBorderColor = GrisSuave,
                focusedLabelColor = AzulOscuroApp,
                unfocusedLabelColor = GrisSuave
            ),
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            )
        )
    }
    fun redondear(value:String): String {
        return value.replace(",",".")
    }
}
