package com.example.saluspet.features.clinics.presentation

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saluspet.core.network.RetrofitClient
import com.example.saluspet.features.clinics.data.HistorialClinico
import kotlinx.coroutines.launch

class HistorialViewModel : ViewModel() {

    val listaHistorial = mutableStateListOf<HistorialClinico>()

    fun cargarHistorialMascota(idMascota: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.obtenerHistorialMascota(idMascota)
                if (response.isSuccessful && response.body() != null) {
                    listaHistorial.clear()
                    listaHistorial.addAll(response.body()!!)
                } else {
                    println("Error al descargar historial: ${response.code()}")
                }
            } catch (e: Exception) {
                println("Fallo de red en historial: ${e.message}")
            }
        }
    }

}