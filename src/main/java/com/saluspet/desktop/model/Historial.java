package com.saluspet.desktop.model;

import com.google.gson.annotations.SerializedName;

public class Historial {

    @SerializedName("idHistorial")
    private int idHistorial;

    @SerializedName("idMascota")
    private int idMascota;

    @SerializedName("idVeterinario")
    private int idVeterinario;

    @SerializedName("tipoEvento")
    private String tipoEvento;

    @SerializedName("fecha")
    private String fecha;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("urlDocumento")
    private String urlDocumento;

    public Historial(int idMascota, int idVeterinario, String tipoEvento, String fecha, String descripcion, String urlDocumento) {
        this.idMascota = idMascota;
        this.idVeterinario = idVeterinario;
        this.tipoEvento = tipoEvento;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.urlDocumento = urlDocumento;
    }

    public int getIdHistorial() { return idHistorial; }
    public int getIdMascota() { return idMascota; }
    public int getIdVeterinario() { return idVeterinario; }
    public String getTipoEvento() { return tipoEvento; }
    public String getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    public String getUrlDocumento() { return urlDocumento; }
}
