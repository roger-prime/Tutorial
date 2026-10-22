# CampusReport

Aplicación Android en Java para guardar lugares, categorías y reportes de mantenimiento de la facultad.
Equipo: Roger y Johan. Versión 0.3.0, actualización del 22 de octubre de 2026.

## Funcionamiento

- **Lugares**: guardar un nombre y consultar la lista.
- **Categorías**: guardar un nombre y consultar la lista.
- **Nuevo reporte**: elegir lugar y categoría, describir el problema y pulsar **Guardar**.
- **Reporte anónimo**: activado inicialmente. No guarda un nombre de autor. Al desactivarlo, solicita **Tu nombre**.
- **Reportes**: consultar registros y abrir **Detalle del reporte**.
- **Actualizar**: descargar los lugares y categorías de Spring Boot y conservar los datos locales.

Los formularios guardan sin conexión. Se pueden crear lugares y categorías antes de contactar al servidor.
El nombre de cada botón coincide con el título de su sección. Los tres formularios usan **Guardar**.

## Abrir el código

La entrega está en la carpeta `campusreport`, dentro de la rama `campusreport-semana1`.
Abrí `campusreport/android` en Android Studio y `campusreport/backend` en Eclipse.
El ejercicio Tutorial que está en la raíz es un proyecto diferente.

Si ya trabajás en esta rama, guardá tus cambios propios y ejecutá `git pull --ff-only`.
Para traerla por primera vez:

```text
git fetch origin
git switch --track origin/campusreport-semana1
```

## Ejecutar el servidor

Usar JDK 17 o 21. Desde `campusreport/backend`, en PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Esperar `Started CampusReportApplication` y mantener abierta la terminal.
Consultar http://localhost:8080/api/catalogo en el navegador.
H2 crea automáticamente `data/campusreport.mv.db`; no es necesario instalar MySQL.
`schema.sql` crea las tablas y aplica los campos nuevos sin borrar datos; `data.sql` incorpora los ejemplos.

En Eclipse: File → Import → Maven → Existing Maven Projects → seleccionar `backend`.
Ejecutar `CampusReportApplication.java` con Run As → Java Application.
No iniciar simultáneamente otra instancia del servidor en el mismo puerto.

## Ejecutar Android

1. File → Open → `campusreport/android` y esperar Gradle Sync.
2. Usar JDK 17 o 21 como Gradle JDK e instalar SDK 35 si Android Studio lo solicita.
3. Elegir un emulador API 26 o superior y pulsar Run en la configuración `app`.
4. Crear registros en Lugares y Categorías o pulsar Actualizar para incorporar los del servidor.
5. Abrir Nuevo reporte y probar ambas opciones de identificación.

La app del emulador consulta `http://10.0.2.2:8080`, que apunta a la notebook.
La configuración HTTP se limita a los hosts locales de desarrollo. Un despliegue externo necesita HTTPS.
El backend escucha en 127.0.0.1. Esta configuración no es acceso directo desde un teléfono por Wi-Fi.

## Datos y privacidad

SQLite tiene versión 2. La migración conserva lugares, categorías y reportes anteriores; los reportes
antiguos quedan identificados como Anónimo, porque no almacenaban un nombre.
Los lugares y categorías propios tienen ID negativo y UUID estable. Los del servidor usan ID positivo.
Una descarga no borra registros locales ni reportes. Las claves foráneas se mantienen activadas.

Un reporte anónimo guarda `anonimo=1` y `autor=NULL`, aun si la función recibe un nombre.
Al marcar la casilla se vacía el campo de nombre. La app no autentica ni verifica nombres declarados.
La opción oculta el nombre del autor; no elimina datos que la persona escriba voluntariamente en la descripción.
Logcat registra eventos y UUID, sin volcar el nombre del autor ni la descripción.

