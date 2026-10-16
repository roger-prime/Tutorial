package com.example.campusreport.data;
public final class Report {
    public final String uuid, lugar, categoria, descripcion, fecha;
    public final boolean sincronizado;
    public Report(String uuid, String lugar, String categoria, String descripcion, String fecha, boolean sincronizado) {
        this.uuid=uuid; this.lugar=lugar; this.categoria=categoria;
        this.descripcion=descripcion; this.fecha=fecha; this.sincronizado=sincronizado;
    }
}
