package com.example.saluspet.features.calendar.data

data class ClinicaCercana(
    val nombre: String,
    val direccion: String,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val distanciaKm: Double = 0.0,
    val valoracion: Double,
    val horasDisponibles: List<String> = listOf("09:00", "12:30", "17:00")
)