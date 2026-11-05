# Complementos de entrega — CampusReport 0.4.0

Integrantes: Johan Escurra y Roger Báez. Rama: `campusreport-integracion-final`.

## Documentación y diagramas disponibles

- [Documento técnico actualizado (Word)](CampusReport_Documentacion_Tecnica_Actualizada.docx) — archivo principal, cuatro páginas.
- [Documento técnico (Markdown)](CampusReport_Documentacion_Tecnica_Actualizada.md) — versión textual navegable en GitHub.
- [Matriz de cobertura (Word)](CampusReport_Matriz_y_Capturas_Pendientes.docx) — anexo de funciones y evidencias requeridas. Fue creado antes de subir las capturas; el estado vigente está en la versión Markdown y en el índice visual.
- [Matriz de cobertura actualizada (Markdown)](CampusReport_Matriz_y_Capturas_Pendientes.md).
- Diagramas de SQLite: [PNG](CampusReport_Diagrama_SQLite.png) · [SVG](CampusReport_Diagrama_SQLite.svg) · [Graphviz DOT](CampusReport_Diagrama_SQLite.dot).
- Diagramas de H2: [PNG](CampusReport_Diagrama_H2.png) · [SVG](CampusReport_Diagrama_H2.svg) · [Graphviz DOT](CampusReport_Diagrama_H2.dot).
- [Índice de seis capturas reales del emulador](../capturas/README.md).

Los archivos DOCX, PNG y SVG **ya fueron subidos** a esta carpeta. No es necesario volver a cargarlos.

## Qué ya había elaborado Roger

Los documentos originales `../archivos/CampusReport_Documentacion_Tecnica.docx`, `CampusReport_Pruebas_y_Defensa.docx` y `CampusReport_Presentacion_Final.pptx` se conservan, sin sobrescribir. También se conservan la guía `../../guia_funcionamiento.md`, los scripts de demostración y las evidencias anteriores.

## Estado para entrega

- Hay seis capturas reales de la aplicación: Inicio, Lugares, Categorías, Nuevo reporte, Detalle y Reporte sincronizado.
- No hay capturas separadas del reporte **pendiente** ni del mensaje emergente de **Sincronizar**. No se presentan como verificadas visualmente.
- El servidor documentado es Spring Boot en `127.0.0.1:8080`, accedido desde el emulador mediante `10.0.2.2:8080`. No es despliegue público.
- Existen dos conjuntos de hashes: Roger registró binarios en `../../evidencias/cierre-pruebas.json` y Johan compiló otros en `../../evidencias/compilacion_johan.md`. **El ZIP definitivo debe usar un conjunto coherente de APK, JAR, evidencias y código, no mezclar los hashes**.
- La carpeta `capturas` documenta evidencia visual disponible; no demuestra que todas las pruebas manuales posibles se hayan realizado.

Antes de entregar, revisar paginación real del Word y contenido visual de la presentación, y formar un paquete con los archivos definitivos.
