package com.example.saluspet.features.pets.presentation

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saluspet.core.network.RetrofitClient
import com.example.saluspet.features.pets.data.Mascota
import com.example.saluspet.features.pets.domain.PetRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// Modelo visual para la interfaz
data class Pet(
    val id: Long,
    val nombre: String,
    val especie: String,
    val sexo: String,
    val edad: String,
    val peso: String,
    val fotoBase64: String? = null
)

class PetViewModel : ViewModel() {

    private val repository = PetRepository()
    val listaMascotas = mutableStateListOf<Pet>()

    // 🌟 MODIFICADO: Ahora cargarMascotas necesita el Context para saber QUIÉN es el usuario
    fun cargarMascotas(context: Context) {
        viewModelScope.launch {
            try {
                // 1. Sacamos el ID del usuario logueado
                val sharedPref = context.getSharedPreferences("perfil_saluspet", Context.MODE_PRIVATE)
                val idUsuario = sharedPref.getInt("idUsuario", 0)

                if (idUsuario == 0) {
                    println("Error: No hay usuario logueado")
                    return@launch
                }

                // 2. 🌟 LLAMADA CORREGIDA: Ahora le pasamos el idUsuario al Repository
                val mascotasApi = repository.obtenerMascotasDesdeServidor(idUsuario)

                // 3. Convertimos el formato del servidor (Mascota) al de la interfaz (Pet)
                val mascotasConvertidas = mascotasApi.map { apiMascota ->
                    Pet(
                        id = apiMascota.idMascota.toLong(),
                        nombre = apiMascota.nombre,
                        especie = apiMascota.especie,
                        sexo = apiMascota.genero ?: "No especificado",
                        edad = apiMascota.fechaNacimiento ?: "-",
                        peso = apiMascota.peso.toString(),
                        fotoBase64 = apiMascota.urlFoto ?: apiMascota.fotoBase64
                    )
                }

                listaMascotas.clear()
                listaMascotas.addAll(mascotasConvertidas)

            } catch (e: Exception) {
                println("Error al cargar mascotas: ${e.message}")
            }
        }
    }

    // --- FUNCIÓN PARA CREAR (CORREGIDA) ---
    fun crearMascotaEnServidor(
        context: Context,
        nombre: String,
        especie: String,
        peso: Double,
        fechaNac: String,
        genero: String,
        fotoBase64: String?
    ) {
        viewModelScope.launch {
            try {
                val sharedPreferences = context.getSharedPreferences("perfil_saluspet", Context.MODE_PRIVATE)
                val idPropietario = sharedPreferences.getInt("idUsuario", 0)

                if (idPropietario == 0) return@launch

                val nuevaMascotaApi = Mascota(
                    idMascota = 0,
                    idUsuario = idPropietario,
                    nombre = nombre,
                    especie = especie,
                    peso = peso,
                    fechaNacimiento = fechaNac,
                    genero = genero,
                    fotoBase64 = fotoBase64
                )

                val response = RetrofitClient.apiService.registrarMascota(nuevaMascotaApi)

                if (response.isSuccessful) {
                    // 🌟 Recargamos pasando el context
                    cargarMascotas(context)
                }
            } catch (e: Exception) {
                println("Fallo de conexión: ${e.message}")
            }
        }
    }

    // --- BORRAR MASCOTA (CORREGIDA) ---
    fun eliminarMascota(pet: Pet) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.eliminarMascota(pet.id.toInt())
                if (response.isSuccessful) {
                    listaMascotas.remove(pet)
                }
            } catch (e: Exception) {
                println("Error de red: ${e.message}")
            }
        }
    }

    // ✏️ EDITAR MASCOTA (Actualización Optimista)
    fun editarMascota(context: android.content.Context, petAntiguo: Pet, petNuevo: Pet) {

        // 🚀 1. ACTUALIZACIÓN AL INSTANTE: Cambiamos la lista local sin esperar a nadie
        val index = listaMascotas.indexOfFirst { it.id == petAntiguo.id }
        if (index != -1) {
            listaMascotas[index] = petNuevo
        }

        // 🌐 2. EN SEGUNDO PLANO: Le avisamos al servidor de Aarón
        viewModelScope.launch {
            try {
                val sharedPreferences = context.getSharedPreferences("perfil_saluspet", android.content.Context.MODE_PRIVATE)
                val idPropietario = sharedPreferences.getInt("idUsuario", 0)

                val mascotaActualizadaApi = com.example.saluspet.features.pets.data.Mascota(
                    idMascota = petNuevo.id.toInt(),
                    idUsuario = idPropietario,
                    nombre = petNuevo.nombre,
                    especie = petNuevo.especie,
                    peso = petNuevo.peso.replace(",", ".").toDoubleOrNull() ?: 1.0,
                    fechaNacimiento = petNuevo.edad, // Ya viene limpia del HomeScreen
                    genero = petNuevo.sexo,
                    fotoBase64 = petNuevo.fotoBase64
                )

                val response = com.example.saluspet.core.network.RetrofitClient.apiService.actualizarMascota(petNuevo.id.toInt(), mascotaActualizadaApi)

                if (response.isSuccessful) {
                    println("✅ Mascota actualizada en la base de datos MySQL")
                } else {
                    println("❌ Aarón rechazó los datos. Código: ${response.code()}")
                    // Opcional: Podrías revertir el cambio visual aquí si quisieras (listaMascotas[index] = petAntiguo)
                }
            } catch (e: Exception) {
                println("❌ Fallo de red: ${e.message}")
            }
        }
    }

    // --- ACTUALIZAR FOTO ---
    fun actualizarFotoPet(context: Context, pet: Pet, nuevaFotoBase64: String?) {
        viewModelScope.launch {
            try {
                val bodyRequest = mapOf("fotoBase64" to nuevaFotoBase64)
                val response = RetrofitClient.apiService.actualizarFotoMascota(pet.id.toInt(), bodyRequest)
                if (response.isSuccessful) {
                    val index = listaMascotas.indexOfFirst { it.id == pet.id }
                    if (index != -1) {
                        listaMascotas[index] = pet.copy(fotoBase64 = nuevaFotoBase64)
                    }
                }
            } catch (e: Exception) {
                println("Fallo de conexión: ${e.message}")
            }
        }
    }
}