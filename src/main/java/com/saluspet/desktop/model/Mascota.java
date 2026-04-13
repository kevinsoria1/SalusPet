package com.saluspet.desktop.model;

import com.google.gson.annotations.SerializedName;

/**
 * Entidad DTO que mapea el listado de pacientes del backend.
 */
public class Mascota {
    
    @SerializedName(value = "idMascota", alternate = {"IdMascota"})
    private int idMascota;
    
    @SerializedName(value = "idUsuario", alternate = {"IdUsuario"})
    private int idUsuario;
    
    @SerializedName(value = "dueño", alternate = {"Dueño"})
    private String dueño;
    
    @SerializedName(value = "nombre", alternate = {"Nombre"})
    private String nombre;
    
    @SerializedName(value = "especie", alternate = {"Especie"})
    private String especie;
    
    // Tratado como String crudo para no romper el parser por culpa del tipo "DateOnly" asincrónico de C# (.NET)
    @SerializedName(value = "fechaNacimiento", alternate = {"FechaNacimiento"})
    private String fechaNacimiento; 
    
    @SerializedName(value = "peso", alternate = {"Peso"})
    private Double peso;
    
    @SerializedName(value = "genero", alternate = {"Genero"})
    private String genero;
    
    @SerializedName(value = "foto", alternate = {"Foto", "urlFoto"})
    private String urlFoto;
    
    @SerializedName(value = "validada", alternate = {"Validada"})
    private Boolean validada;

    // Getters rápidos
    public int getIdMascota() { return idMascota; }
    public int getIdUsuario() { return idUsuario; }
    public String getDueño() { return dueño; }
    public String getNombre() { return nombre; }
    public String getEspecie() { return especie; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public Double getPeso() { return peso; }
    public String getGenero() { return genero; }
    public String getUrlFoto() { return urlFoto; }
    public Boolean getValidada() { return validada; }
}
