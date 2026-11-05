# CampusReport

Aplicación Android para guardar y sincronizar lugares, categorías y reportes de mantenimiento.
Roger Báez y Johan Escurra · Versión 0.4.0.

## Ruta de entrega auditada (Johan · 5/11/2026)

Esta rama conserva **el código funcional 0.4.0 de Roger**, con documentación y evidencias adicionales de Johan. El paquete actual de Johan se crea con `demo/windows/crear_entrega_johan.ps1`, no con `demo/package_final.py` (este último exige los hashes históricos de los binarios de Roger).

En Windows, desde el directorio `Tutorial`, ejecutar:

```powershell
git switch campusreport-integracion-final
git pull --ff-only origin campusreport-integracion-final
powershell -ExecutionPolicy Bypass -File .\campusreport\demo\windows\crear_entrega_johan.ps1
```

El script prepara `Documentos/CampusReport_Entrega_Final_0_4_0_Auditada.zip`, valida los SHA-256 de los ejecutables **dentro del ZIP** y agrega una aclaración de las diferencias frente a los registros históricos de Roger. No modifica la app ni borra bases de datos.

El documento técnico corregido de cuatro páginas y la matriz de cobertura están en `docs/cierre/complementos`. Hay seis capturas auténticas en `docs/cierre/capturas`. No hay capturas separadas de reporte pendiente ni del mensaje de sincronización.

## Entrega final
La versión estable conserva el APK y el JAR 0.4.0. El 4 de noviembre se ejecutaron
17 pruebas Android, 13 del backend y, por separado, 8 comprobaciones HTTP del servidor.
El ensayo en Windows y las evidencias individuales corresponden a los integrantes.

- [Documentos Word y presentación PowerPoint](docs/cierre/archivos).
- [Documentación técnica y configuración de H2](docs/cierre/documentacion_tecnica.md).
- [Resultados, demostración de 15 minutos y defensa individual](docs/cierre/pruebas_y_defensa.md).
- [Evidencia por prueba y huellas de los binarios](docs/evidencias/cierre-pruebas.json).
- [Resultados del servidor ejecutable](docs/evidencias/cierre-http.json).

Para reproducir el paquete final desde un clon con historial:
`python demo/package_final.py --demo CampusReport_Demostracion.zip --output CampusReport_Entrega_Final.zip`.

## Funcionalidades

- **Lugares** y **Categorías**: consultar listas y **Guardar** registros sin conexión.
- **Nuevo reporte**: elegir referencias, escribir una descripción y elegir si será anónimo.
- **Reportes** y **Detalle del reporte**: consultar SQLite y ver **Pendiente de sincronización** o **Sincronizado**.
- **Sincronizar**: enviar lugares y categorías pendientes, enviar reportes y descargar novedades.
- Las pantallas leen SQLite y siguen funcionando sin servidor. Guardar nunca requiere internet.

## Garantías de sincronización

Cada nuevo registro recibe un UUID que se conserva en los reintentos. El servidor devuelve 201 al
crear y 200 si ya recibió ese UUID con el mismo contenido; devuelve 409 ante contenido diferente.
Android valida la confirmación antes de cambiar el estado. Una respuesta perdida, un rechazo o
un error de red conserva el pendiente. Los éxitos previos no se revierten por un fallo posterior.

SQLite versión 3 agrega `server_id` a lugares y categorías sin eliminar datos. Los IDs locales
negativos permanecen estables; `server_id` permite enviar sus referencias positivas. Cuando un
lugar o categoría ya existe en el servidor, se reutiliza su ID y se reasignan las referencias
locales en una transacción. Las descargas actualizan por ID e incorporan reportes por UUID,
sin sustituir reportes pendientes. Se descargan todas las filas para este volumen académico.

