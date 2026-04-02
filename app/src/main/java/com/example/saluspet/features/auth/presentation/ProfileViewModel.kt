package com.example.saluspet.features.auth.presentation

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saluspet.core.network.RetrofitClient
import com.example.saluspet.features.auth.data.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel : ViewModel() {

    var usuarioData by mutableStateOf<Usuario?>(null)
        private set

    // ⬇️ DESCARGAR PERFIL
    fun cargarPerfil(context: Context) {
        val sharedPref = context.getSharedPreferences("perfil_saluspet", Context.MODE_PRIVATE)
        val idUsuario = sharedPref.getInt("idUsuario", 0)

        if (idUsuario != 0) {
            viewModelScope.launch {
                try {
                    val response = RetrofitClient.apiService.obtenerPerfilUsuario(idUsuario)
                    if (response.isSuccessful && response.body() != null) {
                        val usuarioBD = response.body()!!
                        usuarioData = usuarioBD

                        sharedPref.edit().apply {
                            putString("nombre", usuarioBD.nombre ?: "")
                            putString("apellidos", usuarioBD.apellidos ?: "")
                            putString("correo", usuarioBD.email ?: "") // ⚠️ Cuidado si se llama email o correo
                            putString("telefono", usuarioBD.telefono ?: "")
                            apply()
                        }
                    } else {
                        println("Error GET Perfil: ${response.code()}")
                    }
                } catch (e: Exception) {
                    println("Fallo de red GET Perfil: ${e.message}")
                }
            }
        }
    }

    // ✏️ ACTUALIZAR PERFIL (Con chivato de errores)
    fun actualizarPerfil(context: Context, usuarioEditado: Usuario) {
        val sharedPref = context.getSharedPreferences("perfil_saluspet", Context.MODE_PRIVATE)
        val idUsuario = sharedPref.getInt("idUsuario", 0)

        if (idUsuario != 0) {
            // Actualización visual rápida
            usuarioData = usuarioEditado

            viewModelScope.launch {
                try {
                    val response = RetrofitClient.apiService.actualizarPerfilUsuario(idUsuario, usuarioEditado)

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Perfil guardado en la nube", Toast.LENGTH_SHORT).show()
                        } else {
                            // 🚨 AQUÍ ESTÁ LA CLAVE: Nos dirá qué falla
                            Toast.makeText(context, "Error del servidor: ${response.code()}", Toast.LENGTH_LONG).show()
                            println("Error PUT Perfil: ${response.errorBody()?.string()}")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Fallo de conexión", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}