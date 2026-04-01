package com.example.saluspet.features.auth.data

import com.example.saluspet.features.pets.data.Mascota
import java.util.Date


data class Usuario(
    val idUsuario: Int,
    val nombre: String,
    val apellidos: String? = null,
    val email: String,
    val telefono: String? = null,
    val password: String,
    val rol: String,
    val fechaRegistro: Date? = null,
    val mascota: List<Mascota> = emptyList()
)