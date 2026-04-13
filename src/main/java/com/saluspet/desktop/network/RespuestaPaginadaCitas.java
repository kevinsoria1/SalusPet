package com.saluspet.desktop.network;

import com.google.gson.annotations.SerializedName;
import com.saluspet.desktop.model.Cita;
import java.util.List;

public class RespuestaPaginadaCitas {
    @SerializedName("total")
    private int total;
    
    @SerializedName("paginaActual")
    private int paginaActual;
    
    @SerializedName("citas")
    private List<Cita> citas;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPaginaActual() {
        return paginaActual;
    }

    public void setPaginaActual(int paginaActual) {
        this.paginaActual = paginaActual;
    }

    public List<Cita> getCitas() {
        return citas;
    }

    public void setCitas(List<Cita> citas) {
        this.citas = citas;
    }
}
