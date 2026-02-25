package com.example.saluspet.features.pets.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saluspet.features.calendar.presentation.CalendarViewModel
import com.example.saluspet.ui.theme.*

@Composable
fun PetHomeScreen(calendarViewModel: CalendarViewModel) {
    val proximasCitas = calendarViewModel.listaCitas // Obtenemos las citas del ViewModel compartido

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "¡Hola de nuevo!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
        Text(text = "Este es el estado de tus mascotas hoy.", fontSize = 16.sp, color = TextColorGray)

        Spacer(modifier = Modifier.height(20.dp))

        // SECCIÓN PROACTIVA: Recordatorios
        if (proximasCitas.isNotEmpty()) {
            Text(text = "Recordatorios importantes", fontWeight = FontWeight.Bold, color = TextColorDark)
            Spacer(modifier = Modifier.height(8.dp))

            // Solo mostramos la cita más reciente como aviso destacado
            val proxima = proximasCitas.first()
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)), // Amarillo suave
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFFBC02D))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Próxima cita: ${proxima.titulo}", fontWeight = FontWeight.Bold)
                        Text(text = "${proxima.fecha} a las ${proxima.hora}", fontSize = 14.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // LISTA DE MASCOTAS (Tu código anterior)
        Text(text = "Mis Mascotas", fontWeight = FontWeight.Bold, color = TextColorDark)
        Spacer(modifier = Modifier.height(8.dp))

        // Aquí iría el LazyColumn que ya tienes con las tarjetas de tus mascotas
        // ...
    }
}