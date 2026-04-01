package com.example.saluspet.features.pets.data

import com.google.gson.annotations.SerializedName

data class Mascota(
    @SerializedName("idMascota") val idMascota: Int,
    @SerializedName("idUsuario") val idUsuario: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("especie") val especie: String,
    @SerializedName("genero") val genero: String? = null,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String? = null,
    @SerializedName("peso") val peso: Double? = null,
    @SerializedName("fotoBase64") val fotoBase64: String? = null, // La usa para enviártela en el POST
    @SerializedName("urlFoto") val urlFoto: String? = null,       // La usa para recibirla de tu GET y actualizar en el PUT
    @SerializedName("validada") val validada: Boolean? = null
)