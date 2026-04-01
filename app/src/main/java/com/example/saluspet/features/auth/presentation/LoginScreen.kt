package com.example.saluspet.features.auth.presentation

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saluspet.R
import com.example.saluspet.core.network.RetrofitClient
import com.example.saluspet.features.auth.data.LoginRequest
import com.example.saluspet.features.auth.data.RegisterRequest
import com.example.saluspet.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current // ⬅️ Necesario para acceder a la memoria del teléfono

    var isLoginMode by remember { mutableStateOf(true) }
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = PastelBlueBackgroundLighter)
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // --- LOGO ---
        Image(
            painter = painterResource(id = R.drawable.logo_saluspet1),
            contentDescription = "Logo SalusPet",
            modifier = Modifier.height(120.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (isLoginMode) "Bienvenido a SalusPet" else "Crea tu cuenta",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextColorDark
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isLoginMode) "Inicia sesión para continuar" else "Regístrate para empezar",
            fontSize = 14.sp,
            color = TextColorGray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- MENSAJES DE ERROR / ÉXITO ---
        errorMessage?.let {
            Text(text = it, color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(16.dp))
        }
        successMessage?.let {
            Text(text = it, color = PastelGreenPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- FORMULARIO ---
        if (!isLoginMode) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre completo") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = PastelGreenPrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = PastelGreenPrimary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = PastelGreenPrimary) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        if (!isLoginMode) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Repetir Contraseña") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = PastelGreenPrimary) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- BOTÓN PRINCIPAL ---
        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    errorMessage = null
                    successMessage = null

                    try {
                        if (isLoginMode) {
                            // LÓGICA DE LOGIN
                            if (email == "admin" && password == "admin") {
                                onLoginSuccess() // Puerta trasera para presentaciones
                            } else {
                                val response = RetrofitClient.apiService.loginUsuario(LoginRequest(email, password))
                                if (response.isSuccessful) {
                                    // 👇 GUARDAMOS EL ID DEL USUARIO PARA LAS MASCOTAS
                                    val usuarioLogueado = response.body()
                                    if (usuarioLogueado != null) {
                                        val sharedPreferences = context.getSharedPreferences("perfil_saluspet", Context.MODE_PRIVATE)
                                        sharedPreferences.edit().putInt("idUsuario", usuarioLogueado.idUsuario).apply()
                                    }
                                    onLoginSuccess()
                                } else {
                                    errorMessage = "Correo o contraseña incorrectos"
                                }
                            }
                        } else {
                            // LÓGICA DE REGISTRO
                            if (nombre.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                                if (password == confirmPassword) {
                                    val requestRegistro = RegisterRequest(
                                        nombre = nombre,
                                        apellidos = " ",
                                        telefono = " ",
                                        email = email,
                                        password = password
                                    )
                                    val response = RetrofitClient.apiService.registrarUsuario(requestRegistro)

                                    if (response.isSuccessful) {
                                        successMessage = "¡Cuenta creada! Ya puedes iniciar sesión."
                                        isLoginMode = true
                                        password = ""
                                        confirmPassword = ""
                                    } else {
                                        errorMessage = "Error al registrar la cuenta. Verifica los datos."
                                    }
                                } else {
                                    errorMessage = "Las contraseñas no coinciden"
                                }
                            } else {
                                errorMessage = "Por favor, rellena todos los campos"
                            }
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error de conexión con el servidor"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PastelGreenPrimary),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    text = if (isLoginMode) "Entrar" else "Crear Cuenta",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- CAMBIAR MODO ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isLoginMode) "¿No tienes cuenta? " else "¿Ya tienes cuenta? ",
                color = TextColorGray,
                fontSize = 14.sp
            )
            Text(
                text = if (isLoginMode) "Regístrate aquí" else "Inicia sesión",
                color = PastelGreenPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    isLoginMode = !isLoginMode
                    errorMessage = null
                    successMessage = null
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}