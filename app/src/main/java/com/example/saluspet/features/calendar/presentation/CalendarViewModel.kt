package com.example.saluspet.features.calendar.presentation

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class CalendarViewModel : ViewModel() {
    // Esta lista vive fuera del ciclo de vida de la pantalla
    val listaCitas = mutableStateListOf<Cita>()

    fun agregarCita(cita: Cita) {
        listaCitas.add(cita)
    }

    fun eliminarCita(cita: Cita) {
        listaCitas.remove(cita)
    }

    fun editarCita(citaAntigua: Cita, citaNueva: Cita) {
        val index = listaCitas.indexOfFirst { it.id == citaAntigua.id }
        if (index != -1) {
            listaCitas[index] = citaNueva
        }
    }
}