package com.example.saluspet.features.calendar.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.saluspet.features.calendar.data.Veterinario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitarCitaScreen(
    viewModel: SolicitarCitaViewModel = viewModel(),
    onVolver: () -> Unit
) {
    // Pedimos a la BD los veterinarios en cuanto se abre el pop-up
    LaunchedEffect(Unit) {
        viewModel.cargarVeterinariosDesdeBD()
    }

    // Estados de Mascota
    var mascotaSeleccionada by remember { mutableStateOf("") }
    var expandidoMascota by remember { mutableStateOf(false) }

    // Estados del Veterinario (guardamos el objeto entero para poder sacar su ID luego)
    var veterinarioSeleccionado by remember { mutableStateOf<Veterinario?>(null) }
    var expandidoVeterinario by remember { mutableStateOf(false) }

    // Estados de los campos de texto
    var motivo by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }

    // Datos
    val mascotas = listOf("Ryu", "Kira") // (Esto en el futuro puedes traerlo de la BD también)
    val veterinariosBD = viewModel.listaVeterinarios

    Dialog(onDismissRequest = onVolver) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF8)), // Fondo clarito de tu app
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Solicitar Cita al\nVeterinario",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3748),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 1. Desplegable Mascota
                ExposedDropdownMenuBox(
                    expanded = expandidoMascota,
                    onExpandedChange = { expandidoMascota = !expandidoMascota }
                ) {
                    OutlinedTextField(
                        value = mascotaSeleccionada,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mascota") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoMascota) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandidoMascota,
                        onDismissRequest = { expandidoMascota = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        mascotas.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m) },
                                onClick = {
                                    mascotaSeleccionada = m
                                    expandidoMascota = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Desplegable Veterinario (Desde Base de Datos NestJS)
                ExposedDropdownMenuBox(
                    expanded = expandidoVeterinario,
                    onExpandedChange = { expandidoVeterinario = !expandidoVeterinario }
                ) {
                    OutlinedTextField(
                        value = veterinarioSeleccionado?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Veterinario / Clínica") },
                        placeholder = { if (viewModel.estaCargandoVeterinarios) Text("Cargando BD...") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoVeterinario) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandidoVeterinario,
                        onDismissRequest = { expandidoVeterinario = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        if (veterinariosBD.isEmpty() && !viewModel.estaCargandoVeterinarios) {
                            DropdownMenuItem(
                                text = { Text("No hay veterinarios") },
                                onClick = { expandidoVeterinario = false }
                            )
                        } else {
                            veterinariosBD.forEach { vet ->
                                DropdownMenuItem(
                                    // Muestra el nombre y la clínica, ej: "Dr. García - SalusPet"
                                    text = { Text(vet.nombre) },
                                    onClick = {
                                        veterinarioSeleccionado = vet
                                        expandidoVeterinario = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Campo Motivo
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    label = { Text("Motivo de la cita") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Fecha y Hora
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = fecha, onValueChange = { fecha = it },
                        label = { Text("Fecha") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = hora, onValueChange = { hora = it },
                        label = { Text("Hora") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 5. Botones Cancelar y Enviar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onVolver) {
                        Text("Cancelar", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (mascotaSeleccionada.isNotEmpty() && veterinarioSeleccionado != null) {
                                // Pasamos el ID real del veterinario sacado de la base de datos
                                viewModel.enviarSolicitud(mascotaSeleccionada, veterinarioSeleccionado!!.id, motivo, fecha, hora)
                                onVolver()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD8EBE4)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Enviar Solicitud", color = Color(0xFF2D3748), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}