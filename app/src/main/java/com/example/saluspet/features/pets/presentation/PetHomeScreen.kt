package com.example.saluspet.features.pets.presentation

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.* import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.saluspet.R
import com.example.saluspet.features.calendar.presentation.CalendarViewModel
import com.example.saluspet.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PetHomeScreen(calendarViewModel: CalendarViewModel, petViewModel: PetViewModel) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        petViewModel.cargarMascotas(context)
    }

    val mascotas = petViewModel.listaMascotas
    var showDialog by remember { mutableStateOf(false) }
    var mascotaAEditar by remember { mutableStateOf<Pet?>(null) }
    var petSeleccionado by remember { mutableStateOf<Pet?>(null) }
    var mascotaParaFoto by remember { mutableStateOf<Pet?>(null) }

    fun uriToBase64(context: android.content.Context, uriString: String): String? {
        return try {
            val uri = android.net.Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) Base64.encodeToString(bytes, Base64.NO_WRAP) else null
        } catch (e: Exception) { null }
    }

    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                val fotoBase64 = uriToBase64(context, uri.toString())
                if (fotoBase64 != null) {
                    mascotaParaFoto?.let { pet ->
                        petViewModel.actualizarFotoPet(context, pet, fotoBase64)
                    }
                }
            }
        }
    }

    fun lanzarRecorte(pet: Pet) {
        mascotaParaFoto = pet
        imageCropLauncher.launch(
            CropImageContractOptions(
                uri = null,
                cropImageOptions = CropImageOptions(
                    imageSourceIncludeGallery = true,
                    fixAspectRatio = true,
                    aspectRatioX = 1,
                    aspectRatioY = 1
                )
            )
        )
    }

    if (petSeleccionado != null) {
        val petActualizado = mascotas.find { it.id == petSeleccionado!!.id }
        if (petActualizado == null) {
            petSeleccionado = null
        } else {
            PetDetailView(
                pet = petActualizado,
                onBack = { petSeleccionado = null },
                onEdit = { mascotaAEditar = petActualizado; showDialog = true },
                onDelete = {
                    petViewModel.eliminarMascota(petActualizado)
                    petSeleccionado = null
                },
                onUpdatePhoto = { lanzarRecorte(petActualizado) }
            )
        }
    } else {
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
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Image(painter = painterResource(id = R.drawable.logo_saluspet1), contentDescription = null, modifier = Modifier.height(105.dp))
                }
                Text("Mis Mascotas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
                Spacer(modifier = Modifier.height(8.dp))

                if (mascotas.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aún no has añadido ninguna mascota", color = TextColorGray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(mascotas) { mascota ->
                            MascotaCardGrande(pet = mascota, onClick = { petSeleccionado = mascota })
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        MascotaDialogPremium(
            petExistente = mascotaAEditar,
            onDismiss = { showDialog = false },
            onSave = { nuevaMascota ->

                val pesoLimpio = nuevaMascota.peso.replace(",", ".").toDoubleOrNull() ?: 1.0

                val fechaParaBackend = try {
                    val inFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val outFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    if (nuevaMascota.edad.isNotBlank() && !nuevaMascota.edad.contains("-")) {
                        outFormat.format(inFormat.parse(nuevaMascota.edad)!!)
                    } else {
                        nuevaMascota.edad
                    }
                } catch (e: Exception) { "2026-01-01" }

                if (mascotaAEditar == null) {
                    // CREAR
                    petViewModel.crearMascotaEnServidor(
                        context = context,
                        nombre = nuevaMascota.nombre,
                        especie = nuevaMascota.especie,
                        peso = pesoLimpio,
                        fechaNac = fechaParaBackend,
                        genero = nuevaMascota.sexo,
                        fotoBase64 = nuevaMascota.fotoBase64
                    )
                } else {
                    // EDITAR
                    val mascotaActualizadaYFormateada = nuevaMascota.copy(
                        edad = fechaParaBackend,
                        peso = pesoLimpio.toString()
                    )
                    petViewModel.editarMascota(context, mascotaAEditar!!, mascotaActualizadaYFormateada)

                    if (petSeleccionado?.id == mascotaAEditar!!.id) {
                        petSeleccionado = mascotaActualizadaYFormateada
                    }
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun ImagenDecodificada(fotoString: String?, modifier: Modifier = Modifier, fallbackSize: Int = 64) {
    if (fotoString.isNullOrBlank()) {
        Box(modifier = modifier.background(PastelBlueBackgroundLighter), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Pets, contentDescription = null, modifier = Modifier.size(fallbackSize.dp), tint = Color.White)
        }
        return
    }

    if (fotoString.startsWith("content://") || fotoString.startsWith("http")) {
        AsyncImage(model = fotoString, contentDescription = null, modifier = modifier, contentScale = ContentScale.Crop)
        return
    }

    val bitmap = remember(fotoString) {
        try {
            val cleanBase64 = if (fotoString.contains(",")) fotoString.split(",")[1] else fotoString
            val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    if (bitmap != null) {
        Image(bitmap = bitmap, contentDescription = null, modifier = modifier, contentScale = ContentScale.Crop)
    } else {
        Box(modifier = modifier.background(PastelBlueBackgroundLighter), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Pets, contentDescription = null, modifier = Modifier.size(fallbackSize.dp), tint = Color.White)
        }
    }
}

@Composable
fun MascotaCardGrande(pet: Pet, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            ImagenDecodificada(fotoString = pet.fotoBase64, modifier = Modifier.fillMaxSize())
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)), startY = 200f)))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text(pet.nombre, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    InfoChip(pet.especie)
                    Spacer(modifier = Modifier.width(8.dp))
                    InfoChip(pet.sexo)
                }
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

// 🚀 VISTA DE DETALLE REESTRUCTURADA
@Composable
fun PetDetailView(pet: Pet, onBack: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit, onUpdatePhoto: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            ImagenDecodificada(fotoString = pet.fotoBase64, modifier = Modifier.fillMaxSize(), fallbackSize = 100)
            IconButton(onClick = onBack, modifier = Modifier.padding(16.dp).align(Alignment.TopStart).background(Color.Black.copy(alpha = 0.4f), CircleShape)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Text(pet.nombre, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
            Spacer(modifier = Modifier.height(24.dp))

            TarjetaInformacionMascota(pet = pet)

            Spacer(modifier = Modifier.weight(1f))

            BotonesAccionMascota(
                onCambiarFoto = onUpdatePhoto,
                onEditar = onEdit,
                onEliminar = onDelete
            )
        }
    }
}

// 🧩 TARJETA DE INFORMACIÓN PERFECTAMENTE SIMÉTRICA
@Composable
fun TarjetaInformacionMascota(pet: Pet) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Información de la Mascota",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextColorDark
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoRowItem(icono = Icons.Outlined.Pets, etiqueta = "Especie", valor = pet.especie)
                    InfoRowItem(icono = Icons.Outlined.CalendarMonth, etiqueta = "Nacimiento", valor = pet.edad)
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(90.dp)
                        .background(Color.LightGray.copy(alpha = 0.4f))
                )

                Spacer(modifier = Modifier.width(20.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val iconoSexo = if (pet.sexo == "Hembra") Icons.Outlined.Female else Icons.Outlined.Male
                    InfoRowItem(icono = iconoSexo, etiqueta = "Sexo", valor = pet.sexo)
                    InfoRowItem(icono = Icons.Outlined.MonitorWeight, etiqueta = "Peso", valor = "${pet.peso} kg")
                }
            }
        }
    }
}

