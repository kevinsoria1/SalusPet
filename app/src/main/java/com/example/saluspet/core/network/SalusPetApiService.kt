package com.example.saluspet.core.network

import com.example.saluspet.features.auth.data.LoginRequest
import com.example.saluspet.features.auth.data.RegisterRequest // Añadimos la importación
import com.example.saluspet.features.auth.data.Usuario
import com.example.saluspet.features.pets.data.Mascota
import com.example.saluspet.features.pets.data.RegisterMascotaRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface SalusPetApiService {

    // 1. Login del Usuario
    @POST("api/auth/login")
    suspend fun loginUsuario(@Body request: LoginRequest): Response<Usuario>

    // 2. Obtener las mascotas del servidor (Esto lo haremos más adelante)
    @GET("api/Mascotas")
    suspend fun getMascotas(): Response<List<Mascota>>

    // 3. Registro de nuevo Usuario
    @POST("api/auth/register")
    suspend fun registrarUsuario(@Body request: RegisterRequest): Response<Any>

    // 4. Registrar una nueva mascota
    // 4. Registrar una nueva mascota
    @POST("api/Mascotas")
    suspend fun registrarMascota(@Body mascota: Mascota): Response<Mascota>

    // 5. Actualizar una mascota existente (Para guardar la foto o editarla)
    @PUT("api/Mascotas/foto/{id}")
    suspend fun actualizarFotoMascota(@Path("id") id: Int, @Body body: Map<String, String?>): retrofit2.Response<Unit>
}