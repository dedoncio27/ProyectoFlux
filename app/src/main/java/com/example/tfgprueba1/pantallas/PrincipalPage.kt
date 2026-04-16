package com.example.tfgprueba1.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import kotlinx.coroutines.launch

object PrincipalPage : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val iconoPersona = rememberVectorPainter(Icons.Default.Person)
            return remember {
                TabOptions(
                    index = 0u,
                    title = "Principal",
                    icon = iconoPersona
                )
            }
        }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val userDataStore = remember { UserDataStore(context) }
        val coroutineScope = rememberCoroutineScope()
        val navigator = LocalNavigator.currentOrThrow.parent ?: LocalNavigator.currentOrThrow

        val savedAltura by userDataStore.alturaFlow.collectAsState("")
        val savedPeso by userDataStore.pesoFlow.collectAsState("")
        val savedEdad by userDataStore.edadFlow.collectAsState("")
        val savedSexo by userDataStore.sexoFlow.collectAsState("")
        val savedActividad by userDataStore.actividadFlow.collectAsState("")
        val savedObjetivo by userDataStore.objetivoFlow.collectAsState("Mantener peso")

        // Estados locales para la edición
        var altura by remember { mutableStateOf("") }
        var peso by remember { mutableStateOf("") }
        var edad by remember { mutableStateOf("") }
        var sexo by remember { mutableStateOf("") }
        var actividad by remember { mutableStateOf("") }
        var objetivo by remember { mutableStateOf("Mantener peso") }

        // Sincronizar cuando los datos cargan de DataStore
        LaunchedEffect(savedAltura, savedPeso, savedEdad, savedSexo, savedActividad, savedObjetivo) {
            altura = savedAltura
            peso = savedPeso
            edad = savedEdad
            sexo = savedSexo
            actividad = savedActividad
            objetivo = savedObjetivo
        }

        val esFormularioValido = altura.toDoubleOrNull() != null && 
                                peso.toDoubleOrNull() != null && 
                                edad.toIntOrNull() != null && 
                                sexo.isNotBlank() && 
                                actividad.isNotBlank()

        Column(
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        ) {
            // Campos numéricos
            CustomOutlinedTextField(value = altura, onValueChange = { if(it.all { c -> c.isDigit() }) altura = it }, label = "Altura (cm)")
            CustomOutlinedTextField(value = peso, onValueChange = { if(it.all { c -> c.isDigit() || c == '.' }) peso = it }, label = "Peso (Kg)")
            CustomOutlinedTextField(value = edad, onValueChange = { if(it.all { c -> c.isDigit() }) edad = it }, label = "Edad")

            // Menús desplegables
            CustomDropdownMenu(
                label = "Sexo",
                selectedOption = sexo,
                options = listOf("Hombre", "Mujer"),
                onOptionSelected = { sexo = it }
            )

            CustomDropdownMenu(
                label = "Actividad",
                selectedOption = actividad,
                options = listOf("Casi nada de actividad", "Poca actividad", "Actividad moderada", "Actividad alta", "Actividad muy intensa"),
                onOptionSelected = { actividad = it }
            )

            CustomDropdownMenu(
                label = "Objetivo",
                selectedOption = objetivo,
                options = listOf("Mantener peso", "Subir de peso lentamente", "Subir de peso rápido", "Bajar de peso lentamente", "Bajar de peso rápido"),
                onOptionSelected = { objetivo = it }
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        userDataStore.saveUserData(altura, peso, edad, sexo, actividad, objetivo)
                        navigator.push(AjustesCaloryPage)
                    }
                },
                enabled = esFormularioValido,
                modifier = Modifier.padding(top = 24.dp).fillMaxWidth(0.8f)
            ) {
                Text(if (esFormularioValido) "Guardar datos" else "Completa todos los campos")
            }
        }
    }
}

@Composable
fun CustomOutlinedTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(0.8f)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdownMenu(label: String, selectedOption: String, options: List<String>, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = { },
            label = { Text(label) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
