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

data class Pet(
    val id: Long,
    val nombre: String,
    val especie: String,
    val sexo: String,
    val edad: String,
    val peso: String,
    val fotoBase64: String? = null // 猬咃笍 Ya lo ten铆as perfecto
)

class PetViewModel : ViewModel() {

    // Instanciamos el repositorio directamente
    private val repository = PetRepository()

    // La lista que lee tu PetHomeScreen
    val listaMascotas = mutableStateListOf<Pet>()

    init {
        // Nada m谩s cargar la app, pedimos las mascotas a la base de datos de Aar贸n
        cargarMascotas()
    }

    fun cargarMascotas() {
        viewModelScope.launch {
            try {
                // 1. Descargamos de la API (nos devuelve una lista de Mascota)
                val mascotasApi = repository.obtenerMascotasDesdeServidor()

                // Formateador para pasar de Date a String legible (ej: 12/05/2024)
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                // 2. Convertimos el formato del servidor (Mascota) al de tu interfaz (Pet)
                val mascotasConvertidas = mascotasApi.map { apiMascota ->
                    Pet(
                        id = apiMascota.idMascota.toLong(),
                        nombre = apiMascota.nombre,
                        especie = apiMascota.especie,
                        sexo = apiMascota.genero ?: "No especificado",
                        edad = apiMascota.fechaNacimiento ?: "-",
                        peso = apiMascota.peso.toString(),
                        // 馃敟 MODIFICADO: Ahora lee "urlFoto" (GET de C#) o "fotoBase64"
                        fotoBase64 = apiMascota.urlFoto ?: apiMascota.fotoBase64
                    )
                }

                // 3. Actualizamos la pantalla
                listaMascotas.clear()
                listaMascotas.addAll(mascotasConvertidas)

            } catch (e: Exception) {
                println("Error al cargar mascotas: ${e.message}")
            }
        }
    }

    // --- NUEVA FUNCI脫N PARA ENVIAR AL SERVIDOR ---
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
                // 1. Recuperamos el ID del usuario
                val sharedPreferences = context.getSharedPreferences("perfil_saluspet", Context.MODE_PRIVATE)
                val idPropietario = sharedPreferences.getInt("idUsuario", 0)

                if (idPropietario == 0) {
                    println("Error: No se encontr贸 el ID del usuario.")
                    return@launch
                }

                // 2. Creamos el objeto exacto que pide el Swagger de Aar贸n
                val nuevaMascotaApi = Mascota(
                    idMascota = 0,
                    idUsuario = idPropietario,
                    nombre = nombre,
                    especie = especie,
                    peso = peso,
                    fechaNacimiento = fechaNac,
                    genero = genero,
                    fotoBase64 = fotoBase64    // 猬咃笍 CORRECCI脫N: Ahora s铆 enviamos la foto
                )

                // 3. Lo enviamos por Retrofit
                val response = RetrofitClient.apiService.registrarMascota(nuevaMascotaApi)

                if (response.isSuccessful) {
                    println("隆Mascota guardada en la base de datos de Aar贸n!")
                    // Recargamos la lista para que aparezca en pantalla
                    cargarMascotas()
                } else {
                    println("Error del servidor (${response.code()}): ${response.errorBody()?.string()}")
                }

            } catch (e: Exception) {
                println("Fallo de conexi贸n: ${e.message}")
            }
        }
    }

    // --- Mantenemos estas funciones para que la UI no d茅 error de momento ---
    fun agregarMascota(pet: Pet) {
        listaMascotas.add(pet)
    }

    fun eliminarMascota(pet: Pet) {
        listaMascotas.remove(pet)
    }

    fun editarMascota(petAntiguo: Pet, petNuevo: Pet) {
        val index = listaMascotas.indexOfFirst { it.id == petAntiguo.id }
        if (index != -1) {
            listaMascotas[index] = petNuevo
        }
    }

    // Aseg煤rate de pasar el 'Context' como primer par谩metro
    fun actualizarFotoPet(context: Context, pet: Pet, nuevaFotoBase64: String?) {
        viewModelScope.launch {
            try {
                // 1. Actualizamos la pantalla del móvil rápido
                val index = listaMascotas.indexOfFirst { it.id == pet.id }
                if (index != -1) {
                    listaMascotas[index] = pet.copy(fotoBase64 = nuevaFotoBase64)
                }

                // 2. Llamamos al NUEVO ENDPOINT súper seguro de Aarón
                val bodyRequest = mapOf("fotoBase64" to nuevaFotoBase64)
                val response = RetrofitClient.apiService.actualizarFotoMascota(pet.id.toInt(), bodyRequest)

                if (response.isSuccessful) {
                    println("¡Bingo! Foto subida directamente a la base de datos de Aarón")
                } else {
                    println("Error en la subida (${response.code()}): ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                println("Fallo de conexión crítico: ${e.message}")
            }
        }
    }
}