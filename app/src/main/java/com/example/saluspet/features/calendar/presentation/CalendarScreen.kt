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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Send
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
import com.example.saluspet.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// --- MODELOS DE DATOS ---
data class Cita(
    val id: Long = System.currentTimeMillis(),
    val titulo: String,
    val fecha: String,
    val hora: String,
    val descripcion: String
)

enum class EstadoPeticion { SOLICITADA, RESPUESTA_VET }

data class PeticionVet(
    val id: Long = System.currentTimeMillis(),
    val motivo: String,
    val fechaSolicitada: String,
    val horaSolicitada: String, // ¡NUEVO CAMPO HORA!
    var fechaPropuestaVet: String? = null,
    var horaPropuestaVet: String? = null, // ¡NUEVO CAMPO HORA RESPUESTA!
    var estado: EstadoPeticion = EstadoPeticion.SOLICITADA
)

// --- PANTALLA PRINCIPAL ---
@Composable
fun CalendarScreen(calendarViewModel: CalendarViewModel) {
    val listaCitas = calendarViewModel.listaCitas
    val listaPeticiones = remember { mutableStateListOf<PeticionVet>() }

    var showDialogInterna by remember { mutableStateOf(false) }
    var citaAEditar by remember { mutableStateOf<Cita?>(null) }
    var showDialogSolicitud by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            // BOTONES ESTILO "FAB" (Idénticos al Home)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 8.dp), // Un poco de aire por abajo
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { citaAEditar = null; showDialogInterna = true },
                    modifier = Modifier.weight(1f).height(56.dp), // Altura estándar de FAB
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

                // 1. SECCIÓN DE PETICIONES AL VETERINARIO
                if (listaPeticiones.isNotEmpty()) {
                    item {
                        Text("Trámites con la Clínica", fontWeight = FontWeight.Bold, color = PastelGreenPrimary, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(listaPeticiones) { peticion ->
                        PeticionCard(
                            peticion = peticion,
                            onAccept = {
                                calendarViewModel.agregarCita(
                                    Cita(
                                        titulo = peticion.motivo,
                                        fecha = peticion.fechaPropuestaVet ?: peticion.fechaSolicitada,
                                        hora = peticion.horaPropuestaVet ?: peticion.horaSolicitada,
                                        descripcion = "Cita confirmada por el veterinario"
                                    )
                                )
                                listaPeticiones.remove(peticion)
                            },
                            onDeny = { listaPeticiones.remove(peticion) },
                            onSimularRespuestaVet = {
                                val index = listaPeticiones.indexOf(peticion)
                                if (index != -1) {
                                    listaPeticiones[index] = peticion.copy(
                                        estado = EstadoPeticion.RESPUESTA_VET,
                                        fechaPropuestaVet = "25/12/2026",
                                        horaPropuestaVet = "11:30" // El vet propone también la hora
                                    )
                                }
                            }
                        )
                    }
                }

                // 2. SECCIÓN DE AGENDA INTERNA
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Mi Agenda Confirmada", fontWeight = FontWeight.Bold, color = TextColorDark, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (listaCitas.isEmpty()) {
                        Text("No hay citas programadas", color = TextColorGray)
                    }
                }
                items(listaCitas) { cita ->
                    CitaCardExpandible(
                        cita = cita,
                        onDelete = { calendarViewModel.eliminarCita(cita) },
                        onEdit = { citaAEditar = cita; showDialogInterna = true }
                    )
                }
            }
        }

        // DIÁLOGOS
        if (showDialogInterna) {
            CitaDialogPremium(
                citaExistente = citaAEditar,
                onDismiss = { showDialogInterna = false },
                onSave = { nuevaCita ->
                    if (citaAEditar == null) calendarViewModel.agregarCita(nuevaCita)
                    else calendarViewModel.editarCita(citaAEditar!!, nuevaCita)
                    showDialogInterna = false
                }
            )
        }

        if (showDialogSolicitud) {
            SolicitarVetDialog(
                onDismiss = { showDialogSolicitud = false },
                onSend = { nuevaPeticion ->
                    listaPeticiones.add(nuevaPeticion)
                    showDialogSolicitud = false
                }
            )
        }
    }
}

// --- TARJETA DE PETICIONES AL VETERINARIO ---
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
                    Text(peticion.motivo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextColorDark)
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
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