@Composable
fun InfoRowItem(icono: ImageVector, etiqueta: String, valor: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = PastelBlueBackgroundLighter,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = PastelGreenPrimary,
                modifier = Modifier.padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = etiqueta, fontSize = 12.sp, color = TextColorGray)
            Text(text = valor, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
        }
    }
}

// 🧩 BOTONES COMPACTOS CON FONDO TINTADO
@Composable
fun BotonesAccionMascota(
    onCambiarFoto: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onCambiarFoto,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, PastelGreenPrimary.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = PastelGreenPrimary.copy(alpha = 0.3f),
                contentColor = PastelGreenPrimary
            )
        ) {
            Icon(Icons.Outlined.CameraAlt, contentDescription = "Cambiar Foto", modifier = Modifier.size(26.dp))
        }

        val colorEditar = Color(0xFFFBC02D)
        OutlinedButton(
            onClick = onEditar,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, colorEditar.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = colorEditar.copy(alpha = 0.1f),
                contentColor = colorEditar
            )
        ) {
            Icon(Icons.Outlined.Edit, contentDescription = "Editar", modifier = Modifier.size(26.dp))
        }

        OutlinedButton(
            onClick = onEliminar,
            modifier = Modifier.weight(1f).height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Red.copy(alpha = 0.08f),
                contentColor = Color.Red.copy(alpha = 0.8f)
            )
        ) {
            Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", modifier = Modifier.size(26.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MascotaDialogPremium(petExistente: Pet?, onDismiss: () -> Unit, onSave: (Pet) -> Unit) {
    var nombre by remember { mutableStateOf(petExistente?.nombre ?: "") }
    var especie by remember { mutableStateOf(petExistente?.especie ?: "") }
    var edad by remember { mutableStateOf(petExistente?.edad ?: "") }
    var peso by remember { mutableStateOf(petExistente?.peso ?: "") }

    var sexo by remember { mutableStateOf(petExistente?.sexo ?: "") }
    var expanded by remember { mutableStateOf(false) }
    val opcionesSexo = listOf("Macho", "Hembra", "Sin respuesta")

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val fieldBackgroundColor = Color(0xFFF8F9FA)

    // 🤖 --- LÓGICA DEL CAPTCHA --- 🤖
    val esNuevaMascota = petExistente == null
    // Generamos dos números aleatorios entre 1 y 10
    var num1 by remember { mutableStateOf((1..10).random()) }
    var num2 by remember { mutableStateOf((1..10).random()) }
    var captchaInput by remember { mutableStateOf("") }

    // Validamos si la respuesta es correcta. Si estamos editando, siempre es 'true'
    val captchaCorrecto = if (esNuevaMascota) {
        captchaInput.trim() == (num1 + num2).toString()
    } else {
        true
    }

    LaunchedEffect(petExistente) {
        if (petExistente != null && petExistente.edad.contains("-")) {
            try {
                val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val visualFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                edad = visualFormat.format(dbFormat.parse(petExistente.edad)!!)
            } catch (e: Exception) { /* Ignorar si falla */ }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        edad = sdf.format(Date(millis))
                    }
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = datePickerState) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = PastelGreenPrimary),
            elevation = CardDefaults.cardElevation(8.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(shape = RoundedCornerShape(16.dp), color = PastelBlueBackgroundLighter, modifier = Modifier.size(64.dp)) {
                    Icon(Icons.Filled.Pets, contentDescription = null, tint = PastelGreenPrimary, modifier = Modifier.padding(16.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(if (esNuevaMascota) "Nueva Mascota" else "Editar Mascota", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre de la mascota") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedBorderColor = PastelGreenPrimary, unfocusedBorderColor = Color.Transparent))
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = especie, onValueChange = { especie = it }, label = { Text("Especie (Perro, Gato...)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedBorderColor = PastelGreenPrimary, unfocusedBorderColor = Color.Transparent))
                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = sexo, onValueChange = {}, readOnly = true, label = { Text("Sexo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedBorderColor = PastelGreenPrimary, unfocusedBorderColor = Color.Transparent)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = Color.White) {
                        opcionesSexo.forEach { seleccion -> DropdownMenuItem(text = { Text(seleccion) }, onClick = { sexo = seleccion; expanded = false }) }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = edad, onValueChange = { }, readOnly = true, label = { Text("F. Nacimiento") },
                        modifier = Modifier.weight(1f).clickable { showDatePicker = true }, enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(disabledTextColor = TextColorDark, disabledBorderColor = Color.Transparent, disabledContainerColor = fieldBackgroundColor, disabledLabelColor = TextColorGray)
                    )
                    OutlinedTextField(value = peso, onValueChange = { peso = it }, label = { Text("Peso (kg)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedBorderColor = PastelGreenPrimary, unfocusedBorderColor = Color.Transparent))
                }

                // 🤖 --- INTERFAZ DEL CAPTCHA (Solo al crear) --- 🤖
                if (esNuevaMascota) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PastelBlueBackgroundLighter),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Verificación de seguridad",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextColorDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "¿$num1 + $num2?",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PastelGreenPrimary
                                )
                                OutlinedTextField(
                                    value = captchaInput,
                                    onValueChange = { if (it.length <= 3) captchaInput = it }, // Máximo 3 dígitos
                                    label = { Text("Resultado") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = if (captchaCorrecto) PastelGreenPrimary else Color.Red.copy(alpha = 0.5f),
                                        unfocusedBorderColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = TextColorGray, fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(Pet(id = petExistente?.id ?: System.currentTimeMillis(), nombre = nombre, especie = especie, sexo = sexo, edad = edad, peso = peso, fotoBase64 = petExistente?.fotoBase64)) },
                        // 🔒 El botón solo se habilita si los datos están llenos Y el captcha es correcto
                        enabled = nombre.isNotBlank() && especie.isNotBlank() && sexo.isNotBlank() && captchaCorrecto,
                        colors = ButtonDefaults.buttonColors(containerColor = PastelGreenPrimary), shape = RoundedCornerShape(16.dp)
                    ) { Text("Guardar", color = TextColorDark, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}