package com.example.saluspet.features.pets.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saluspet.ui.theme.*

@Composable
fun PetHistoryScreen() {
    val informes = listOf("Vacuna Rabia - Luna", "Desparasitación - Milo", "Analítica 2024")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Historial Médico", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(informes) { informe ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = Color(0xFFE57373))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = informe, fontWeight = FontWeight.Medium, color = TextColorDark)
                    }
                }
            }
        }
    }
}