package com.example.saluspet.features.pets.presentation

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

// 1. Actualizamos el modelo Pet para incluir la foto
data class Pet(
    val id: Long = System.currentTimeMillis(),
    val nombre: String,
    val especie: String, // Perro, Gato, etc.
    val sexo: String,
    val edad: String,
    val peso: String,
    // Nuevo campo: Uri como String? (null por defecto)
    val fotoUri: String? = null
)

// 2. Actualizamos el ViewModel para manejar la foto
class PetViewModel : ViewModel() {
    val listaMascotas = mutableStateListOf<Pet>()

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

    // --- NUEVA FUNCIÓN PARA ACTUALIZAR LA FOTO ---
    fun actualizarFotoPet(pet: Pet, nuevaFotoUri: String?) {
        val index = listaMascotas.indexOfFirst { it.id == pet.id }
        if (index != -1) {
            // Creamos una copia de la mascota con la nueva foto Uri
            val petActualizada = pet.copy(fotoUri = nuevaFotoUri)
            listaMascotas[index] = petActualizada
        }
    }
}