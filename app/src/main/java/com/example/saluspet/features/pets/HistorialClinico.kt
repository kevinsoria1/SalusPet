package com.example.saluspet.features.pets

data class HistorialClinico(
    val idHistorial: Int,
    val idMascota: Int,
    val idVeterinario: Int?,
    val tipoEvento: String,
    val fecha: String,
    val descripcion: String?,
    val urlDocumento: String?
)