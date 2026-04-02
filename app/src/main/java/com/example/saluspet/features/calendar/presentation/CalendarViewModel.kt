package com.example.saluspet.features.calendar.presentation

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saluspet.core.network.RetrofitClient
import com.example.saluspet.features.calendar.data.Cita
import kotlinx.coroutines.launch

class CalendarViewModel : ViewModel() {

    // Esta lista vive fuera del ciclo de vida de la pantalla
    val listaCitas = mutableStateListOf<Cita>()

    // 🌐 --- 1. DESCARGAR CITAS DE LA BASE DE DATOS DE AARÓN ---
    fun cargarCitasMascota(idMascota: Int) {
        viewModelScope.launch {
            try {
                // Llamamos a tu GET: api/Citas/mascota/{id}
                val response = RetrofitClient.apiService.obtenerCitasMascota(idMascota)
                if (response.isSuccessful && response.body() != null) {
                    listaCitas.clear()
                    listaCitas.addAll(response.body()!!)
                } else {
                    println("Error al descargar citas: ${response.code()}")
                }
            } catch (e: Exception) {
                println("Fallo de conexión al cargar citas: ${e.message}")
            }
        }
    }

    // 🌐 --- 2. ENVIAR NUEVA CITA A LA BASE DE DATOS DE AARÓN ---
    fun crearCitaEnServidor(citaNueva: Cita) {
        viewModelScope.launch {
            try {
                // Llamamos a tu POST: api/Citas
                val response = RetrofitClient.apiService.registrarCita(citaNueva)
                if (response.isSuccessful && response.body() != null) {
                    // Si el backend de Aarón dice OK (200), la añadimos a la pantalla
                    listaCitas.add(response.body()!!)
                    println("¡Cita guardada en el servidor con éxito!")
                } else {
                    println("El servidor rechazó la cita: ${response.code()}")
                }
            } catch (e: Exception) {
                println("Fallo de red al crear cita: ${e.message}")
            }
        }
    }

    // 📱 --- FUNCIONES LOCALES DE KEVIN (Mantenidas para que no le dé error) ---
    fun agregarCita(cita: Cita) {
        // Ahora es mejor que la pantalla use 'crearCitaEnServidor' en vez de esta
        listaCitas.add(cita)
    }
    fun cargarAgendaGlobal(context: Context) {
        viewModelScope.launch {
            try {
                // 1. Recuperamos el ID del usuario logueado
                val sharedPref = context.getSharedPreferences("perfil_saluspet", Context.MODE_PRIVATE)
                val idUsuario = sharedPref.getInt("idUsuario", 0)

                if (idUsuario != 0) {
                    // 2. Llamamos al nuevo endpoint de Aarón
                    val response = RetrofitClient.apiService.obtenerCitasUsuario(idUsuario)
                    if (response.isSuccessful) {
                        listaCitas.clear()
                        listaCitas.addAll(response.body() ?: emptyList())
                    }
                }
            } catch (e: Exception) {
                println("Error al cargar agenda: ${e.message}")
            }
        }
    }
    // 🗑️ --- BORRAR CITA DEL SERVIDOR ---
    fun eliminarCita(cita: Cita) {
        viewModelScope.launch {
            try {
                // 1. Llamamos a la nueva ruta de Retrofit para que Aarón la borre de MySQL
                val response = RetrofitClient.apiService.eliminarCita(cita.idCita)

                if (response.isSuccessful) {
                    // 2. Si el servidor la borra correctamente, la quitamos de la pantalla
                    listaCitas.remove(cita)
                    println("Cita borrada del servidor correctamente.")
                } else {
                    println("Error del servidor al borrar: ${response.code()}")
                }
            } catch (e: Exception) {
                println("Fallo de red al intentar borrar la cita: ${e.message}")
            }
        }
    }

    // ✏️ --- ACTUALIZAR CITA EN EL SERVIDOR ---
    fun editarCita(citaAntigua: Cita, citaNueva: Cita) {
        viewModelScope.launch {
            try {
                // 1. Enviamos la cita modificada a la nueva ruta PUT de Retrofit
                val response = RetrofitClient.apiService.actualizarCita(citaAntigua.idCita, citaNueva)

                if (response.isSuccessful) {
                    // 2. Si el servidor dice OK, actualizamos la pantalla
                    val index = listaCitas.indexOfFirst { it.idCita == citaAntigua.idCita }
                    if (index != -1) {
                        listaCitas[index] = citaNueva
                    }
                    println("Cita actualizada en el servidor correctamente.")
                } else {
                    println("Error del servidor al actualizar: ${response.code()}")
                }
            } catch (e: Exception) {
                println("Fallo de red al intentar actualizar la cita: ${e.message}")
            }
        }
    }
}