Actualmente Guardar persiste en el dispositivo. Actualizar descarga referencias: no transmite los reportes
ni los lugares o categorías creados localmente. El servidor ya expone POST /api/reportes para recibir JSON y conservarlo en H2.
El estado **Pendiente de sincronización** permanece en los reportes de Android porque Guardar no llama al POST.
El POST requiere identificadores positivos del catálogo del servidor: los lugares y categorías creados únicamente
en SQLite no se envían con este contrato. Consultar `docs/guia_funcionamiento.md` para el formato y la demostración.

## Endpoints

| Método | Ruta | Resultado |
|---|---|---|
| GET | /api/catalogo | Lugares y categorías |
| GET | /api/lugares | Lista de lugares |
| GET | /api/categorias | Lista de categorías |
| GET | /api/reportes | Reportes recibidos en H2 |
| GET | /api/reportes/{uuid} | Detalle de un reporte recibido |
| POST | /api/reportes | Recibir un reporte JSON con validación |

## Demostración en Windows

El paquete `CampusReport_Demostracion.zip` contiene el APK, el JAR y accesos de Windows.
Las fuentes de los accesos se encuentran en `demo/windows`; los binarios se generan con los comandos de abajo.

1. Extraer el ZIP y abrir un único emulador en Android Studio → Tools → Device Manager.
2. Ejecutar `1_iniciar_servidor.cmd` y esperar `Started CampusReportApplication`.
3. Abrir http://localhost:8080/api/catalogo en Windows.
4. Ejecutar `2_instalar_app.cmd`; en CampusReport pulsar **Actualizar** y consultar **Lugares** y **Categorías**.
5. Desactivar la red de Windows y detener el servidor con Ctrl+C. Apagar solo Wi-Fi no corta la conexión local.
6. Pulsar **Actualizar**: aparece el mensaje de error y se conservan las listas.
7. Crear dos reportes desde **Nuevo reporte**, uno anónimo y otro con nombre. Ambos quedan **Pendiente de sincronización**.
8. Ejecutar `3_cerrar_app.cmd` y después `4_abrir_app.cmd`. Volver a **Reportes** y revisar los dos detalles.

Los accesos usan Java y Android SDK Platform-Tools ya instalados. No borran los datos de la app.
Si una firma distinta impide actualizar la instalación, usar un emulador nuevo y conservar el anterior.
Guía completa: `docs/guia_funcionamiento.md`. JSON de ejemplo: `demo/reporte_ejemplo.json`.

## Recepción JSON

ReportService valida UUID, referencias, descripción, fecha, anonimato y autor. ReportRepository persiste
el reporte en H2. Un nuevo UUID devuelve 201; un reenvío con el mismo contenido devuelve 200; el mismo UUID
con contenido diferente devuelve 409. Los campos inválidos devuelven 400 y un detalle inexistente, 404.
La recepción anónima descarta el nombre recibido. La base del servidor es persistente en archivo.

## Pruebas

```powershell
# Dentro de campusreport/backend
.\mvnw.cmd test package
# Dentro de campusreport/android
.\gradlew.bat assembleDebug testDebugUnitTest
# Con un emulador conectado
.\gradlew.bat connectedDebugAndroidTest
```

Las pruebas locales de SQLite cubren guardado, persistencia, descarga repetida, anonimato y migración.
Se usa una base de prueba separada. Los resultados ejecutados se registran en `docs/validacion.md`.
APK: `android/app/build/outputs/apk/debug/app-debug.apk`.
Servidor compilado: `backend/target/campusreport-backend-0.3.0.jar`.

## Documentación y equipo

`docs/definicion.md` contiene la definición actualizada; `docs/prototipo.md`, los nombres y acciones de las pantallas.
`docs/guia_funcionamiento.md` explica el funcionamiento actual y la demostración; se entrega también en Word.
La base se preparó con asistencia de IA. Cada integrante debe realizar y explicar aportes reales en Git;
ver `docs/aportes.md`. No se atribuyen cambios a quien no los realizó.