La sincronización es manual, desde **Sincronizar**, con una sola ejecución a la vez. La versión
integra altas y consultas; no implementa edición/borrado de reportes ni resolución de ediciones
concurrentes. Los datos compartidos del servidor son visibles para los usuarios de esta demostración.
No hay autenticación ni verificación de los nombres declarados.

## Ejecutar en Windows

El paquete `CampusReport_Demostracion.zip` incluye APK, JAR, guía Word y accesos numerados.
Extraerlo completo. Iniciar un único emulador ya instalado en Android Studio → Tools → Device Manager.
Ejecutar `1_iniciar_servidor.cmd`, esperar `Started CampusReportApplication`, y ejecutar
`2_instalar_app.cmd`. Pulsar **Sincronizar** en CampusReport.

Si existe una base anterior del servidor, detener ambos servidores y copiar la carpeta `data`
completa al nuevo paquete antes de iniciarlo. Conservar el original. El acceso opcional
`0_respaldar_datos_android.cmd` copia SQLite antes de actualizar la app.
Si aparece `UPDATE_INCOMPATIBLE`, conservar el emulador anterior y crear otro con la misma
imagen ya descargada. No desinstalar ni usar Wipe Data sobre datos que se quieran conservar.

Guía paso a paso: [guia_funcionamiento.md](docs/guia_funcionamiento.md).

## Abrir las fuentes

Repositorio `roger-prime/Tutorial`, rama `campusreport-semana1`.
Abrir **campusreport/android** en Android Studio; el Tutorial de la raíz es otro proyecto.
Importar **campusreport/backend** como Maven en Eclipse o abrirlo en otro IDE Java.
El IDE no cambia el backend: es Spring Boot con Java y H2 persistente.
Guardar o commitear cambios propios antes de actualizar con `git pull --ff-only`.

Backend, desde su carpeta y con JDK 17 o 21:

```powershell
.\mvnw.cmd test package
.\mvnw.cmd spring-boot:run
```

Android, desde su carpeta:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest
```

La compilación usa SDK 35. Esto no obliga a cambiar el emulador; admite API 26 o superior.
APK: `android/app/build/outputs/apk/debug/app-debug.apk`.
JAR: `backend/target/campusreport-backend-0.4.0.jar`.
Para crear el paquete: `python demo/package.py --guide RUTA_A_LA_GUIA.docx`.

## API REST

| Método | Ruta | Resultado |
|---|---|---|
| GET | /api/catalogo | Lugares y categorías |
| GET / POST | /api/lugares | Consultar / guardar lugar con UUID |
| GET / POST | /api/categorias | Consultar / guardar categoría con UUID |
| GET / POST | /api/reportes | Consultar / guardar reporte con UUID |
| GET | /api/reportes/{uuid} | Detalle del reporte |

Windows: `http://localhost:8080`. Emulador: `http://10.0.2.2:8080`.
El servidor escucha en 127.0.0.1. Es una configuración local de demostración, no un despliegue
público ni una conexión directa desde un teléfono físico. La persistencia H2 está en `data/campusreport.mv.db`.

## Pruebas y demostración

Descargar, desconectar y detener el servidor, crear tres reportes, cerrar y abrir la app,
reconectar y sincronizar, verificar en el servidor, repetir sin duplicaciones y crear un
reporte en el servidor con `6_crear_dato_servidor.cmd` para descargarlo a SQLite.

Las pruebas cubren SQLite nativo con Robolectric, migraciones, reintentos, errores, confirmaciones,
descargas transaccionales y backend. La prueba HTTP integrada de Android requiere configurar
`CAMPUS_BACKEND_JAR` con la ruta absoluta del JAR compilado; sin esa variable se omite explícitamente.
`demo/verify_http.py` prueba HTTP y persistencia tras reiniciar el JAR usando una base temporal.
Resultados: [validacion.md](docs/validacion.md).

La documentación describe el código actual. El proyecto se desarrolla con asistencia de IA;
cada integrante debe registrar y explicar sus aportes reales en Git.
