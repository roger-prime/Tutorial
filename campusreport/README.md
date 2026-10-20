# CampusReport

Aplicación Android en Java para guardar lugares, categorías y reportes de mantenimiento de la facultad.
Equipo: Roger y Johan. Versión 0.2.0, actualización del 20 de octubre de 2026.

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
ni los lugares o categorías creados localmente. El servidor conserva el esquema de reportes con sus
campos de anonimato, pero no expone un POST de recepción. Esta precisión es técnica y no aparece en el prototipo.

## Endpoints

| Método | Ruta | Resultado |
|---|---|---|
| GET | /api/catalogo | Lugares y categorías |
| GET | /api/lugares | Lista de lugares |
| GET | /api/categorias | Lista de categorías |

## Demostración

1. Guardar un lugar y una categoría sin conexión.
2. Guardar un reporte con Reporte anónimo activado y otro indicando un nombre.
3. Consultar ambos desde Reportes y comprobar el autor en Detalle del reporte.
4. Cerrar y abrir la app y comprobar que los registros permanecen guardados.
5. Iniciar Spring Boot, pulsar Actualizar y comprobar que los registros propios se conservan.
6. En Logcat usar `package:mine tag:CampusReport level:DEBUG` para ver los eventos.

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
Servidor compilado: `backend/target/campusreport-backend-0.2.0.jar`.

## Documentación y equipo

`docs/definicion.md` contiene la definición actualizada; `docs/prototipo.md`, los nombres y acciones de las pantallas.
La definición y el prototipo se entregan también en Word para su revisión.
La base se preparó con asistencia de IA. Cada integrante debe realizar y explicar aportes reales en Git;
ver `docs/aportes.md`. No se atribuyen cambios a quien no los realizó.
