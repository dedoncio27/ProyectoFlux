package com.example.tfgprueba1.pantallas

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabDisposable
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.example.tfgprueba1.ui.theme.AzulOscuroApp
import com.example.tfgprueba1.ui.theme.BlancoPuro

class BottomBarScreen : Screen {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
    @Composable
    //Toda la lógica de la barra de opciones
    override fun Content() {
        
        //Se crea con la función TabNavigator de voyager y metemos
        TabNavigator(
            PrincipalPage,
            //Declaramos cuál será la página que primero se ejecute
            tabDisposable = {
                TabDisposable(
                    //Aquí añadimos todas las opciones y pestañas que va a tener nuestro navigator
                    it,
                    listOf(PrincipalPage, CaloryPageScreen, TrainingPageScreen)
                )
            }
        ){ tabNavigator ->
            //Un scaffold simplemente para darle un formato
            Scaffold(
                //Una barra superior en la que escoja simplemente el título de la página que se encuentre el usuario en ese momento
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(tabNavigator.current.options.title, color = BlancoPuro) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = AzulOscuroApp
                        )
                    )
                },
                //Barra inferior donde van a estar todas las opciones
                bottomBar = {
                    NavigationBar(
                        containerColor = AzulOscuroApp
                    ) {
                        //Variable para que identifique donde se encuentra el usuario
                        val currentTabNavigator = LocalTabNavigator.current

                        //Metemos de uno en uno todas las opciones que va a tener nuestra bottom bar con los comandos que recogen los datos de la pestaña
                        NavigationBarItem(
                            selected = currentTabNavigator.current.key == PrincipalPage.key,
                            label = {Text(PrincipalPage.options.title, color = BlancoPuro)},
                            icon = { Icon(painter = PrincipalPage.options.icon!!, contentDescription = null, tint = BlancoPuro) },
                            onClick = {currentTabNavigator.current= PrincipalPage},
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BlancoPuro,
                                unselectedIconColor = BlancoPuro.copy(alpha = 0.7f),
                                selectedTextColor = BlancoPuro,
                                unselectedTextColor = BlancoPuro.copy(alpha = 0.7f),
                                indicatorColor = BlancoPuro.copy(alpha = 0.2f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentTabNavigator.current.key == CaloryPageScreen.key,
                            label = {Text(CaloryPageScreen.options.title, color = BlancoPuro)},
                            icon = { Icon(painter = CaloryPageScreen.options.icon!!, contentDescription = null, tint = BlancoPuro) },
                            onClick = {currentTabNavigator.current= CaloryPageScreen},
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BlancoPuro,
                                unselectedIconColor = BlancoPuro.copy(alpha = 0.7f),
                                selectedTextColor = BlancoPuro,
                                unselectedTextColor = BlancoPuro.copy(alpha = 0.7f),
                                indicatorColor = BlancoPuro.copy(alpha = 0.2f)
                            )
                        )
                        NavigationBarItem(
                            selected = currentTabNavigator.current.key == TrainingPageScreen.key,
                            label = {Text(TrainingPageScreen.options.title, color = BlancoPuro)},
                            icon = { Icon(painter = TrainingPageScreen.options.icon!!, contentDescription = null, tint = BlancoPuro) },
                            onClick = {currentTabNavigator.current= TrainingPageScreen},
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BlancoPuro,
                                unselectedIconColor = BlancoPuro.copy(alpha = 0.7f),
                                selectedTextColor = BlancoPuro,
                                unselectedTextColor = BlancoPuro.copy(alpha = 0.7f),
                                indicatorColor = BlancoPuro.copy(alpha = 0.2f)
                            )
                        )

                    }
                }
            ) { paddingValues ->
                // Aplicamos el padding aquí para que el contenido de las pestañas
                // respete el espacio de la TopAppBar y la NavigationBar
                Box(modifier = Modifier.padding(paddingValues)) {
                    CurrentTab()
                }
            }
        }

    }
}
