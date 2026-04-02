package com.example.saluspet.features.calendar.presentation

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
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

// --- MODELOS LOCALES PARA LAS PETICIONES ---
enum class EstadoPeticion { SOLICITADA, RESPUESTA_VET }

data class PeticionVet(
    val id: Long = System.currentTimeMillis(),
    val motivo: String,
    val fechaSolicitada: String,
    val horaSolicitada: String,
    var idMascota: Int,
    var nombreMascota: String,
    var fechaPropuestaVet: String? = null,
    var horaPropuestaVet: String? = null,
    var estado: EstadoPeticion = EstadoPeticion.SOLICITADA
)

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
    val listaCitas = calendarViewModel.listaCitas
    val listaPeticiones = remember { mutableStateListOf<PeticionVet>() }
    val misMascotas = petViewModel.listaMascotas

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
                if (listaPeticiones.isNotEmpty()) {
                    item {
                        Text("Trámites con la Clínica", fontWeight = FontWeight.Bold, color = PastelGreenPrimary, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(listaPeticiones) { peticion ->
                        PeticionCard(
                            peticion = peticion,
                            onAccept = {
                                val nuevaCitaVet = Cita(
                                    idCita = 0,
                                    idMascota = peticion.idMascota,
                                    nombreMascota = peticion.nombreMascota, // ⬅️ Enviamos el nombre
                                    tipo = "Veterinaria",
                                    titulo = peticion.motivo,
                                    fecha = convertirFechaBackend(peticion.fechaPropuestaVet ?: peticion.fechaSolicitada),
                                    hora = peticion.horaPropuestaVet ?: peticion.horaSolicitada,
                                    descripcion = "Cita veterinaria confirmada"
                                )
                                calendarViewModel.crearCitaEnServidor(nuevaCitaVet)
                                listaPeticiones.remove(peticion)
                                calendarViewModel.cargarAgendaGlobal(context)
                            },
                            onDeny = { listaPeticiones.remove(peticion) },
                            onSimularRespuestaVet = {
                                val index = listaPeticiones.indexOf(peticion)
                                if (index != -1) {
                                    listaPeticiones[index] = peticion.copy(
                                        estado = EstadoPeticion.RESPUESTA_VET,
                                        fechaPropuestaVet = "25/12/2026",
                                        horaPropuestaVet = "11:30"
                                    )
                                }
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Mi Agenda Confirmada", fontWeight = FontWeight.Bold, color = TextColorDark, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (listaCitas.isEmpty()) {
                        Text("No hay citas programadas", color = TextColorGray)
                    }
                }

                items(listaCitas) { cita ->
                    CitaCard(
                        cita = cita,
                        listaMascotas = misMascotas, // ⬅️ Le pasamos las mascotas para que busque el nombre
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
                onSend = { nuevaPeticion ->
                    listaPeticiones.add(nuevaPeticion)
                    showDialogSolicitud = false
                }
            )
        }
    }
}

@Composable
fun CitaCard(cita: Cita, listaMascotas: List<Pet>, onDelete: () -> Unit, onEdit: () -> Unit) {
    val isVeterinaria = cita.tipo == "Veterinaria"

    // 🔍 BUSCADOR INTELIGENTE: Si el backend no devuelve el nombre, lo buscamos en tu lista local por ID
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
                    // 🔥 Aquí pegamos el nombre real de forma automática
                    Text(text = "$nombreReal - ${cita.titulo}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.DarkGray)

                    if (isVeterinaria) {
                        Text(text = cita.estado ?: "Pendiente", fontWeight = FontWeight.Bold, color = if (cita.estado == "Pendiente") Color(0xFFFFA000) else Color(0xFF388E3C))
                    } else {
                        Text(text = "Recordatorio Personal", fontWeight = FontWeight.Medium, color = PastelGreenPrimary, fontSize = 14.sp)
                    }
                }
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, null, tint = TextColorGray) }
                    IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, null, tint = Color.Red) }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Icon(Icons.Filled.CalendarToday, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = cita.fecha, color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Filled.AccessTime, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
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

@Composable
fun PeticionCard(peticion: PeticionVet, onAccept: () -> Unit, onDeny: () -> Unit, onSimularRespuestaVet: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().pointerInput(Unit) { detectTapGestures(onLongPress = { onSimularRespuestaVet() }) },
        colors = CardDefaults.cardColors(containerColor = if (peticion.estado == EstadoPeticion.SOLICITADA) Color(0xFFFFF9C4) else PastelBlueBackgroundLighter),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (peticion.estado == EstadoPeticion.SOLICITADA) Icons.Filled.PendingActions else Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = if (peticion.estado == EstadoPeticion.SOLICITADA) Color(0xFFFBC02D) else PastelGreenPrimary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("${peticion.nombreMascota}: ${peticion.motivo}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextColorDark)
                    if (peticion.estado == EstadoPeticion.SOLICITADA) {
                        Text("Solicitada: ${peticion.fechaSolicitada} a las ${peticion.horaSolicitada}", color = TextColorGray, fontSize = 14.sp)
                        Text("Esperando confirmación...", color = Color(0xFFFBC02D), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    } else {
                        Text("El veterinario propone: ${peticion.fechaPropuestaVet} a las ${peticion.horaPropuestaVet}", color = PastelGreenPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (peticion.estado == EstadoPeticion.RESPUESTA_VET) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDeny) { Text("Rechazar", color = Color.Red) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onAccept, colors = ButtonDefaults.buttonColors(containerColor = PastelGreenPrimary)) {
                        Text("Aceptar y Agendar", color = TextColorDark)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarVetDialog(listaMascotas: List<Pet>, onDismiss: () -> Unit, onSend: (PeticionVet) -> Unit) {
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
                                onSend(PeticionVet(motivo = motivo, fechaSolicitada = fecha, horaSolicitada = hora, idMascota = pet.id.toInt(), nombreMascota = pet.nombre))
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
                                    nombreMascota = pet.nombre, // ⬅️ IMPORTANTE: Añadimos el nombre aquí para que se vea rápido
                                    tipo = citaExistente?.tipo ?: "Personal",
                                    titulo = titulo,
                                    fecha = convertirFechaBackend(fecha),
                                    hora = hora,
                                    descripcion = desc.ifBlank { "Sin notas" }
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