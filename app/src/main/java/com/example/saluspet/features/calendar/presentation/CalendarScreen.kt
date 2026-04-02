package com.example.saluspet.features.calendar.presentation

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.saluspet.features.calendar.data.Cita
import com.example.saluspet.features.pets.presentation.Pet
import com.example.saluspet.features.pets.presentation.PetViewModel
import com.example.saluspet.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.outlined.*

fun convertirFechaBackend(fecha: String): String {
    return try {
        val inFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        outFormat.format(inFormat.parse(fecha)!!)
    } catch (e: Exception) {
        fecha
    }
}

// --- PANTALLA PRINCIPAL ---
@Composable
fun CalendarScreen(
    calendarViewModel: CalendarViewModel,
    petViewModel: PetViewModel
) {
    val context = LocalContext.current
    val listaCitasTotales = calendarViewModel.listaCitas
    val misMascotas = petViewModel.listaMascotas

    // 🚀 FILTROS INTELIGENTES: Separamos la lista de la base de datos en dos
    val peticionesPendientes = listaCitasTotales.filter { it.tipo == "Veterinaria" && it.estado == "Pendiente" }
    val agendaConfirmada = listaCitasTotales.filter { it.tipo == "Personal" || it.estado == "Confirmada" || it.estado.isNullOrBlank() }

    var showDialogInterna by remember { mutableStateOf(false) }
    var citaAEditar by remember { mutableStateOf<Cita?>(null) }
    var showDialogSolicitud by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        calendarViewModel.cargarAgendaGlobal(context)
    }

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { citaAEditar = null; showDialogInterna = true },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PastelGreenPrimary),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agenda Interna", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showDialogSolicitud = true },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PastelGreenPrimary),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Solicitar Cita", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Gestión de Citas", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // --- SECCIÓN 1: TRÁMITES CON LA CLÍNICA (AMARILLOS) ---
                if (peticionesPendientes.isNotEmpty()) {
                    item {
                        Text("Trámites con la Clínica", fontWeight = FontWeight.Bold, color = PastelGreenPrimary, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(peticionesPendientes) { peticion ->
                        PeticionCard(
                            cita = peticion,
                            onDelete = { calendarViewModel.eliminarCita(peticion) } // Si te arrepientes, la borras de MySQL
                        )
                    }
                }

                // --- SECCIÓN 2: MI AGENDA CONFIRMADA (VERDES/AZULES) ---
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Mi Agenda Confirmada", fontWeight = FontWeight.Bold, color = TextColorDark, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (agendaConfirmada.isEmpty()) {
                        Text("No hay citas programadas", color = TextColorGray)
                    }
                }

                items(agendaConfirmada) { cita ->
                    CitaCard(
                        cita = cita,
                        listaMascotas = misMascotas,
                        onDelete = { calendarViewModel.eliminarCita(cita) },
                        onEdit = { citaAEditar = cita; showDialogInterna = true }
                    )
                }
            }
        }

        if (showDialogInterna) {
            CitaDialogPremium(
                citaExistente = citaAEditar,
                listaMascotas = misMascotas,
                onDismiss = { showDialogInterna = false },
                onSave = { nuevaCita ->
                    if (citaAEditar == null) {
                        calendarViewModel.crearCitaEnServidor(nuevaCita)
                    } else {
                        calendarViewModel.editarCita(citaAEditar!!, nuevaCita)
                    }
                    showDialogInterna = false
                }
            )
        }

        if (showDialogSolicitud) {
            SolicitarVetDialog(
                listaMascotas = misMascotas,
                onDismiss = { showDialogSolicitud = false },
                onSend = { nuevaCitaVeterinaria ->
                    // 🚀 La enviamos directamente al servidor real
                    calendarViewModel.crearCitaEnServidor(nuevaCitaVeterinaria)
                    showDialogSolicitud = false
                }
            )
        }
    }
}

