package com.example.tfgprueba1.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.tfgprueba1.ui.theme.AmarilloGrasas
import com.example.tfgprueba1.ui.theme.AzulBarraBusqueda
import com.example.tfgprueba1.ui.theme.AzulCalorias
import com.example.tfgprueba1.ui.theme.AzulOscuroApp
import com.example.tfgprueba1.ui.theme.AzulProte
import com.example.tfgprueba1.ui.theme.BlancoPuro
import com.example.tfgprueba1.ui.theme.GrisFuerte
import com.example.tfgprueba1.ui.theme.GrisSuave
import com.example.tfgprueba1.ui.theme.NegroPuro
import com.example.tfgprueba1.ui.theme.RosaCarbos
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName


data class Receta(
    val id: String = "",
    @get:PropertyName("NombreReceta") @set:PropertyName("NombreReceta") var NombreReceta: String = "",
    @get:PropertyName("Descripcion")    @set:PropertyName("Descripcion")    var Descripcion: String = "",
    @get:PropertyName("CantidadMedida")       @set:PropertyName("CantidadMedida")       var CantidadMedida: String = "",
    @get:PropertyName("CantidadTotal")       @set:PropertyName("CantidadTotal")       var CantidadTotal: String = "",
    @get:PropertyName("Calorias")       @set:PropertyName("Calorias")       var Calorias: String = "",
    @get:PropertyName("Carbohidratos")  @set:PropertyName("Carbohidratos")  var Carbohidratos: String = "",
    @get:PropertyName("Proteinas")      @set:PropertyName("Proteinas")      var Proteinas: String = "",
    @get:PropertyName("Grasas")         @set:PropertyName("Grasas")         var Grasas: String = "",
    @get:PropertyName("Marca")          @set:PropertyName("Marca")          var Marca: String = "",
    @get:PropertyName("Medida")         @set:PropertyName("Medida")         var Medida: String = "g",
    @get:PropertyName("email")          @set:PropertyName("email")          var email: String = "",
    @get:PropertyName("ListaIngredientes")  @set:PropertyName("ListaIngredientes")  var ListaIngredientes: List<String> = emptyList(),
    @get:PropertyName("CantidadesReceta")  @set:PropertyName("CantidadesReceta")  var CantidadesReceta: Map<String, String> = emptyMap()
)


object MisRecetasScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator      = LocalNavigator.currentOrThrow
        val listaRecetas = remember { mutableStateListOf<Receta>() }
        val db             = FirebaseFirestore.getInstance()
        var selectedTabIndex by remember { mutableStateOf(2) }
        var searchText       by remember { mutableStateOf("") }

        
        DisposableEffect(Unit) {
            val emailUsuario = usuarioActual.email

            val registration = db.collection("Recetas")
                .whereEqualTo("email", emailUsuario)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener
                    if (snapshot != null) {
                        listaRecetas.clear()
                        for (doc in snapshot.documents) {
                            val receta = Receta(
                                id             = doc.id,
                                email          = doc.getString("email") ?: "",
                                NombreReceta = doc.getString("NombreReceta") ?: "Sin nombre",
                                Descripcion    = doc.getString("Descripcion") ?: "",
                                CantidadMedida = doc.getString("CantidadMedidaReceta") ?: "100",
                                CantidadTotal = doc.getString("CantidadTotalReceta") ?: "0",
                                Calorias       = doc.getString("CaloriasReceta") ?: "0",
                                Carbohidratos  = doc.getString("CarbohidratosReceta") ?: "0",
                                Proteinas      = doc.getString("ProteinasReceta") ?: "0",
                                Grasas         = doc.getString("GrasasReceta") ?: "0",
                                Medida         = doc.getString("MedidaReceta") ?: "g",
                                ListaIngredientes = doc.get("AlimentosReceta") as? List<String> ?: emptyList(),
                                CantidadesReceta = doc.get("CantidadesReceta") as? Map<String, String> ?: emptyMap()
                            )
                            listaRecetas.add(receta)
                        }
                    }
                }

            
            onDispose {
                registration.remove()
            }
        }

        Scaffold(
            topBar = {
                Column {
                    CenterAlignedTopAppBar(
                        title = { Text("Recetas", color = BlancoPuro, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = BlancoPuro)
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AzulOscuroApp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(AzulOscuroApp)
                            .padding(0.dp)
                    ) {
                        TextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 0.dp, top = 0.dp, start = 16.dp, end = 16.dp)
                            ,
                            shape = RoundedCornerShape(15.dp),
                            placeholder = { Text("Buscar en mis recetas...", color = GrisFuerte) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = NegroPuro) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor   = AzulBarraBusqueda,
                                unfocusedContainerColor = AzulBarraBusqueda,
                                focusedTextColor        = NegroPuro,
                                unfocusedTextColor      = NegroPuro,
                                cursorColor             = AzulOscuroApp,
                                focusedIndicatorColor   = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = AzulOscuroApp,
                        contentColor = BlancoPuro,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = BlancoPuro
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = {
                                selectedTabIndex = 0
                                navigator.replace(MisAlimentosScreen)
                            },
                            text = { Text("Mis alimentos", color = BlancoPuro) }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = {
                                selectedTabIndex = 1
                                navigator.replace(BibliotecaAlimentosScreen) // Cambiado push por replace
                            },
                            text = { Text("Biblioteca", color = BlancoPuro) }
                        )
                        Tab(
                            selected = selectedTabIndex == 2,
                            onClick = {
                                selectedTabIndex = 2
                            },
                            text = { Text("Mis recetas", color = BlancoPuro) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BlancoPuro)
                    .padding(paddingValues)
            ) {
                Button(
                    onClick = { navigator.push(NuevaRecetaScreen) },
                    modifier = Modifier.fillMaxWidth()
                        .padding(bottom = 5.dp, top = 5.dp, start = 16.dp, end = 16.dp)
                    ,
                    colors = ButtonDefaults.buttonColors(AzulBarraBusqueda)
                ){
                    Text("Añadir nueva receta", color = BlancoPuro)
                }


                val filteredRecetas = listaRecetas.filter {
                    it.NombreReceta.contains(searchText, ignoreCase = true)
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredRecetas) { Receta ->
                        AlimentoItemRecetas(Receta, onClick = {
                            val miReceta = DatosRecetas(
                                id              = Receta.id,
                                nombre          = Receta.NombreReceta,
                                cantidadMedida  = Receta.CantidadMedida.toDoubleOrNull() ?: 0.0,
                                cantidadAlimento = Receta.CantidadTotal.toDoubleOrNull() ?: 0.0,
                                calorias        = Receta.Calorias.toDoubleOrNull() ?: 0.0,
                                carbohidratos   = Receta.Carbohidratos.toDoubleOrNull() ?: 0.0,
                                proteinas       = Receta.Proteinas.toDoubleOrNull() ?: 0.0,
                                grasas          = Receta.Grasas.toDoubleOrNull() ?: 0.0,
                                fecha           = fechaAlimentos.fechaSeleccionada.toString(),
                                momentoComida   = comida.momentoComida,
                                medida          = Receta.Medida,
                                listaIngredientes = Receta.ListaIngredientes,
                                CantidadesReceta = Receta.CantidadesReceta
                            )
                            navigator.push(AñadeOEditaRecetaScreen(miReceta))
                        })
                        HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}


@Composable
fun AlimentoItemRecetas(receta: Receta, onClick: () -> Unit) {
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
            // Corrección: todo usa el objeto 'receta' ahora
            Text(receta.NombreReceta, color = NegroPuro, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("${receta.CantidadMedida} ${receta.Medida}", color = GrisFuerte, fontSize = 13.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${safeToInt(receta.Calorias)} kcal", color = AzulCalorias, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Row {
                MacroMiniTextReceta("C", safeToInt(receta.Carbohidratos), RosaCarbos)
                Spacer(Modifier.width(6.dp))
                MacroMiniTextReceta("P", safeToInt(receta.Proteinas), AzulProte)
                Spacer(Modifier.width(6.dp))
                MacroMiniTextReceta("G", safeToInt(receta.Grasas), AmarilloGrasas)
            }
        }
    }
}

@Composable
fun MacroMiniTextReceta(label: String, value: String, color: Color) {
    Row {
        Text("$label: ", color = GrisSuave, fontSize = 11.sp)
        Text(value, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

