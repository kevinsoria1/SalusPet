package com.example.saluspet.features.calendar.data

data class Veterinario(
    val id: Int,
    val nombre: String,
    val especialidad: String,
    val valoracion: Double = 5.0,
    val horasDisponibles: List<String> = listOf("09:00", "11:30", "16:00", "18:00"),
    val lugaresDisponibles: List<String> = listOf("Hospital Central", "Clínica Norte")
)