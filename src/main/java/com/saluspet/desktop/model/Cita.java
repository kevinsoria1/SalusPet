package com.saluspet.desktop.model;

import com.google.gson.annotations.SerializedName;

public class Cita {

    @SerializedName(value = "idCita", alternate = {"IdCita", "id_cita"})
    private int idCita;

    @SerializedName(value = "idMascota", alternate = {"IdMascota", "id_mascota"})
    private int idMascota;

    @SerializedName(value = "tipo", alternate = {"Tipo", "TIPO"})
    private String tipo;

    @SerializedName(value = "titulo", alternate = {"Titulo", "TITULO", "motivo", "Motivo"})
    private String titulo;

    @SerializedName(value = "nombreMascota", alternate = {"mascota", "Mascota"})
    private String nombreMascota;

    @SerializedName(value = "fecha", alternate = {"Fecha"})
    private String fecha;

    @SerializedName(value = "hora", alternate = {"Hora"})
    private String hora;

    @SerializedName(value = "descripcion", alternate = {"Descripcion"})
    private String descripcion;

    @SerializedName(value = "idVeterinario", alternate = {"IdVeterinario", "id_veterinario"})
    private Integer idVeterinario;

    @SerializedName(value = "estado", alternate = {"Estado"})
    private String estado;

    @SerializedName(value = "idMascotaNavigation", alternate = {"IdMascotaNavigation"})
    private Object idMascotaNavigation;

    // Getters rápidos
    public int getIdCita() { return idCita; }
    public int getIdMascota() { return idMascota; }
    public String getNombreMascota() { return nombreMascota; }
    public String getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public String getDescripcion() { return descripcion; }
    public String getEstado() { return estado; }
    public Integer getIdVeterinario() { return idVeterinario; }

    public void setIdVeterinario(Integer idVeterinario) { this.idVeterinario = idVeterinario; }
    public void setEstado(String estado) { this.estado = estado; }
}
