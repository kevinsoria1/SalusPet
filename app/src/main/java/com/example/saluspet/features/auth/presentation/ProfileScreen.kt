package com.example.saluspet.features.auth.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.saluspet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class) // Necesario para FilterChip
@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    // Estado para la foto de perfil
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    // Estado para las mascotas (Simulado)
    val mascotas = remember { mutableStateListOf("Luna", "Milo") }
    var mascotaSeleccionada by remember { mutableStateOf("Luna") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mi Perfil", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(20.dp))

        // SECCIÓN FOTO DE PERFIL
        Box(
            modifier = Modifier
                .size(120.dp)
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.BottomEnd
        ) {
            if (imageUri == null) {
                Surface(modifier = Modifier.fillMaxSize(), shape = CircleShape, color = PastelBlueBackgroundLighter) {
                    Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.padding(25.dp))
                }
            } else {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            // Icono pequeño de "editar"
            Surface(Modifier.size(30.dp), shape = CircleShape, color = PastelGreenPrimary) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.padding(5.dp), tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text("Gestionar mis Mascotas", fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            items(mascotas) { mascota ->
                FilterChip(
                    selected = mascotaSeleccionada == mascota,
                    onClick = { mascotaSeleccionada = mascota },
                    label = { Text(mascota) }
                )
            }
            item {
                IconButton(onClick = { mascotas.add("Nueva Mascota") }) {
                    Icon(Icons.Filled.Add, contentDescription = "Añadir")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = PastelBlueBackgroundLighter.copy(alpha = 0.5f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Viendo datos de: $mascotaSeleccionada", fontWeight = FontWeight.Bold)
                Text("Aquí aparecerá el historial específico de esta mascota más adelante.")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCCCC)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null, tint = Color.Red)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar Sesión", color = Color.Red)
        }
    }
}