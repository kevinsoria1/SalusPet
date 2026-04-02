package com.example.saluspet.features.auth.presentation

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.saluspet.features.pets.presentation.PetViewModel
import com.example.saluspet.features.auth.data.Usuario
import com.example.saluspet.ui.theme.*

// Modelo de datos visual (Sin la fecha de nacimiento)
data class UsuarioPerfil(
    val nombre: String,
    val apellidos: String,
    val correo: String,
    val telefono: String,
    val fotoUri: String? = null
)

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    petViewModel: PetViewModel,
    profileViewModel: ProfileViewModel
) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("perfil_saluspet", Context.MODE_PRIVATE)

    // 🚀 1. Pedir los datos al servidor nada más abrir la pantalla
    LaunchedEffect(Unit) {
        profileViewModel.cargarPerfil(context)
    }

    var usuario by remember {
        mutableStateOf(
            UsuarioPerfil(
                nombre = sharedPreferences.getString("nombre", "") ?: "",
                apellidos = sharedPreferences.getString("apellidos", "") ?: "",
                correo = sharedPreferences.getString("correo", "") ?: "",
                telefono = sharedPreferences.getString("telefono", "") ?: "",
                fotoUri = sharedPreferences.getString("foto_uri", null)
            )
        )
    }

    // 🌐 2. Cuando el servidor responda, actualizamos la pantalla automáticamente
    LaunchedEffect(profileViewModel.usuarioData) {
        profileViewModel.usuarioData?.let { usuarioBD ->
            usuario = usuario.copy(
                nombre = usuarioBD.nombre ?: "",
                apellidos = usuarioBD.apellidos ?: "",
                correo = usuarioBD.email ?: "",
                telefono = usuarioBD.telefono ?: ""
            )

            // Refrescamos las memorias exclusivas del móvil (foto)
            sharedPreferences.edit()
                .putString("nombre", usuarioBD.nombre)
                .putString("apellidos", usuarioBD.apellidos)
                .putString("correo", usuarioBD.email)
                .putString("telefono", usuarioBD.telefono)
                .apply()
        }
    }

    var showEditDialog by remember { mutableStateOf(false) }

    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                val uriString = uri.toString()
                usuario = usuario.copy(fotoUri = uriString)
                sharedPreferences.edit().putString("foto_uri", uriString).apply()
            }
        }
    }

    fun lanzarRecorteUsuario() {
        imageCropLauncher.launch(
            CropImageContractOptions(
                uri = null,
                cropImageOptions = CropImageOptions(
                    imageSourceIncludeGallery = true,
                    imageSourceIncludeCamera = false,
                    fixAspectRatio = true,
                    aspectRatioX = 1,
                    aspectRatioY = 1
                )
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mi Perfil", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
        Spacer(modifier = Modifier.height(24.dp))

        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, PastelGreenPrimary, CircleShape)
                    .clickable { lanzarRecorteUsuario() },
                color = PastelBlueBackgroundLighter
            ) {
                if (usuario.fotoUri != null) {
                    AsyncImage(
                        model = usuario.fotoUri,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(24.dp),
                        tint = Color.White
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .offset(x = (-4).dp, y = (-4).dp),
                shape = CircleShape,
                color = PastelGreenPrimary
            ) {
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = "Cambiar foto",
                    modifier = Modifier.padding(8.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (usuario.nombre.isBlank()) {
            Text("¡Bienvenido/a!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
            Text("Toca 'Editar Datos' para configurar tu perfil", fontSize = 14.sp, color = TextColorGray)
        } else {
            Text("${usuario.nombre} ${usuario.apellidos}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
            Text("Tutor de ${petViewModel.listaMascotas.size} mascota(s)", fontSize = 14.sp, color = PastelGreenPrimary, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoRow(icon = Icons.Filled.Email, label = "Correo Electrónico", value = usuario.correo.ifBlank { "No especificado" })
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))

                ProfileInfoRow(icon = Icons.Filled.Phone, label = "Teléfono", value = usuario.telefono.ifBlank { "No especificado" })
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = { showEditDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextColorDark)
        ) {
            Icon(Icons.Filled.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Editar Datos Personales")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                sharedPreferences.edit().clear().apply()
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCCCC)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión", color = Color.Red, fontWeight = FontWeight.Bold)
        }
    }

    if (showEditDialog) {
        UsuarioEditDialogPremium(
            usuarioActual = usuario,
            onDismiss = { showEditDialog = false },
            onSave = { usuarioEditado ->
                usuario = usuarioEditado

                // 1. Guardamos en local al instante
                sharedPreferences.edit()
                    .putString("nombre", usuarioEditado.nombre)
                    .putString("apellidos", usuarioEditado.apellidos)
                    .putString("correo", usuarioEditado.correo)
                    .putString("telefono", usuarioEditado.telefono)
                    .apply()

                // 2. Enviamos los datos reales al servidor MySQL
                val idUsuarioLogueado = sharedPreferences.getInt("idUsuario", 0)

                val usuarioParaBackend = Usuario(
                    idUsuario = idUsuarioLogueado,
                    nombre = usuarioEditado.nombre,
                    apellidos = usuarioEditado.apellidos,
                    email = usuarioEditado.correo,
                    telefono = usuarioEditado.telefono,
                    password = profileViewModel.usuarioData?.password ?: "",
                    rol = profileViewModel.usuarioData?.rol ?: "Cliente"
                )

                profileViewModel.actualizarPerfil(context, usuarioParaBackend)

                showEditDialog = false
            }
        )
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = PastelBlueBackgroundLighter.copy(alpha = 0.5f)) {
            Icon(icon, contentDescription = null, modifier = Modifier.padding(8.dp), tint = PastelGreenPrimary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = TextColorGray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextColorDark)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioEditDialogPremium(usuarioActual: UsuarioPerfil, onDismiss: () -> Unit, onSave: (UsuarioPerfil) -> Unit) {
    var nombre by remember { mutableStateOf(usuarioActual.nombre) }
    var apellidos by remember { mutableStateOf(usuarioActual.apellidos) }
    var correo by remember { mutableStateOf(usuarioActual.correo) }
    var telefono by remember { mutableStateOf(usuarioActual.telefono) }

    val fieldBackgroundColor = Color(0xFFF8F9FA)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = PastelBlueBackgroundLighter),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.size(64.dp)) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = PastelGreenPrimary, modifier = Modifier.padding(16.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Tus Datos", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedBorderColor = PastelGreenPrimary, unfocusedBorderColor = Color.Transparent)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = apellidos, onValueChange = { apellidos = it }, label = { Text("Apellidos") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedBorderColor = PastelGreenPrimary, unfocusedBorderColor = Color.Transparent)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = correo, onValueChange = { correo = it }, label = { Text("Correo Electrónico") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedBorderColor = PastelGreenPrimary, unfocusedBorderColor = Color.Transparent)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = fieldBackgroundColor, unfocusedContainerColor = fieldBackgroundColor, focusedBorderColor = PastelGreenPrimary, unfocusedBorderColor = Color.Transparent)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = TextColorGray, fontWeight = FontWeight.Bold) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(UsuarioPerfil(nombre, apellidos, correo, telefono, usuarioActual.fotoUri)) },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelGreenPrimary),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Guardar", color = TextColorDark, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}