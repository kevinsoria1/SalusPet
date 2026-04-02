package com.example.saluspet.features.calendar.data

import com.google.gson.annotations.SerializedName

data class Cita(
    @SerializedName("idCita") val idCita: Int = 0, // 0 porque al crear una nueva, MySQL asigna el ID
    @SerializedName("idMascota") val idMascota: Int,
    @SerializedName("nombreMascota") val nombreMascota: String? = null,
    @SerializedName("tipo") val tipo: String,      // ¡NUEVO! Obligatorio enviar "Personal" o "Veterinaria"
    @SerializedName("titulo") val titulo: String,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("hora") val hora: String,
    @SerializedName("descripcion") val descripcion: String? = null,
    @SerializedName("estado") val estado: String? = null // Nullable porque C# lo rellena automáticamente
)