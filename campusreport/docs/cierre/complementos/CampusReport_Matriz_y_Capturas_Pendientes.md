# CampusReport — Matriz de cobertura y evidencia visual

Control actualizado de la versión 0.4.0. Incluye **seis capturas reales ya subidas** en [capturas](../capturas/README.md). El documento Word de matriz original fue creado antes del registro de esas imágenes y conserva la lista inicial de comprobaciones.

| Pantalla / acciones | Documento técnico | Evidencia actual |
| --- | --- | --- |
| Inicio: navegación y botón Sincronizar | §3 y §6 | ✅ [01_inicio.png](../capturas/01_inicio.png): pantalla de inicio; el mensaje específico de sincronización no se capturó |
| Lugares: Nombre / Guardar / Volver | §3 y §4 | ✅ [02_lugares.png](../capturas/02_lugares.png) |
| Categorías: Nombre / Guardar / Volver | §3 y §4 | ✅ [03_categorias.png](../capturas/03_categorias.png) |
| Nuevo reporte: Lugar / Categoría / Descripción | §3 y §6 | ✅ [04_nuevo_reporte.png](../capturas/04_nuevo_reporte.png) |
| Reporte anónimo / Tu nombre | §3 y §6 | ⚠️ Funciones documentadas; la captura del formulario no acredita por sí sola ambos modos |
| Reportes: lista y estados | §3 y §6 | ✅ [08_reporte_sincronizado.png](../capturas/08_reporte_sincronizado.png); ⚠️ falta captura separada del estado pendiente |
| Detalle: campos, fecha y estado | §3 y §6 | ✅ [06_detalle_reporte.png](../capturas/06_detalle_reporte.png) |

## Pruebas automáticas y otras fuentes

`docs/evidencias/cierre-pruebas.json` registra pruebas Android y backend; `docs/evidencias/integracion_http_johan.md` documenta una prueba HTTP integrada aprobada. Las pruebas funcionales y la experiencia visual del emulador son fuentes complementarias, no equivalentes.

## Pendientes conocidos aceptados para esta entrega

- `05_reporte_pendiente.png` no existe en el repositorio.
- `07_sincronizacion.png` no existe en el repositorio.
- No indicar en la presentación ni en el documento que se adjuntaron ocho capturas.
- No sustituir las capturas faltantes con ilustraciones o imágenes de otra versión.

## Revisión final

- Verificar que el documento técnico principal siga dentro del límite de cinco páginas.
- Revisar que los dos diagramas coincidan con los esquemas SQLite v3 y H2 0.4.0.
- Mantener coherencia entre binarios y hashes antes de empaquetar: los hashes originales de Roger difieren de los registrados por Johan.
- Mantener las limitaciones reales del sistema: sin autenticación, edición de reportes ni sincronización periódica y servidor de desarrollo en `127.0.0.1:8080`.

Documentación base: `campusreport-semana1` (commit f6293bb) y `campusreport-integracion-final`. 
