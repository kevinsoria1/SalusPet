package com.saluspet.desktop.model;

/**
 * Entidad principal devuelta por el Backend tras el Login exitoso.
 */
public class Veterinario {
    @com.google.gson.annotations.SerializedName(value = "idVeterinario", alternate = {"IdVeterinario", "idUsuario", "IdUsuario", "id", "Id"})
    private int idVeterinario;

    @com.google.gson.annotations.SerializedName(value = "nombre", alternate = {"Nombre"})
    private String nombre;

    @com.google.gson.annotations.SerializedName(value = "especialidad", alternate = {"Especialidad"})
    private String especialidad;

    @com.google.gson.annotations.SerializedName(value = "rol", alternate = {"Rol"})
    private String rol;

    // Requerido por Gson
    public Veterinario() {
    }

    public Veterinario(int idVeterinario, String nombre, String especialidad, String rol) {
        this.idVeterinario = idVeterinario;
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.rol = rol;
    }

    public int getIdVeterinario() {
        return idVeterinario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public String getRol() {
        return rol;
    }
}
