# Validación de la entrega

Fecha de preparación: 16 de octubre de 2026. Versión: 0.1.0-semana1.

## Comprobaciones ejecutadas

| Comprobación | Resultado |
|---|---|
| Compilación y empaquetado del backend con Maven | BUILD SUCCESS; JAR ejecutable generado |
| Pruebas de integración del backend | 4 ejecutadas, 0 fallos, 0 errores |
| Arranque real del JAR con H2 en archivo | Spring Boot iniciado; archivo de base creado |
| GET /api/catalogo | HTTP 200; cuatro lugares y cuatro categorías |
| GET /api/lugares y GET /api/categorias | HTTP 200; cuatro elementos por lista |
| Compilación Android assembleDebug | BUILD SUCCESSFUL; APK de depuración generado |
| Inspección del APK | Paquete com.example.campusreport, API mínima 26, objetivo 35, actividad inicial e INTERNET declarados |
| Revisión de documentos | Definición de tres páginas y prototipo de dos páginas renderizados y revisados visualmente |

Las cuatro pruebas del backend verifican: contrato del catálogo, lectura de una fila nueva desde la base,
endpoint de categorías y existencia de la tabla de reportes con claves foráneas. No usan un servidor falso
para responder con datos constantes. La respuesta de la ejecución HTTP está en `evidencias/catalogo-respuesta.json`.

## Pendiente de comprobar en el emulador del equipo

No se ejecutó un emulador Android durante esta preparación. Compilar el APK no sustituye estas pruebas:

- Abrir Inicio y navegar por todas las pantallas.
- Descargar desde el Spring Boot de la notebook y verificar lugares en pantalla.
- Consultar SQLite desde Database Inspector.
- Crear dos reportes sin conexión; cerrar y abrir la app y verificar persistencia.
- Rotar el formulario y comprobar que conserva el borrador.
- Repetir una descarga y comprobar que no duplica lugares ni elimina reportes.
- Detener el backend e intentar descargar: mostrar error y conservar el catálogo anterior.
- Revisar Toast y Logcat, y tomar capturas reales para la entrega.

Se incluyen tres pruebas instrumentadas de SQLite para ejecutar con `connectedDebugAndroidTest`:
persistencia y descarga repetida; reversión de una descarga inválida; validación de descripción corta.
Estas pruebas usan `campusreport-test.db`, una base independiente de la base de la app.
Las imágenes del prototipo son diseños de referencia, no evidencia de una ejecución Android.

## Requisitos académicos que debe completar el equipo

- Aprobación del alcance por el profesor.
- Demostración en vivo y comprensión del código por ambos integrantes.
- Aportes reales identificables de Roger y Johan en Git. Ver `aportes.md`.

El envío de reportes y la confirmación de sincronización están previstos para la semana 3 y no se
presentan como funcionalidades terminadas de esta versión.
