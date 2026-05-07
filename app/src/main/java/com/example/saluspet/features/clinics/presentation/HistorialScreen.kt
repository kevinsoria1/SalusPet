package com.example.saluspet.features.clinics.presentation

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saluspet.R
import com.example.saluspet.features.clinics.data.HistorialClinico
import com.example.saluspet.features.pets.presentation.PetViewModel
import com.example.saluspet.ui.theme.PastelGreenPrimary
import com.example.saluspet.ui.theme.TextColorDark
import com.example.saluspet.ui.theme.TextColorGray
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(viewModel: HistorialViewModel, petViewModel: PetViewModel) {

    val misMascotas = petViewModel.listaMascotas

    var mascotaSeleccionada by remember { mutableStateOf(misMascotas.firstOrNull()) }
    var expandedPets by remember { mutableStateOf(false) }

    LaunchedEffect(mascotaSeleccionada) {
        mascotaSeleccionada?.let { pet ->
            viewModel.cargarHistorialMascota(pet.id.toInt())
        }
    }

    val historial = viewModel.listaHistorial

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Historial Clínico", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
        Spacer(modifier = Modifier.height(16.dp))

        // --- SELECTOR DE MASCOTAS ---
        ExposedDropdownMenuBox(expanded = expandedPets, onExpandedChange = { expandedPets = !expandedPets }) {
            OutlinedTextField(
                value = mascotaSeleccionada?.nombre ?: "Sin mascotas",
                onValueChange = {},
                readOnly = true,
                label = { Text("Ver historial de:") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPets) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8F9FA),
                    unfocusedContainerColor = Color(0xFFF8F9FA),
                    focusedBorderColor = PastelGreenPrimary,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            ExposedDropdownMenu(expanded = expandedPets, onDismissRequest = { expandedPets = false }, containerColor = Color.White) {
                misMascotas.forEach { pet ->
                    DropdownMenuItem(
                        text = { Text(pet.nombre) },
                        onClick = {
                            mascotaSeleccionada = pet
                            expandedPets = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (historial.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No hay registros médicos para esta mascota.", color = TextColorGray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(historial) { informe ->
                    HistorialCard(
                        informe = informe,
                        nombreMascota = mascotaSeleccionada?.nombre ?: "Paciente"
                    )
                }
            }
        }
    }
}

@Composable
fun HistorialCard(informe: HistorialClinico, nombreMascota: String) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera de la Tarjeta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PastelGreenPrimary.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.MedicalServices, contentDescription = null, tint = PastelGreenPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(informe.tipoEvento, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextColorDark)
                }
                Text(informe.fecha, color = TextColorGray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 📄 REPRESENTACIÓN VISUAL DEL INFORME (ESTILO FOLIO)
            if (!informe.descripcion.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFAFAFA),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Logo y Título Corporativo
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_saluspet1),
                                contentDescription = "Logo SalusPet",
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("INFORME CLÍNICO", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = PastelGreenPrimary)
                                Text("SALUSPET VETERINARIA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextColorGray)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = PastelGreenPrimary.copy(alpha = 0.3f), thickness = 2.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Datos resumidos
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Paciente:", fontSize = 12.sp, color = TextColorGray)
                                Text(nombreMascota, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Fecha:", fontSize = 12.sp, color = TextColorGray)
                                Text(informe.fecha, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextColorDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Atendido por:", fontSize = 12.sp, color = TextColorGray)
                        Text(informe.veterinario ?: "Equipo SalusPet", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextColorDark)

                        Spacer(modifier = Modifier.height(20.dp))

                        // Bloque de Diagnóstico
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PastelGreenPrimary.copy(alpha = 0.05f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("DIAGNÓSTICO Y TRATAMIENTO", fontSize = 12.sp, color = PastelGreenPrimary, fontWeight = FontWeight.ExtraBold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = informe.descripcion, fontSize = 14.sp, color = TextColorDark, lineHeight = 22.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Firma
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                            HorizontalDivider(modifier = Modifier.width(120.dp), color = TextColorGray, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Firma del Veterinario", fontSize = 11.sp, color = TextColorGray)
                        }
                    }
                }
            }

            // 📄 BOTÓN DE DESCARGA (Genera el PDF con el mismo estilo)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { generarPDFLocalSaluspet(context, informe, nombreMascota) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Descargar PDF del Informe", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Función que genera un archivo PDF físico en el almacenamiento del dispositivo.
 * Utiliza los mismos recursos visuales (Logo y Colores) que la interfaz de la app.
 */
fun generarPDFLocalSaluspet(context: Context, informe: HistorialClinico, nombreMascota: String) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas

    // Estilos de pincel
    val paintVerde = Paint().apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textSize = 22f
        color = android.graphics.Color.rgb(108, 194, 155)
    }
    val paintGrisSub = Paint().apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textSize = 14f
        color = android.graphics.Color.GRAY
    }
    val paintNegroBold = Paint().apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textSize = 14f
        color = android.graphics.Color.BLACK
    }
    val paintTextoNormal = Paint().apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL); textSize = 14f
        color = android.graphics.Color.DKGRAY
    }
    val paintLinea = Paint().apply { color = android.graphics.Color.LTGRAY; strokeWidth = 2f }

    // 1. Dibujar Logo e Identidad
    try {
        val logo = BitmapFactory.decodeResource(context.resources, R.drawable.logo_saluspet1)
        val scaledLogo = android.graphics.Bitmap.createScaledBitmap(logo, 85, 85, false)
        canvas.drawBitmap(scaledLogo, 40f, 40f, null)
    } catch (e: Exception) {}

    canvas.drawText("INFORME CLÍNICO", 145f, 75f, paintVerde)
    canvas.drawText("SALUSPET VETERINARIA", 145f, 100f, paintGrisSub)
    canvas.drawLine(40f, 140f, 555f, 140f, paintLinea)

    // 2. Información del Paciente
    canvas.drawText("Paciente:", 40f, 180f, paintGrisSub)
    canvas.drawText(nombreMascota, 40f, 205f, paintNegroBold)
    canvas.drawText("Fecha:", 420f, 180f, paintGrisSub)
    canvas.drawText(informe.fecha, 420f, 205f, paintNegroBold)

    canvas.drawText("Atendido por:", 40f, 245f, paintGrisSub)
    canvas.drawText(informe.veterinario ?: "Equipo SalusPet", 40f, 270f, paintNegroBold)
    canvas.drawLine(40f, 300f, 555f, 300f, paintLinea)

    // 3. Contenido del Informe
    canvas.drawText("DIAGNÓSTICO Y TRATAMIENTO", 40f, 340f, paintVerde)

    var yPos = 375f
    val texto = informe.descripcion ?: "Sin detalles registrados."
    val chunkedLines = texto.chunked(70) // Dividir texto largo en líneas
    for (line in chunkedLines) {
        canvas.drawText(line.trim(), 40f, yPos, paintTextoNormal)
        yPos += 25f
    }

    // 4. Pie de página (Firma)
    val firmaY = yPos + 80f
    canvas.drawLine(380f, firmaY, 555f, firmaY, paintLinea)
    canvas.drawText("Firma del Veterinario", 405f, firmaY + 25f, paintGrisSub)

    pdfDocument.finishPage(page)

    // --- GUARDAR EN DESCARGAS Y ABRIR ---
    try {
        // 1. Apuntamos a la carpeta de Descargas del propio dispositivo
        val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!folder.exists()) {
            folder.mkdirs()
        }

        // 2. Creamos el archivo PDF con un nombre claro
        val nombreArchivo = "Informe_SalusPet_${nombreMascota}.pdf"
        val file = File(folder, nombreArchivo)
        pdfDocument.writeTo(FileOutputStream(file))

        // 3. Puente de seguridad (FileProvider)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        // 4. Intent para visualizarlo inmediatamente
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // 5. Abrir el selector (Aquí puedes elegir el visor de tu móvil, no solo Drive)
        context.startActivity(Intent.createChooser(intent, "Abrir Informe Médico"))

        Toast.makeText(context, "Guardado en Descargas: $nombreArchivo", Toast.LENGTH_LONG).show()

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    } finally {
        pdfDocument.close()
    }
}