@Composable
fun CitaCard(cita: Cita, listaMascotas: List<Pet>, onDelete: () -> Unit, onEdit: () -> Unit) {
    val isVeterinaria = cita.tipo == "Veterinaria"

    val mascotaEncontrada = listaMascotas.find { it.id.toInt() == cita.idMascota }
    val nombreReal = cita.nombreMascota ?: mascotaEncontrada?.nombre ?: "Mascota"

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isVeterinaria) Color(0xFFE3F2FD) else Color(0xFFF1F8E9)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "$nombreReal - ${cita.titulo}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.DarkGray)

                    if (isVeterinaria) {
                        Text(text = cita.estado ?: "Confirmada", fontWeight = FontWeight.Bold, color = Color(0xFF388E3C))
                    } else {
                        Text(text = "Recordatorio Personal", fontWeight = FontWeight.Medium, color = PastelGreenPrimary, fontSize = 14.sp)
                    }
                }

                // 🎨 AQUÍ ESTÁ EL CAMBIO VISUAL DE LOS ICONOS 🎨
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp) // Reducimos el tamaño del área pulsable
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit, // Usamos el icono hueco (Outlined)
                            contentDescription = "Editar",
                            tint = Color.DarkGray.copy(alpha = 0.7f), // Un gris oscuro sutil
                            modifier = Modifier.size(20.dp) // Hacemos el dibujito más pequeño
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete, // Usamos la papelera hueca
                            contentDescription = "Eliminar",
                            tint = Color.DarkGray.copy(alpha = 0.7f), // Ya no es rojo chillón, es elegante
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = cita.fecha, color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = cita.hora, color = Color.Gray, fontSize = 14.sp)
            }
            if (!cita.descripcion.isNullOrBlank() && cita.descripcion != "Sin notas") {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.LightGray.copy(alpha = 0.5f))
                Text(text = cita.descripcion, fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

// 🟡 NUEVA TARJETA DE PETICIÓN (AHORA LEE UNA CITA REAL)
@Composable
fun PeticionCard(cita: Cita, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)), // Amarillo suave
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icono de reloj/espera en estilo Outlined
                    Icon(
                        imageVector = Icons.Outlined.PendingActions,
                        contentDescription = null,
                        tint = Color(0xFFFBC02D),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${cita.nombreMascota ?: "Mascota"}: ${cita.titulo}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextColorDark
                        )
                        Text(
                            text = "Solicitada: ${cita.fecha} a las ${cita.hora}",
                            color = TextColorGray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Esperando confirmación...",
                            color = Color(0xFFFBC02D),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Botón de eliminar pequeño y sutil (estilo Outlined)
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Cancelar",
                        tint = Color.DarkGray.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarVetDialog(listaMascotas: List<Pet>, onDismiss: () -> Unit, onSend: (Cita) -> Unit) {
    var motivo by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }

    var mascotaSeleccionada by remember { mutableStateOf(listaMascotas.firstOrNull()) }
    var expandedPets by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        fecha = sdf.format(Date(millis))
                    }
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    hora = String.format("%02d:%02d:00", timePickerState.hour, timePickerState.minute)
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") } },
            text = { TimePicker(state = timePickerState) }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = PastelBlueBackgroundLighter), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.size(64.dp)) {
                    Icon(Icons.Filled.Send, null, tint = PastelGreenPrimary, modifier = Modifier.padding(16.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Solicitar Cita al Veterinario", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
                Spacer(modifier = Modifier.height(24.dp))

                ExposedDropdownMenuBox(expanded = expandedPets, onExpandedChange = { expandedPets = !expandedPets }) {
                    OutlinedTextField(
                        value = mascotaSeleccionada?.nombre ?: "Selecciona mascota", onValueChange = {}, readOnly = true, label = { Text("Mascota") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPets) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = PastelGreenPrimary, unfocusedBorderColor = Color.Transparent)
                    )
                    ExposedDropdownMenu(expanded = expandedPets, onDismissRequest = { expandedPets = false }, containerColor = Color.White) {
                        listaMascotas.forEach { pet ->
                            DropdownMenuItem(text = { Text(pet.nombre) }, onClick = { mascotaSeleccionada = pet; expandedPets = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = motivo, onValueChange = { motivo = it }, label = { Text("Motivo de la cita") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = PastelGreenPrimary, unfocusedBorderColor = Color.Transparent))
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = fecha, onValueChange = {}, readOnly = true, label = { Text("Fecha") }, modifier = Modifier.weight(1f).clickable { showDatePicker = true }, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = TextColorDark, disabledContainerColor = Color.White, disabledBorderColor = Color.Transparent))
                    OutlinedTextField(value = hora, onValueChange = {}, readOnly = true, label = { Text("Hora") }, modifier = Modifier.weight(1f).clickable { showTimePicker = true }, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = TextColorDark, disabledContainerColor = Color.White, disabledBorderColor = Color.Transparent))
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = TextColorGray, fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            mascotaSeleccionada?.let { pet ->
                                // 🌟 AQUÍ OCURRE LA MAGIA: Creamos la cita como "Pendiente" y viaja a MySQL
                                val nuevaCitaVet = Cita(
                                    idCita = 0,
                                    idMascota = pet.id.toInt(),
                                    nombreMascota = pet.nombre,
                                    tipo = "Veterinaria",
                                    titulo = motivo,
                                    fecha = convertirFechaBackend(fecha),
                                    hora = hora,
                                    descripcion = "Solicitud enviada a la clínica. Esperando respuesta.",
                                    estado = "Pendiente" // ⬅️ CLAVE PARA QUE SALGA AMARILLA ARRIBA
                                )
                                onSend(nuevaCitaVet)
                            }
                        },
                        enabled = motivo.isNotBlank() && fecha.isNotBlank() && hora.isNotBlank() && mascotaSeleccionada != null,
                        colors = ButtonDefaults.buttonColors(containerColor = PastelGreenPrimary), shape = RoundedCornerShape(16.dp)
                    ) { Text("Enviar Solicitud", color = TextColorDark, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitaDialogPremium(citaExistente: Cita?, listaMascotas: List<Pet>, onDismiss: () -> Unit, onSave: (Cita) -> Unit) {
    val context = LocalContext.current
    var titulo by remember { mutableStateOf(citaExistente?.titulo ?: "") }
    var fecha by remember { mutableStateOf(citaExistente?.fecha ?: "") }
    var hora by remember { mutableStateOf(citaExistente?.hora ?: "") }
    var desc by remember { mutableStateOf(citaExistente?.descripcion ?: "") }

    var mascotaSeleccionada by remember { mutableStateOf(listaMascotas.find { it.id.toInt() == citaExistente?.idMascota } ?: listaMascotas.firstOrNull()) }
    var expandedPets by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        fecha = sdf.format(Date(millis))
                    }
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    hora = String.format("%02d:%02d:00", timePickerState.hour, timePickerState.minute)
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") } },
            text = { TimePicker(state = timePickerState) }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = PastelBlueBackgroundLighter), elevation = CardDefaults.cardElevation(8.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.size(64.dp)) {
                    Icon(Icons.Filled.CalendarMonth, null, tint = PastelGreenPrimary, modifier = Modifier.padding(16.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(if (citaExistente == null) "Nueva Cita Interna" else "Editar Cita", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
                Spacer(modifier = Modifier.height(24.dp))

                ExposedDropdownMenuBox(expanded = expandedPets, onExpandedChange = { expandedPets = !expandedPets }) {
                    OutlinedTextField(
                        value = mascotaSeleccionada?.nombre ?: "Selecciona mascota", onValueChange = {}, readOnly = true, label = { Text("Asignar a Mascota") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPets) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = PastelGreenPrimary, unfocusedBorderColor = Color.Transparent)
                    )
                    ExposedDropdownMenu(expanded = expandedPets, onDismissRequest = { expandedPets = false }, containerColor = Color.White) {
                        listaMascotas.forEach { pet ->
                            DropdownMenuItem(text = { Text(pet.nombre) }, onClick = { mascotaSeleccionada = pet; expandedPets = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Motivo / Título") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = PastelGreenPrimary, unfocusedBorderColor = Color.Transparent))
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = fecha, onValueChange = {}, readOnly = true, label = { Text("Fecha") }, modifier = Modifier.weight(1f).clickable { showDatePicker = true }, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = TextColorDark, disabledContainerColor = Color.White, disabledBorderColor = Color.Transparent))
                    OutlinedTextField(value = hora, onValueChange = {}, readOnly = true, label = { Text("Hora") }, modifier = Modifier.weight(1f).clickable { showTimePicker = true }, enabled = false, colors = OutlinedTextFieldDefaults.colors(disabledTextColor = TextColorDark, disabledContainerColor = Color.White, disabledBorderColor = Color.Transparent))
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = desc, onValueChange = { if (it.length <= 500) desc = it }, label = { Text("Notas o Descripción") }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedBorderColor = PastelGreenPrimary, unfocusedBorderColor = Color.Transparent))

                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = TextColorGray, fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            mascotaSeleccionada?.let { pet ->
                                val citaGuardar = Cita(
                                    idCita = citaExistente?.idCita ?: 0,
                                    idMascota = pet.id.toInt(),
                                    nombreMascota = pet.nombre,
                                    tipo = citaExistente?.tipo ?: "Personal",
                                    titulo = titulo,
                                    fecha = convertirFechaBackend(fecha),
                                    hora = hora,
                                    descripcion = desc.ifBlank { "Sin notas" },
                                    estado = citaExistente?.estado
                                )
                                onSave(citaGuardar)

                                if (citaExistente == null) {
                                    val intent = Intent(Intent.ACTION_INSERT).apply {
                                        data = CalendarContract.Events.CONTENT_URI
                                        putExtra(CalendarContract.Events.TITLE, "SalusPet ($pet.nombre): $titulo")
                                        putExtra(CalendarContract.Events.DESCRIPTION, desc)
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        },
                        enabled = titulo.isNotBlank() && fecha.isNotBlank() && hora.isNotBlank() && mascotaSeleccionada != null,
                        colors = ButtonDefaults.buttonColors(containerColor = PastelGreenPrimary), shape = RoundedCornerShape(16.dp)
                    ) { Text(if (citaExistente == null) "Guardar" else "Actualizar", color = TextColorDark, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}