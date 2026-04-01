package com.example.saluspet.features.pets.data

import java.sql.Blob

data class RegisterMascotaRequest(
    val idUsuario: Int,      // ¡Obligatorio para saber de quién es la mascota!
    val nombre: String,      // Obligatorio
    val especie: String,     // Obligatorio
    val genero: String? = "Desconocido",
    val peso: Double? = null,
    val fechaNacimiento: String? = null, // Enviar en formato "YYYY-MM-DD"
    val fotoBase64: String? = null
)