// --- FORMULARIO DE SOLICITUD AL VET (CON HORA Y FECHA) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarVetDialog(onDismiss: () -> Unit, onSend: (PeticionVet) -> Unit) {
    var motivo by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }

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
                    hora = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") } },
            text = { TimePicker(state = timePickerState) }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = PastelBlueBackgroundLighter),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.size(64.dp)) {
                    Icon(Icons.Filled.Send, contentDescription = null, tint = PastelGreenPrimary, modifier = Modifier.padding(16.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Solicitar Cita al Veterinario", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = motivo, onValueChange = { motivo = it }, label = { Text("Motivo de la cita") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color(0xFFF8F9FA), unfocusedContainerColor = Color(0xFFF8F9FA), focusedBorderColor = PastelGreenPrimary, unfocusedBorderColor = Color.Transparent)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Selector de Fecha y Hora
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = fecha, onValueChange = {}, readOnly = true, label = { Text("Fecha") },
                        modifier = Modifier.weight(1f).clickable { showDatePicker = true }, enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(disabledTextColor = TextColorDark, disabledBorderColor = Color.Transparent, disabledContainerColor = Color(0xFFF8F9FA), disabledLabelColor = TextColorGray)
                    )
                    OutlinedTextField(
                        value = hora, onValueChange = {}, readOnly = true, label = { Text("Hora") },
                        modifier = Modifier.weight(1f).clickable { showTimePicker = true }, enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(disabledTextColor = TextColorDark, disabledBorderColor = Color.Transparent, disabledContainerColor = Color(0xFFF8F9FA), disabledLabelColor = TextColorGray)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = TextColorGray, fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSend(PeticionVet(motivo = motivo, fechaSolicitada = fecha, horaSolicitada = hora)) },
                        enabled = motivo.isNotBlank() && fecha.isNotBlank() && hora.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PastelGreenPrimary),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Enviar Solicitud", color = TextColorDark, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

// --- AGENDA INTERNA: CITA CARD EXPANDIBLE Y DIÁLOGO ---
@Composable
fun CitaCardExpandible(cita: Cita, onDelete: () -> Unit, onEdit: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = PastelBlueBackgroundLighter, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = PastelGreenPrimary, modifier = Modifier.padding(12.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(cita.titulo, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextColorDark)
                        Text("${cita.fecha} a las ${cita.hora}", color = PastelGreenPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, null, tint = TextColorGray) }
                    IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, null, tint = Color.Red) }
                }
            }
            if (expanded && cita.descripcion.isNotBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))
                Text("Detalles / Notas:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextColorGray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(cita.descripcion, fontSize = 14.sp, color = TextColorDark)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitaDialogPremium(citaExistente: Cita?, onDismiss: () -> Unit, onSave: (Cita) -> Unit) {
    val context = LocalContext.current
    var titulo by remember { mutableStateOf(citaExistente?.titulo ?: "") }
    var fecha by remember { mutableStateOf(citaExistente?.fecha ?: "") }
    var hora by remember { mutableStateOf(citaExistente?.hora ?: "") }
    var desc by remember { mutableStateOf(citaExistente?.descripcion ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()
    val fieldBackgroundColor = Color(0xFFF8F9FA)

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
                    hora = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") } },
            text = { TimePicker(state = timePickerState) }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = PastelBlueBackgroundLighter),
            elevation = CardDefaults.cardElevation(8.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.size(64.dp)) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = PastelGreenPrimary, modifier = Modifier.padding(16.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(if (citaExistente == null) "Nueva Cita Interna" else "Editar Cita", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = titulo, onValueChange = { titulo = it }, label = { Text("Motivo") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedBorderColor = PastelGreenPrimary, unfocusedBorderColor = Color.Transparent)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = fecha, onValueChange = {}, readOnly = true, label = { Text("Fecha") },
                        modifier = Modifier.weight(1f).clickable { showDatePicker = true }, enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(disabledTextColor = TextColorDark, disabledContainerColor = fieldBackgroundColor, disabledBorderColor = Color.Transparent, disabledLabelColor = TextColorGray)
                    )
                    OutlinedTextField(
                        value = hora, onValueChange = {}, readOnly = true, label = { Text("Hora") },
                        modifier = Modifier.weight(1f).clickable { showTimePicker = true }, enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(disabledTextColor = TextColorDark, disabledContainerColor = fieldBackgroundColor, disabledBorderColor = Color.Transparent, disabledLabelColor = TextColorGray)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = desc, onValueChange = { if (it.length <= 500) desc = it }, label = { Text("Notas o Descripción") }, modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedBorderColor = PastelGreenPrimary, unfocusedBorderColor = Color.Transparent)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = TextColorGray, fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val citaGuardar = Cita(id = citaExistente?.id ?: System.currentTimeMillis(), titulo = titulo, fecha = fecha, hora = hora, descripcion = desc)
                            onSave(citaGuardar)

                            if (citaExistente == null) {
                                val intent = Intent(Intent.ACTION_INSERT).apply {
                                    data = CalendarContract.Events.CONTENT_URI
                                    putExtra(CalendarContract.Events.TITLE, "SalusPet: $titulo")
                                    putExtra(CalendarContract.Events.DESCRIPTION, desc)
                                }
                                context.startActivity(intent)
                            }
                        },
                        enabled = titulo.isNotBlank() && fecha.isNotBlank() && hora.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = PastelGreenPrimary), shape = RoundedCornerShape(16.dp)
                    ) { Text(if (citaExistente == null) "Guardar" else "Actualizar", color = TextColorDark, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}