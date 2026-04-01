package com.example.saluspet.features.auth.data

data class RegisterRequest(
    val nombre: String,
    val apellidos: String? = null,
    val telefono: String? = null,
    val email: String,
    val password: String
)