package com.example.campusreport.data;
public final class Report {
    public final String uuid, lugar, categoria, descripcion, fecha, autor;
    public final boolean sincronizado, anonimo;
    public Report(String uuid, String lugar, String categoria, String descripcion, String fecha,
                  boolean sincronizado, boolean anonimo, String autor) {
        this.uuid=uuid;this.lugar=lugar;this.categoria=categoria;this.descripcion=descripcion;
        this.fecha=fecha;this.sincronizado=sincronizado;this.anonimo=anonimo;
        this.autor=anonimo?null:autor;
    }
    public String autorVisible() { return anonimo?"Anónimo":autor; }
}
