package com.example.saluspet.features.auth.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.example.saluspet.features.pets.presentation.PetViewModel
import com.example.saluspet.ui.theme.*

// Modelo de datos temporal para el Usuario (Hasta que tengamos Base de Datos)
data class Usuario(
    val nombre: String = "Juan",
    val apellidos: String = "Pérez García",
    val correo: String = "juan.perez@email.com",
    val telefono: String = "+34 600 123 456",
    val fechaNacimiento: String = "15/05/1990",
    val fotoUri: String? = null
)

@Composable
fun ProfileScreen(onLogout: () -> Unit, petViewModel: PetViewModel) {
    // Estado del usuario actual
    var usuario by remember { mutableStateOf(Usuario()) }

    // Estado para controlar si mostramos el diálogo de edición
    var showEditDialog by remember { mutableStateOf(false) }

    // Lanzador para cambiar la foto de perfil del usuario (Con recorte 1:1)
    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                usuario = usuario.copy(fotoUri = uri.toString())
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mi Perfil", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
        Spacer(modifier = Modifier.height(24.dp))

        // --- FOTO DE PERFIL DEL USUARIO ---
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable { lanzarRecorteUsuario() },
                color = PastelBlueBackgroundLighter,
                border = androidx.compose.foundation.BorderStroke(3.dp, PastelGreenPrimary)
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
            // Icono de cámara superpuesto
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
        Text("${usuario.nombre} ${usuario.apellidos}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
        Text("Tutor de ${petViewModel.listaMascotas.size} mascota(s)", fontSize = 14.sp, color = PastelGreenPrimary, fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(32.dp))

        // --- TARJETA DE INFORMACIÓN ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoRow(icon = Icons.Filled.Email, label = "Correo Electrónico", value = usuario.correo)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))

                ProfileInfoRow(icon = Icons.Filled.Phone, label = "Teléfono", value = usuario.telefono)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))

                ProfileInfoRow(icon = Icons.Filled.Cake, label = "Fecha de Nacimiento", value = usuario.fechaNacimiento)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- BOTONES DE ACCIÓN ---
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
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCCCC)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión", color = Color.Red, fontWeight = FontWeight.Bold)
        }
    }

    // --- DIÁLOGO DE EDICIÓN DE PERFIL ---
    if (showEditDialog) {
        UsuarioEditDialog(
            usuarioActual = usuario,
            onDismiss = { showEditDialog = false },
            onSave = { usuarioEditado ->
                usuario = usuarioEditado
                showEditDialog = false
            }
        )
    }
}

// Componente reutilizable para cada fila de información
@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = PastelBlueBackgroundLighter.copy(alpha = 0.3f)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.padding(8.dp), tint = PastelGreenPrimary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = TextColorGray)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextColorDark)
        }
    }
}

// Diálogo para editar los datos del usuario
@Composable
fun UsuarioEditDialog(usuarioActual: Usuario, onDismiss: () -> Unit, onSave: (Usuario) -> Unit) {
    var nombre by remember { mutableStateOf(usuarioActual.nombre) }
    var apellidos by remember { mutableStateOf(usuarioActual.apellidos) }
    var correo by remember { mutableStateOf(usuarioActual.correo) }
    var telefono by remember { mutableStateOf(usuarioActual.telefono) }
    var fechaNacimiento by remember { mutableStateOf(usuarioActual.fechaNacimiento) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = apellidos, onValueChange = { apellidos = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo Electrónico") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = fechaNacimiento, onValueChange = { fechaNacimiento = it }, label = { Text("Fecha de Nacimiento") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(Usuario(nombre, apellidos, correo, telefono, fechaNacimiento, usuarioActual.fotoUri))
                },
                enabled = nombre.isNotBlank() && correo.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}