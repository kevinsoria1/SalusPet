package com.example.saluspet.core.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.example.saluspet.features.pets.presentation.PetHomeScreen
import com.example.saluspet.features.pets.presentation.PetHistoryScreen
import com.example.saluspet.features.pets.presentation.PetViewModel
import com.example.saluspet.features.calendar.presentation.CalendarScreen
import com.example.saluspet.features.calendar.presentation.CalendarViewModel
import com.example.saluspet.features.auth.presentation.ProfileScreen
import com.example.saluspet.ui.theme.*

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : BottomNavItem("home_tab", Icons.Filled.Home, "Inicio")
    object History : BottomNavItem("history_tab", Icons.Filled.Description, "Historial")
    object Calendar : BottomNavItem("calendar_tab", Icons.Filled.DateRange, "Agenda")
    object Profile : BottomNavItem("profile_tab", Icons.Filled.Person, "Perfil")
}

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val bottomNavController = rememberNavController()

    // 1. Compose se encarga de instanciar y mantener vivos los ViewModels
    val calendarViewModel: CalendarViewModel = viewModel()
    val petViewModel: PetViewModel = viewModel()

    Scaffold(
        bottomBar = { SalusPetBottomBar(navController = bottomNavController) }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Home.route) {
                // Pasamos AMBOS ViewModels a la Home (para avisos de agenda + lista de mascotas)
                PetHomeScreen(
                    calendarViewModel = calendarViewModel,
                    petViewModel = petViewModel
                )
            }

            composable(BottomNavItem.History.route) {
                PetHistoryScreen()
            }

            composable(BottomNavItem.Calendar.route) {
                CalendarScreen(calendarViewModel = calendarViewModel)
            }

            composable(BottomNavItem.Profile.route) {
                // Pasamos el petViewModel al perfil para ver los detalles y el contador
                ProfileScreen(
                    onLogout = onLogout,
                    petViewModel = petViewModel
                )
            }
        }
    }
}

@Composable
fun SalusPetBottomBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.History,
        BottomNavItem.Calendar,
        BottomNavItem.Profile
    )

    NavigationBar(
        containerColor = PastelBlueBackgroundLighter,
        contentColor = TextColorDark
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TextColorDark,
                    unselectedIconColor = TextColorGray,
                    selectedTextColor = TextColorDark,
                    unselectedTextColor = TextColorGray,
                    indicatorColor = PastelGreenPrimary
                )
            )
        }
    }
}