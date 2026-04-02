package com.example.saluspet.features.pets.domain

import com.example.saluspet.core.network.RetrofitClient
import com.example.saluspet.features.pets.data.Mascota
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PetRepository {
    // 🌟 AHORA RECIBIMOS EL ID DEL USUARIO COMO PARÁMETRO
    suspend fun obtenerMascotasDesdeServidor(idUsuario: Int): List<Mascota> {
        // Ejecutamos la llamada en un hilo secundario para no congelar la pantalla
        return withContext(Dispatchers.IO) {
            try {
                // 🌟 LE PASAMOS EL ID AL ENDPOINT DE AARÓN (Aquí es donde se arregla el error)
                val response = RetrofitClient.apiService.getMascotas(idUsuario)

                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    println("Error del servidor: ${response.code()}")
                    emptyList()
                }
            } catch (e: Exception) {
                println("Error de conexión (Swagger apagado o sin internet): ${e.message}")
                emptyList()
            }
        }
    }
}