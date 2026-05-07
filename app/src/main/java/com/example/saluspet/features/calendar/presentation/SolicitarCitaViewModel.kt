package com.example.saluspet.features.calendar.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saluspet.core.network.RetrofitClient
import com.example.saluspet.features.calendar.data.Veterinario
import kotlinx.coroutines.launch

class SolicitarCitaViewModel : ViewModel() {

    // Lista real que se llenará con los datos de tu base de datos MySQL a través de NestJS
    var listaVeterinarios by mutableStateOf<List<Veterinario>>(emptyList())
    var estaCargandoVeterinarios by mutableStateOf(false)

    // Función que se llama al abrir el pop-up para traer los veterinarios
    fun cargarVeterinariosDesdeBD() {
        viewModelScope.launch {
            estaCargandoVeterinarios = true
            try {
                // Llamada REAL a tu endpoint @GET("veterinarios")
                val veterinariosBD = RetrofitClient.apiService.getVeterinarios()
                listaVeterinarios = veterinariosBD
            } catch (e: Exception) {
                e.printStackTrace()
                // Si falla la conexión, la lista se queda vacía pero la app no crashea
            } finally {
                estaCargandoVeterinarios = false
            }
        }
    }

    // Función para enviar todos los datos del formulario (aquí harás el POST)
    fun enviarSolicitud(mascota: String, veterinarioId: Int, motivo: String, fecha: String, hora: String) {
        viewModelScope.launch {
            try {
                // Aquí conectarás con el endpoint @POST("api/Citas") de tu backend
                println("Enviando a NestJS -> Mascota: $mascota | Vet ID: $veterinarioId | Motivo: $motivo | Fecha: $fecha | Hora: $hora")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}