# Validación de CampusReport

Fecha: 22 de octubre de 2026. Versión: 0.3.0.

| Comprobación | Resultado |
|---|---|
| Compilación Android | APK debug generado; versionCode 3, versionName 0.3.0, minSdk 26 |
| Robolectric con SQLite nativo | 6 pruebas, 0 fallos, 0 errores |
| Backend Maven | JAR ejecutable generado; 9 pruebas de integración, 0 fallos, 0 errores |
| JAR real y H2 en archivo | GET del catálogo, POST anónimo, reenvío, conflicto y persistencia tras reinicio verificados por HTTP |
| Guía Word | Renderizada y revisada visualmente antes de entregar |

## Casos cubiertos

SQLite: creación local de referencias; descarga repetida sin pérdida; nombre anónimo nulo; nombre validado;
migración de base anterior; catálogo descargado y dos reportes pendientes después de cerrar y reabrir la base;
rechazo de descripción inválida y referencia inexistente; rollback ante catálogo inválido.

Backend: lectura del catálogo desde H2; categorías; claves foráneas; recepción y lectura de reportes;
anonimato; nombre requerido; UUID repetido sin duplicar; conflicto por contenido distinto; validación
de campos y referencias; JSON mal formado y detalle inexistente.

La ejecución real del servidor está registrada en `evidencias/ejecucion-offline.json`.
Las evidencias anteriores del 16 de octubre permanecen identificadas como históricas.

## Ensayo en el equipo

No se ejecutó un emulador Android ni los accesos de Windows en este entorno. Las pruebas de SQLite se
realizaron con Robolectric SDK 28 y motor SQLite nativo, y el APK se inspeccionó con aapt.
El equipo debe ensayar instalación, navegación, formularios y cierre mediante force-stop siguiendo
`guia_funcionamiento.md`. No se presenta el documento como evidencia de una ejecución visual en Android.

La prueba de recepción HTTP guarda en H2; no marca como sincronizados los reportes locales de Android.
