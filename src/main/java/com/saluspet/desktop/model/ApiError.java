package com.saluspet.desktop.model;

/**
 * Entidad que mapea la respuesta de error de la API (ej. { "mensaje": "Contraseña Inválida" })
 */
public class ApiError {
    private String mensaje;

    public ApiError() {
    }

    public String getMensaje() {
        return mensaje;
    }
}
