package com.example.saluspet.core.network

import com.example.saluspet.features.auth.data.LoginRequest
import com.example.saluspet.features.auth.data.RegisterRequest
import com.example.saluspet.features.auth.data.Usuario
import com.example.saluspet.features.calendar.data.Cita
import com.example.saluspet.features.calendar.data.ClinicaCercana
import com.example.saluspet.features.calendar.data.Veterinario
import com.example.saluspet.features.clinics.data.HistorialClinico
import com.example.saluspet.features.pets.data.Mascota
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface SalusPetApiService {

    // ==========================================
    // 👤 USUARIOS / AUTH
    // ==========================================
    @POST("api/auth/login")
    suspend fun loginUsuario(@Body request: LoginRequest): Response<Usuario>

    @POST("api/auth/register")
    suspend fun registrarUsuario(@Body request: RegisterRequest): Response<Any>


    // ==========================================
    // 🐾 MASCOTAS
    // ==========================================
    @GET("api/Mascotas/usuario/{idUsuario}")
    suspend fun getMascotas(@Path("idUsuario") idUsuario: Int): Response<List<Mascota>>

    @POST("api/Mascotas")
    suspend fun registrarMascota(@Body mascota: Mascota): Response<Mascota>

    @PUT("api/Mascotas/foto/{id}")
    suspend fun actualizarFotoMascota(@Path("id") id: Int, @Body body: Map<String, String?>): Response<Unit>

    // 🆕 NUEVO: Editar toda la información de una mascota
    @PUT("api/Mascotas/{id}")
    suspend fun actualizarMascota(@Path("id") id: Int, @Body mascota: Mascota): Response<Unit>

    // 🆕 NUEVO: Eliminar una mascota
    @DELETE("api/Mascotas/{id}")
    suspend fun eliminarMascota(@Path("id") id: Int): Response<Unit>


    // ==========================================
    // 📅 CITAS (AGENDA)
    // ==========================================
    @GET("api/Citas/mascota/{idMascota}")
    suspend fun obtenerCitasMascota(@Path("idMascota") idMascota: Int): Response<List<Cita>>

    @GET("api/Citas/usuario/{idUsuario}")
    suspend fun obtenerCitasUsuario(@Path("idUsuario") idUsuario: Int): Response<List<Cita>>

    @GET("veterinarios")
    suspend fun getVeterinarios(): List<Veterinario>
    @POST("api/Citas")
    suspend fun registrarCita(@Body cita: Cita): Response<Cita>

    @PUT("api/Citas/{id}")
    suspend fun actualizarCita(@Path("id") idCita: Int, @Body cita: Cita): Response<Unit>

    // 🆕 NUEVO: Borrar una cita de la agenda
    @DELETE("api/Citas/{id}")
    suspend fun eliminarCita(@Path("id") idCita: Int): Response<Unit>
    // Dentro de SalusPetApiService.kt
    @GET("clinics") // O la ruta que hayas puesto en tu NestJS
    suspend fun getClinicas(): retrofit2.Response<List<ClinicaCercana>>

    // ==========================================
    // 🏥 HISTORIAL CLÍNICO
    // ==========================================
    @GET("api/HistorialClinico/mascota/{idMascota}")
    suspend fun obtenerHistorialMascota(@Path("idMascota") idMascota: Int): Response<List<HistorialClinico>>

    // ==========================================
    // 👤 PERFIL DE USUARIO
    // ==========================================
    @GET("api/Usuarios/{id}")
    suspend fun obtenerPerfilUsuario(@Path("id") id: Int): Response<Usuario>

    @PUT("api/Usuarios/{id}")
    suspend fun actualizarPerfilUsuario(@Path("id") id: Int, @Body usuario: Usuario): Response<Unit>

}