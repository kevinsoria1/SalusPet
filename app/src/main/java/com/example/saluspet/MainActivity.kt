package com.example.saluspet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.saluspet.core.presentation.MainScreen
import com.example.saluspet.features.auth.presentation.LoginScreen
import com.example.saluspet.ui.theme.SalusPetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SalusPetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "login") {

                        // 1. Pantalla de Login (que ahora también incluye el Registro)
                        composable(route = "login") {
                            LoginScreen(
                                onLoginSuccess = { // ⬅️ ¡AQUÍ ESTÁ EL CAMBIO!
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. Contenedor Principal con la barra de navegación inferior
                        composable(route = "main") {
                            MainScreen(onLogout = {
                                navController.navigate("login") {
                                    popUpTo("main") { inclusive = true }
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}