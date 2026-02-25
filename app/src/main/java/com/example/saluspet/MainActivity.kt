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

                        // 1. Pantalla de Login
                        composable(route = "login") {
                            LoginScreen(
                                onNavigateToHome = {
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    // Futura pantalla de registro
                                }
                            )
                        }

                        // 2. Contenedor Principal
                        composable(route = "main") {
                            // Pasamos la lógica de Logout aquí
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