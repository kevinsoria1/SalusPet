package com.example.saluspet.features.clinics.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saluspet.features.clinics.data.HistorialClinico
import com.example.saluspet.features.pets.presentation.PetViewModel // ⬅️ Necesario para la lista de mascotas
import com.example.saluspet.ui.theme.PastelGreenPrimary
import com.example.saluspet.ui.theme.TextColorDark
import com.example.saluspet.ui.theme.TextColorGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(viewModel: HistorialViewModel, petViewModel: PetViewModel) {

    val misMascotas = petViewModel.listaMascotas

    // 🔘 Selector de mascota (por defecto selecciona la primera que tengas)
    var mascotaSeleccionada by remember { mutableStateOf(misMascotas.firstOrNull()) }
    var expandedPets by remember { mutableStateOf(false) }

    // 🚀 Disparador: Descargar datos cada vez que cambiemos la mascota en el desplegable
    LaunchedEffect(mascotaSeleccionada) {
        mascotaSeleccionada?.let { pet ->
            viewModel.cargarHistorialMascota(pet.id.toInt())
        }
    }

    val historial = viewModel.listaHistorial

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Historial Clínico", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
        Spacer(modifier = Modifier.height(16.dp))

        // --- DESPLEGABLE DE MASCOTAS ---
        ExposedDropdownMenuBox(expanded = expandedPets, onExpandedChange = { expandedPets = !expandedPets }) {
            OutlinedTextField(
                value = mascotaSeleccionada?.nombre ?: "Sin mascotas",
                onValueChange = {},
                readOnly = true,
                label = { Text("Ver historial de:") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPets) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8F9FA),
                    unfocusedContainerColor = Color(0xFFF8F9FA),
                    focusedBorderColor = PastelGreenPrimary,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            ExposedDropdownMenu(expanded = expandedPets, onDismissRequest = { expandedPets = false }, containerColor = Color.White) {
                misMascotas.forEach { pet ->
                    DropdownMenuItem(
                        text = { Text(pet.nombre) },
                        onClick = {
                            mascotaSeleccionada = pet
                            expandedPets = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (historial.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No hay registros médicos para esta mascota.", color = TextColorGray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(historial) { informe ->
                    HistorialCard(informe)
                }
            }
        }
    }
}

@Composable
fun HistorialCard(informe: HistorialClinico) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.MedicalServices, contentDescription = null, tint = PastelGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(informe.tipoEvento, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextColorDark)
                }
                Text(informe.fecha, color = TextColorGray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Atendido por: ${informe.veterinario ?: "Desconocido"}", fontSize = 12.sp, color = PastelGreenPrimary, fontWeight = FontWeight.SemiBold)

            if (!informe.descripcion.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(informe.descripcion, fontSize = 14.sp, color = TextColorDark)
            }

            // 📄 Botón de PDF dinámico: Solo aparece si hay una URL
            if (!informe.urlDocumento.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        // Abre el enlace del PDF en el navegador o visor del móvil
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(informe.urlDocumento))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)), // Un rojito PDF
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver Informe PDF", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}