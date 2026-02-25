package com.example.saluspet.features.calendar.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saluspet.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class Cita(
    val id: Long = System.currentTimeMillis(),
    val titulo: String,
    val fecha: String,
    val hora: String,
    val descripcion: String
)

@Composable
fun CalendarScreen(calendarViewModel: CalendarViewModel = viewModel ()) {
    // Ya no usamos "remember { mutableStateListOf }", usamos el del ViewModel
    val listaCitas = calendarViewModel.listaCitas

    var showDialog by remember { mutableStateOf(false) }
    var citaAEditar by remember { mutableStateOf<Cita?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { citaAEditar = null; showDialog = true },
                containerColor = PastelGreenPrimary
            ) { Icon(Icons.Filled.Add, contentDescription = "Añadir", tint = Color.White) }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Agenda de Citas", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextColorDark)

            Spacer(modifier = Modifier.height(16.dp))

            if (listaCitas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay citas programadas", color = TextColorGray)
                }
            }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(listaCitas) { cita ->
                    CitaCard(
                        cita = cita,
                        onDelete = { calendarViewModel.eliminarCita(cita) },
                        onEdit = { citaAEditar = cita; showDialog = true }
                    )
                }
            }
        }

        if (showDialog) {
            CitaDialog(
                citaExistente = citaAEditar,
                onDismiss = { showDialog = false },
                onSave = { nuevaCita ->
                    if (citaAEditar == null) {
                        calendarViewModel.agregarCita(nuevaCita)
                    } else {
                        calendarViewModel.editarCita(citaAEditar!!, nuevaCita)
                    }
                    showDialog = false
                }
            )
        }
    }
}

// --- FUNCIONES DE VALIDACIÓN PROFESIONAL ---

fun esFechaValida(fecha: String): Boolean {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.isLenient = false // ¡ESTA ES LA CLAVE! Evita que 31/02 sea válido
        sdf.parse(fecha)
        fecha.matches(Regex("""\d{2}/\d{2}/\d{4}"""))
    } catch (e: Exception) { false }
}

fun esHoraValida(hora: String): Boolean {
    // Regex para formato 24h (00:00 a 23:59)
    val regex = Regex("""^([01]\d|2[0-3]):([0-5]\d)$""")
    return hora.matches(regex)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitaDialog(citaExistente: Cita?, onDismiss: () -> Unit, onSave: (Cita) -> Unit) {
    var titulo by remember { mutableStateOf(citaExistente?.titulo ?: "") }
    var fecha by remember { mutableStateOf(citaExistente?.fecha ?: "") }
    var hora by remember { mutableStateOf(citaExistente?.hora ?: "") }
    var desc by remember { mutableStateOf(citaExistente?.descripcion ?: "") }

    // Estados de error visual
    val fechaError = fecha.isNotEmpty() && !esFechaValida(fecha)
    val horaError = hora.isNotEmpty() && !esHoraValida(hora)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (citaExistente == null) "Nueva Cita" else "Editar Cita") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fecha,
                    onValueChange = { if (it.length <= 10) fecha = it },
                    label = { Text("Fecha (DD/MM/AAAA)") },
                    isError = fechaError,
                    supportingText = { if (fechaError) Text("Fecha no válida (ej: 28/02/2026)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hora,
                    onValueChange = { if (it.length <= 5) hora = it },
                    label = { Text("Hora (HH:MM)") },
                    isError = horaError,
                    supportingText = { if (horaError) Text("Formato 24h incorrecto (ej: 14:30)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { if (it.length <= 500) desc = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                    Text("${desc.length}/500", fontSize = 10.sp, modifier = Modifier.align(Alignment.End))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(Cita(citaExistente?.id ?: System.currentTimeMillis(), titulo, fecha, hora, desc)) },
                enabled = titulo.isNotBlank() && esFechaValida(fecha) && esHoraValida(hora)
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun CitaCard(cita: Cita, onDelete: () -> Unit, onEdit: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(cita.titulo, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("${cita.fecha} - ${cita.hora}", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                }
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, null, tint = TextColorGray) }
                    IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, null, tint = Color.Red) }
                }
            }
            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Descripción:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextColorGray)
                Text(cita.descripcion, fontSize = 14.sp)
            }
        }
    }
}