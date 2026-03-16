package com.example.saluspet.features.pets.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.saluspet.features.calendar.presentation.CalendarViewModel
import com.example.saluspet.ui.theme.*

@Composable
fun PetHomeScreen(calendarViewModel: CalendarViewModel, petViewModel: PetViewModel) {
    val mascotas = petViewModel.listaMascotas
    val proximasCitas = calendarViewModel.listaCitas

    var showDialog by remember { mutableStateOf(false) }
    var mascotaAEditar by remember { mutableStateOf<Pet?>(null) }

    var petSeleccionado by remember { mutableStateOf<Pet?>(null) }

    // Estado para saber a qué mascota le vamos a recortar la foto
    var mascotaParaFoto by remember { mutableStateOf<Pet?>(null) }

    // --- NUEVO: LANZADOR DE RECORTE DE IMAGEN ESTILO WHATSAPP ---
    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                mascotaParaFoto?.let { pet ->
                    petViewModel.actualizarFotoPet(pet, uri.toString())
                    // Si estamos dentro de la vista de detalle, actualizamos los datos en vivo
                    if (petSeleccionado?.id == pet.id) {
                        petSeleccionado = petViewModel.listaMascotas.find { it.id == pet.id }
                    }
                }
            }
        }
    }

    // Función auxiliar para abrir la galería y luego el recortador
    fun lanzarRecorte(pet: Pet) {
        mascotaParaFoto = pet
        imageCropLauncher.launch(
            CropImageContractOptions(
                uri = null, // null abre la galería para elegir una foto nueva
                cropImageOptions = CropImageOptions(
                    imageSourceIncludeGallery = true,
                    imageSourceIncludeCamera = false, // Solo galería por simplicidad de permisos
                    fixAspectRatio = true, // Obliga a que el recorte sea un cuadrado (1:1)
                    aspectRatioX = 1,
                    aspectRatioY = 1
                )
            )
        )
    }

    // 1. VISTA DE DETALLE (CUANDO HAY UNA MASCOTA SELECCIONADA)
    if (petSeleccionado != null) {
        val petActualizado = mascotas.find { it.id == petSeleccionado!!.id }

        if (petActualizado == null) {
            petSeleccionado = null
        } else {
            PetDetailView(
                pet = petActualizado,
                onBack = { petSeleccionado = null },
                onEdit = {
                    mascotaAEditar = petActualizado
                    showDialog = true
                },
                onDelete = {
                    petViewModel.eliminarMascota(petActualizado)
                    petSeleccionado = null
                },
                onUpdatePhoto = { lanzarRecorte(petActualizado) } // Llamamos a nuestro recortador
            )
        }
    } else {
        // 2. VISTA DE INICIO (LISTA DE MASCOTAS)
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { mascotaAEditar = null; showDialog = true },
                    containerColor = PastelGreenPrimary,
                    contentColor = Color.White
                ) { Icon(Icons.Filled.Add, contentDescription = "Añadir Mascota") }
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Text("¡Hola de nuevo!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
                Text("Gestiona tus compañeros peludos", fontSize = 16.sp, color = TextColorGray)
                Spacer(modifier = Modifier.height(20.dp))

                if (proximasCitas.isNotEmpty()) {
                    val proxima = proximasCitas.first()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
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

                Text("Mis Mascotas", fontWeight = FontWeight.Bold, color = TextColorDark)
                Spacer(modifier = Modifier.height(12.dp))

                if (mascotas.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aún no has añadido ninguna mascota", color = TextColorGray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(mascotas) { mascota ->
                            MascotaCardGrande(
                                pet = mascota,
                                onClick = { petSeleccionado = mascota }
                            )
                        }
                    }
                }
            }
        }
    }

    // 3. DIÁLOGO PARA CREAR/EDITAR (Mantiene la información)
    if (showDialog) {
        MascotaDialog(
            petExistente = mascotaAEditar,
            onDismiss = { showDialog = false },
            onSave = { nuevaMascota ->
                if (mascotaAEditar == null) petViewModel.agregarMascota(nuevaMascota)
                else petViewModel.editarMascota(mascotaAEditar!!, nuevaMascota)
                showDialog = false
            }
        )
    }
}

@Composable
fun MascotaCardGrande(pet: Pet, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            if (pet.fotoUri != null) {
                AsyncImage(
                    model = pet.fotoUri,
                    contentDescription = "Foto de ${pet.nombre}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop // Ahora el crop se ajustará perfecto al cuadrado recortado
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(PastelBlueBackgroundLighter), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Pets, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.White)
                }
            }

            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                        startY = 200f
                    )
                )
            )

            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text(pet.nombre, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InfoChip(pet.especie)
                    Spacer(modifier = Modifier.width(8.dp))
                    InfoChip(pet.sexo)
                }
            }
        }
    }
}

@Composable
fun PetDetailView(pet: Pet, onBack: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit, onUpdatePhoto: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            if (pet.fotoUri != null) {
                AsyncImage(
                    model = pet.fotoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(PastelBlueBackgroundLighter), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Pets, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.White)
                }
            }

            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(16.dp).align(Alignment.TopStart).background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White) }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Text(pet.nombre, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DetalleItem("Especie", pet.especie)
                DetalleItem("Sexo", pet.sexo)
                DetalleItem("Edad", pet.edad)
                DetalleItem("Peso", "${pet.peso} kg")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onUpdatePhoto, // Lanza la función de recorte
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PastelGreenPrimary)
            ) {
                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cambiar Foto de Perfil")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Edit, contentDescription = null, tint = TextColorDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Editar Información", color = TextColorDark)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCCCC))
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Eliminar Mascota", color = Color.Red)
            }
        }
    }
}

@Composable
fun InfoChip(text: String) {
    Surface(color = Color.White.copy(alpha = 0.25f), shape = RoundedCornerShape(8.dp)) {
        Text(text = text, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DetalleItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = TextColorGray)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
    }
}

@Composable
fun MascotaDialog(petExistente: Pet?, onDismiss: () -> Unit, onSave: (Pet) -> Unit) {
    var nombre by remember { mutableStateOf(petExistente?.nombre ?: "") }
    var especie by remember { mutableStateOf(petExistente?.especie ?: "") }
    var sexo by remember { mutableStateOf(petExistente?.sexo ?: "") }
    var edad by remember { mutableStateOf(petExistente?.edad ?: "") }
    var peso by remember { mutableStateOf(petExistente?.peso ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (petExistente == null) "Nueva Mascota" else "Editar Mascota") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(value = especie, onValueChange = { especie = it }, label = { Text("Especie") })
                OutlinedTextField(value = sexo, onValueChange = { sexo = it }, label = { Text("Sexo") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = edad, onValueChange = { edad = it }, label = { Text("Edad") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = peso, onValueChange = { peso = it }, label = { Text("Peso (kg)") }, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(Pet(
                        id = petExistente?.id ?: System.currentTimeMillis(),
                        nombre = nombre, especie = especie, sexo = sexo, edad = edad, peso = peso,
                        fotoUri = petExistente?.fotoUri
                    ))
                },
                enabled = nombre.isNotBlank() && especie.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}