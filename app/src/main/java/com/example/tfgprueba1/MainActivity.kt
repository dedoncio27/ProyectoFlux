package com.example.tfgprueba1

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import cafe.adriel.voyager.navigator.Navigator
import com.example.tfgprueba1.pantallas.LoginScreen

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Firebase se inicializa automáticamente mediante el plugin google-services
        // en la mayoría de los casos. Si necesitas inicializarlo manualmente,
        // asegúrate de que no cause conflictos con el plugin.

        enableEdgeToEdge()
        setContent {
            MaterialTheme{
                Navigator(screen = LoginScreen())
            }
        }
    }
}