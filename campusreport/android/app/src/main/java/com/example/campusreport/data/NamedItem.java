package com.example.campusreport.data;
public final class NamedItem {
    public final long id;
    public final String nombre;
    public NamedItem(long id, String nombre) { this.id=id; this.nombre=nombre; }
    @Override public String toString() { return nombre; }
}
