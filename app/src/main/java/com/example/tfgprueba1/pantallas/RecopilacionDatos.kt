package com.example.tfgprueba1.pantallas

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")


object usuarioActual {
    // Devuelve el email del usuario logueado, o "" si no hay sesión
    val email: String
        get() = FirebaseAuth.getInstance().currentUser?.email ?: ""

    // Devuelve el UID del usuario logueado, o "" si no hay sesión
    val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
}

class UserDataStore(private val context: Context) {

    companion object {
        val ALTURA_KEY             = stringPreferencesKey("altura")
        val PESO_KEY               = stringPreferencesKey("peso")
        val EDAD_KEY               = stringPreferencesKey("edad")
        val SEXO_KEY               = stringPreferencesKey("sexo")
        val ACTIVIDAD_KEY          = stringPreferencesKey("actividad")
        val OBJETIVO_KEY           = stringPreferencesKey("objetivo")
        val LISTA_ALIMENTOS_KEY    = stringPreferencesKey("lista_alimentos")
        val LISTA_RECETAS_KEY      = stringPreferencesKey("lista_recetas")
        val COMIDAS_CANTIDAD_KEY   = stringPreferencesKey("comidas_cantidad")
        val CALORIAS_OBJETIVO_KEY  = stringPreferencesKey("calorias_objetivo")
        val AUTO_CALCULAR_KEY      = booleanPreferencesKey("auto_calcular")
    }

    suspend fun saveUserData(
        altura: String, peso: String, edad: String,
        sexo: String, actividad: String, objetivo: String
    ) {
        context.dataStore.edit { settings ->
            settings[ALTURA_KEY]   = altura
            settings[PESO_KEY]     = peso
            settings[EDAD_KEY]     = edad
            settings[SEXO_KEY]     = sexo
            settings[ACTIVIDAD_KEY] = actividad
            settings[OBJETIVO_KEY] = objetivo
        }
    }

    suspend fun saveCaloriasObjetivo(calorias: CaloriasObjetivo) {
        val json = Gson().toJson(calorias)
        context.dataStore.edit { it[CALORIAS_OBJETIVO_KEY] = json }
    }

    suspend fun getCaloriasObjetivo(): CaloriasObjetivo? {
        val preferences = context.dataStore.data.first()
        val json = preferences[CALORIAS_OBJETIVO_KEY] ?: return null
        return Gson().fromJson(json, CaloriasObjetivo::class.java)
    }

    val caloriasObjetivoFlow: Flow<CaloriasObjetivo?> = context.dataStore.data.map { preferences ->
        val json = preferences[CALORIAS_OBJETIVO_KEY] ?: return@map null
        Gson().fromJson(json, CaloriasObjetivo::class.java)
    }

    suspend fun saveAutoCalcular(value: Boolean) {
        context.dataStore.edit { it[AUTO_CALCULAR_KEY] = value }
    }

    val autoCalcularFlow: Flow<Boolean> = context.dataStore.data.map { it[AUTO_CALCULAR_KEY] ?: true }

    suspend fun saveObjetivo(objetivo: String) {
        context.dataStore.edit { settings ->
            settings[OBJETIVO_KEY] = objetivo
        }
    }

    suspend fun saveListaAlimentos(lista: List<DatosAlimento>) {
        val json = Gson().toJson(lista)
        context.dataStore.edit { it[LISTA_ALIMENTOS_KEY] = json }
    }

    suspend fun getListaAlimentos(): List<DatosAlimento> {
        return try {
            val preferences = context.dataStore.data.first()
            val json = preferences[LISTA_ALIMENTOS_KEY] ?: ""
            if (json.isEmpty()) emptyList()
            else {
                val type = object : TypeToken<List<DatosAlimento>>() {}.type
                Gson().fromJson(json, type)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveListaRecetas(lista: List<DatosRecetas>) {
        val json = Gson().toJson(lista)
        context.dataStore.edit { it[LISTA_RECETAS_KEY] = json }
    }

    suspend fun getListaRecetas(): List<DatosRecetas> {
        return try {
            val preferences = context.dataStore.data.first()
            val json = preferences[LISTA_RECETAS_KEY] ?: ""
            if (json.isEmpty()) emptyList()
            else {
                val type = object : TypeToken<List<DatosRecetas>>() {}.type
                Gson().fromJson(json, type)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveComidasCantidad(lista: List<String>) {
        val json = Gson().toJson(lista)
        context.dataStore.edit { it[COMIDAS_CANTIDAD_KEY] = json }
    }

    suspend fun getComidasCantidad(): List<String> {
        return try {
            val preferences = context.dataStore.data.first()
            val json = preferences[COMIDAS_CANTIDAD_KEY] ?: ""
            if (json.isEmpty()) listOf("Desayuno", "Comida", "Cena")
            else {
                val type = object : TypeToken<List<String>>() {}.type
                Gson().fromJson(json, type)
            }
        } catch (e: Exception) {
            listOf("Desayuno", "Comida", "Cena")
        }
    }

    val alturaFlow:   Flow<String> = context.dataStore.data.map { it[ALTURA_KEY]   ?: "" }
    val pesoFlow:     Flow<String> = context.dataStore.data.map { it[PESO_KEY]     ?: "" }
    val edadFlow:     Flow<String> = context.dataStore.data.map { it[EDAD_KEY]     ?: "" }
    val sexoFlow:     Flow<String> = context.dataStore.data.map { it[SEXO_KEY]     ?: "" }
    val actividadFlow: Flow<String> = context.dataStore.data.map { it[ACTIVIDAD_KEY] ?: "" }
    val objetivoFlow: Flow<String> = context.dataStore.data.map { it[OBJETIVO_KEY] ?: "Mantener peso" }
}


object comida {
    var momentoComida: String = ""
}

object fechaAlimentos {
    var fechaSeleccionada: LocalDate = LocalDate.now()
}

data class DatosAlimento(
    val id: String = "",
    var nombre: String = "",
    var cantidadMedida: Double = 0.0,
    var cantidadAlimento: Double = 0.0,
    var calorias: Double = 0.0,
    var carbohidratos: Double = 0.0,
    var proteinas: Double = 0.0,
    var grasas: Double = 0.0,
    var fecha: String = LocalDate.now().toString(),
    var momentoComida: String = "",
    var marca: String = "",
    var medida: String = ""
)

object listaComidasCaloryPage {
    val listaAlimentosLog = mutableStateListOf<DatosAlimento>()
}

object listaRecetasCaloryPage {
    val listaRecetasLog = mutableStateListOf<DatosRecetas>()
}

object momentoComidas {
    val momentoComidasLog = mutableStateListOf("Desayuno", "Comida", "Cena")
}

data class CaloriasObjetivo(
    var calorias: Int = 2000,
    var proteinas: Double = 0.0,
    var carbohidratos: Double = 0.0,
    var grasas: Double = 0.0,
    var nombreDieta: String = "Estándar",
    var customHC: String = "",
    var customProt: String = "",
    var customFat: String = ""
)

data class DatosRecetas(
    val id: String = "",
    var nombre: String = "",
    var descripcion: String = "",
    var cantidadMedida: Double = 0.0,
    var cantidadAlimento: Double = 0.0,
    var calorias: Double = 0.0,
    var carbohidratos: Double = 0.0,
    var proteinas: Double = 0.0,
    var grasas: Double = 0.0,
    var fecha: String = LocalDate.now().toString(),
    var momentoComida: String = "",
    var medida: String = "",
    var listaIngredientes: List<String> = emptyList(),
    var CantidadesReceta: Map<String, String> = emptyMap()
)