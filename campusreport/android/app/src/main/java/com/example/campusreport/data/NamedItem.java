package com.example.campusreport.data;
public class NamedItem {
    public final long id;public final String nombre;public final boolean sincronizado;
    public NamedItem(long id,String nombre) {this(id,nombre,id>0);}
    public NamedItem(long id,String nombre,boolean sincronizado) {this.id=id;this.nombre=nombre;this.sincronizado=sincronizado;}
    @Override public String toString() {return nombre;}
}
