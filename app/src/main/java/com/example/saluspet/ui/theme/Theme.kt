package com.example.saluspet.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Tema Claro (El que usaremos)
private val LightColorScheme = lightColorScheme(
    primary = PastelGreenPrimary,
    onPrimary = TextColorDark,
    background = PastelBlueBackgroundLighter, // <-- ¡Aquí aplicamos el fondo azul claro!
    surface = PastelBlueBackgroundLighter,    // La superficie también usa el mismo fondo
    onBackground = TextColorDark,
    onSurface = TextColorDark
)

// Tema Oscuro (Opcional por ahora)
private val DarkColorScheme = darkColorScheme(
    primary = PastelGreenDark,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
)

@Composable
fun SalusPetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Forzamos el tema claro para que se vea siempre pastel como quieres
    // Si quieres que cambie automático, usa: if (darkTheme) DarkColorScheme else LightColorScheme
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}