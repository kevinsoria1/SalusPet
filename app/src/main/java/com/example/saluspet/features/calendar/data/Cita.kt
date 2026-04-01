package com.example.saluspet.features.calendar.data

data class Cita(
    val idCita: Int,
    val idMascota: Int,
    val titulo: String,
    val fecha: String,
    val hora: String,
    val descripcion: String?,
    val estado: String
)