package com.example.campusreport.model;
public record Reporte(String uuid, long lugarId, long categoriaId, String descripcion,
                      String fechaCreacion, boolean anonimo, String autor) {}
