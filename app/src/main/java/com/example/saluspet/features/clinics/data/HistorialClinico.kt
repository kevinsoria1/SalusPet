package com.example.saluspet.features.clinics.data

import com.google.gson.annotations.SerializedName

data class HistorialClinico(
    @SerializedName("idHistorial") val idHistorial: Int,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("tipoEvento") val tipoEvento: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("veterinario") val veterinario: String?,
    @SerializedName("urlDocumento") val urlDocumento: String? // Clave para el PDF
)