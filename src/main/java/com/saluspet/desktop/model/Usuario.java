package com.saluspet.desktop.model;

import com.google.gson.annotations.SerializedName;

public class Usuario {

    @SerializedName(value = "idUsuario", alternate = {"IdUsuario", "id_usuario"})
    private int idUsuario;

    @SerializedName(value = "nombre", alternate = {"Nombre"})
    private String nombre;

    @SerializedName(value = "apellidos", alternate = {"Apellidos"})
    private String apellidos;

    @SerializedName(value = "telefono", alternate = {"Telefono"})
    private String telefono;

    @SerializedName(value = "email", alternate = {"Email"})
    private String email;

    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }

    public String getNombreCompleto() {
        String fullName = (nombre != null ? nombre : "") + " " + (apellidos != null ? apellidos : "");
        return fullName.trim();
    }
}
