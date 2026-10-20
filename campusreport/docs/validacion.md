# Validación de CampusReport

Actualización: 20 de octubre de 2026. Versión: 0.2.0.

## Comprobaciones ejecutadas

| Comprobación | Resultado |
|---|---|
| Compilación y empaquetado del backend con Maven | BUILD SUCCESS; JAR 0.2.0 generado |
| Pruebas de integración del backend | 4 ejecutadas, 0 fallos, 0 errores |
| Compilación Android assembleDebug | APK de depuración generado |
| Pruebas locales Android con Robolectric y SQLite nativo | 4 ejecutadas, 0 fallos, 0 errores |
| Revisión de documentos Word | Definición y prototipo de tres páginas cada uno, renderizados y revisados visualmente |

Las cuatro pruebas del backend verifican el contrato del catálogo, la lectura de una fila nueva desde la base, el endpoint de categorías y las claves foráneas de reportes.

Las cuatro pruebas locales Android verifican:

- Creación de lugares y categorías sin descarga previa, rechazo de nombres repetidos, persistencia al reabrir y conservación tras actualizar dos veces el catálogo.
- Guardado anónimo: el nombre queda NULL aunque se proporcione un valor a la función.
- Guardado con nombre: validación, normalización y lectura del autor.
- Migración de SQLite de versión 1 a 2: conserva el reporte anterior como anónimo y admite los nuevos registros locales.

Se ejecutaron con JUnit 4.13.2, Robolectric 4.14.1 y SQLite nativo en SDK 28. Las pruebas usan una base independiente de la aplicación. Las evidencias de HTTP guardadas anteriormente en `evidencias/` corresponden a la revisión del 16 de octubre; no se presentan como una nueva ejecución.

## Revisión en el emulador del equipo

No se ejecutó un emulador Android durante esta actualización. Queda comprobar visualmente:

- Navegar por Inicio, Lugares, Categorías, Nuevo reporte, Reportes y Detalle del reporte.
- Usar Guardar en Lugares y Categorías; crear un reporte anónimo y otro con nombre.
- Activar Reporte anónimo y comprobar que Tu nombre se oculta y se vacía.
- Rotar los formularios y revisar que conservan el borrador.
- Cerrar y abrir la aplicación y consultar los registros.
- Ejecutar Spring Boot, pulsar Actualizar y revisar los datos descargados.
- Revisar los mensajes breves y Logcat.

Las tres pruebas instrumentadas anteriores siguen disponibles mediante `connectedDebugAndroidTest`; no se ejecutaron en esta actualización. El prototipo Word es una referencia editable, no una captura de ejecución.

Ambos integrantes deben comprender y demostrar el funcionamiento, y registrar sus aportes reales en Git. Ver `aportes.md`.
