package com.saluspet.desktop.utils;

import com.saluspet.desktop.model.Veterinario;

/**
 * Gestor de la sesión local (Patrón Singleton)
 * Evita pasar el objeto a través de constructores por toda la UI.
 */
public class SessionManager {

    private static SessionManager instance;
    private Veterinario veterinarioActual;

    private SessionManager() {
        // constructor privado singleton
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setVeterinarioActual(Veterinario veterinario) {
        this.veterinarioActual = veterinario;
    }

    public Veterinario getVeterinarioActual() {
        return veterinarioActual;
    }

    public void logout() {
        this.veterinarioActual = null;
    }

    public boolean isLoggedIn() {
        return veterinarioActual != null;
    }
}
