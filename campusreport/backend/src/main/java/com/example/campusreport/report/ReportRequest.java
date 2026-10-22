package com.example.campusreport.report;
/** Campos requeridos de un reporte enviado como JSON. Boolean permite detectar anonimato ausente. */
public record ReportRequest(String uuid, Long lugarId, Long categoriaId, String descripcion,
                            String fechaCreacion, Boolean anonimo, String autor) {}
