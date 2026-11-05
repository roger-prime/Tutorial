# CampusReport — Matriz de cobertura y capturas pendientes

Anexo de control de la versión 0.4.0. Relaciona las acciones reales de Android con el documento técnico y define las capturas necesarias. **No contiene imágenes simuladas:** las capturas deben provenir del emulador de la versión entregada.

| Pantalla / acciones | Apartado documento | Evidencia requerida |
| --- | --- | --- |
| Inicio: navegación y Sincronizar | §3 y §6 | Inicio con botones y contadores; mensaje tras sincronizar |
| Lugares: Nombre / Guardar / Volver | §3 y §4 | Pantalla con lugar de ejemplo y formulario |
| Categorías: Nombre / Guardar / Volver | §3 y §4 | Pantalla con categoría y formulario |
| Nuevo reporte: Lugar / Categoría / Descripción | §3 y §6 | Formulario antes de guardar |
| Reporte anónimo / Tu nombre | §3 y §6 | Formulario con opción anónima y otro con autor |
| Reportes: lista y estados | §3 y §6 | Reporte pendiente y sincronizado |
| Detalle: campos, fecha y estado | §3 y §6 | Detalle de un reporte guardado y reabierto |

Todas las capturas deben provenir del mismo APK y del mismo commit fuente de la entrega. No reutilizar capturas antiguas como evidencia de la versión final.

## Comprobaciones esenciales antes de entregar

- Documento técnico principal de cinco páginas como máximo.
- Los dos diagramas coinciden con SQLite v3 y H2 de la 0.4.0.
- Cada botón o control visible queda explicado en el documento o manual.
- Las capturas reales incluyen un caso pendiente y otro sincronizado.
- El informe distingue pruebas automáticas de ensayos visuales.
- APK y JAR que se entreguen coinciden con las huellas SHA-256 registradas.
- No afirmar alojamiento público o conexión remota si el servidor sigue en 127.0.0.1.

## Fuentes utilizadas

Repositorio roger-prime/Tutorial, rama campusreport-semana1 (commit f6293bb); rama campusreport-integracion-final; CampusDb.java, schema.sql, CatalogController.java, SyncManager.java, MainActivity.java y Activities asociadas; documentación de cierre de Roger.
