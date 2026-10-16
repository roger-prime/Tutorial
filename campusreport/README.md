# CampusReport — entrega de la semana 1

Aplicación Android nativa en **Java** para registrar problemas de mantenimiento en la facultad.
Equipo: **Roger y Johan**. Alcance propuesto para aprobación del docente.

## Qué incluye esta versión

- Cinco pantallas nativas: Inicio, Lugares, Nuevo reporte, Mis reportes y Detalle.
- SQLite local con `lugar`, `categoria` y `reporte`, claves foráneas y UUID por reporte.
- Backend Java 17 / Spring Boot 3.5.6 con **H2 persistente en archivo** como base del servidor.
- `GET /api/catalogo`, `GET /api/lugares` y `GET /api/categorias` consultan la base del servidor y devuelven JSON.
- Descarga HTTP en un hilo de trabajo, guardado transaccional en SQLite y visualización desde SQLite.
- Creación de reportes offline con validación, listado, detalle, Toast y Logcat.
- Catálogo vacío al instalar: los datos de referencia llegan realmente desde Spring Boot.
- Una descarga repetida actualiza los catálogos por ID sin duplicarlos ni borrar reportes.
- Al fallar una descarga se conservan los datos locales. El formulario conserva el borrador al rotar la pantalla.

**Todavía no incluye envío de reportes al servidor.** Los reportes permanecen pendientes. El envío y la
confirmación idempotente mediante UUID se implementarán en la semana 3. No hay edición ni borrado compartido.

## Abrir los proyectos correctos

Esta entrega está dentro de `campusreport/`; el proyecto Tutorial anterior continúa en la raíz del repositorio.
Para trabajar en CampusReport, abrí **`campusreport/android`** en Android Studio y **`campusreport/backend`** en Eclipse.

### 1. Descargar esta rama

Si ya tenés el repositorio clonado, guardá o commiteá primero tus cambios locales y ejecutá:

```powershell
git fetch origin
git switch --track origin/campusreport-semana1
```

Si la rama ya existe localmente: `git switch campusreport-semana1` y luego `git pull --ff-only`.
También podés descargar el ZIP de esta rama desde GitHub con **Code → Download ZIP** y extraerlo.

### 2. Ejecutar Spring Boot

Requisito: JDK **17 o 21**, con `JAVA_HOME` configurado. Es un JDK, no solo un JRE.
Maven se descarga mediante el wrapper incluido; la primera ejecución necesita internet.
Abrí PowerShell dentro de `campusreport/backend`:

```powershell
java -version
.\mvnw.cmd spring-boot:run
```

Esperá `Started CampusReportApplication` y dejá la terminal abierta.
Abrí en el navegador: <http://localhost:8080/api/catalogo>.
El resultado contiene cuatro lugares y cuatro categorías. Ejemplo parcial:

```json
{"lugares":[{"id":1,"nombre":"Aula 1"}],"categorias":[{"id":1,"nombre":"Electricidad"}]}
```

H2 crea `backend/data/campusreport.mv.db` automáticamente. No necesitás instalar MySQL.
`schema.sql` crea las tablas y `data.sql` carga ejemplos solo si sus IDs no existen.
La configuración de desarrollo escucha únicamente en la notebook (`127.0.0.1`).
No se usa una base temporal en memoria fuera de las pruebas.

En **Eclipse**: File → Import → Maven → Existing Maven Projects → seleccionar `backend` → Finish.
Seleccioná un JDK 17 o 21 en Installed JREs. Abrí `CampusReportApplication.java` y ejecutá
Run As → Java Application. No ejecutes a la vez Eclipse y Maven en el mismo puerto.

### 3. Ejecutar Android

1. Android Studio → File → Open → seleccionar **`campusreport/android`**.
2. Esperar Gradle Sync. Si solicita Android SDK 35 o Build Tools 35, aceptar su instalación.
3. Usar JDK 17 o 21 como **Gradle JDK** (Settings → Build, Execution, Deployment → Build Tools → Gradle).
   Este proyecto usa AGP 8.9.2 y Gradle 8.11.1; no usar JDK 25 con esta versión de Gradle.
4. Elegir un emulador con Android 8 / API 26 o superior.
5. Ejecutar la configuración `app` con el botón Run.
6. Dejar Spring Boot abierto. En la app tocar **Descargar lugares y categorías**.
7. Abrir **Lugares disponibles**. Deben aparecer Aula 1, Biblioteca, Laboratorio y Patio.

El emulador usa `http://10.0.2.2:8080`, que apunta a la notebook. `localhost` dentro del emulador
sería el propio teléfono virtual. Esta configuración está preparada para **emulador**, no para
acceso por Wi-Fi desde otro equipo. Para un teléfono USB puede usarse `adb reverse tcp:8080 tcp:8080`
y cambiar `BASE_URL` a `http://127.0.0.1:8080` antes de compilar. Para producción se requiere HTTPS.

## Demostración para el profesor — 4 a 5 minutos

1. Presentar problema, usuarios y alcance con `docs/definicion.md` y el documento PDF.
2. Ejecutar Spring Boot; abrir `/api/catalogo` en el navegador para mostrar el JSON real.
3. Abrir Android. Pulsar Descargar y mostrar el mensaje de éxito; abrir Lugares.
4. Abrir Logcat y filtrar `package:mine tag:CampusReport level:DEBUG`.
5. Apagar Wi-Fi y datos en el emulador (o detener Spring Boot). Volver a Lugares: sigue disponible.
6. Crear un reporte: Laboratorio → Electricidad → “El enchufe junto a la puerta no funciona”.
7. Mostrar el Toast, el detalle y el estado Pendiente de envío. Cerrar y abrir la app; sigue en Mis reportes.
8. Mostrar SQLite: View → Tool Windows → App Inspection → Database Inspector → `campusreport.db`.
   En API 26 o superior, con la app activa, inspeccionar sus tres tablas.
9. Explicar que el envío de pendientes se hará en la semana 3; no confundir guardar localmente con enviar.

## Si algo no conecta

- Primero verificá `http://localhost:8080/api/catalogo` en la notebook.
- Si falla allí, revisar la consola de Spring Boot y si el puerto 8080 está ocupado.
- Si funciona allí pero falla en Android, verificar que sea el emulador de Android Studio y que
  `ApiClient.BASE_URL` siga siendo `http://10.0.2.2:8080`.
- Si Windows pregunta por acceso de red, autorizar solo la red privada de desarrollo cuando corresponda.
- Tras cambiar código, volver a ejecutar Run; el Preview no instala cambios en el emulador.
- “JAVA_HOME incorrecto”: apuntar a la carpeta raíz de un JDK compatible, no a su subcarpeta `bin`.
- Eliminar datos de la app borra los reportes locales: no hacerlo durante la demostración.

## Pruebas y compilación

Backend:
```powershell
cd campusreport\backend
.\mvnw.cmd test package
java -jar target\campusreport-backend-0.1.0.jar
```

Android (desde `campusreport/android`):
```powershell
.\gradlew.bat assembleDebug lintDebug
.\gradlew.bat connectedDebugAndroidTest
```

El último comando requiere emulador. Las pruebas de SQLite usan una base separada de los datos de la app.
APK: `android/app/build/outputs/apk/debug/app-debug.apk`.
Los resultados efectivamente ejecutados se documentan en `docs/validacion.md`.

## Organización y aportes

`android/app/src/main/java/com/example/campusreport`: `ui` (pantallas), `data` (SQLite y modelos),
`network` (HTTP) y `CampusApp` (coordinación de descarga).
`backend/src/main/java/com/example/campusreport/catalog`: controlador REST y repositorio JDBC.

Distribución sugerida para continuar: Roger, Android y SQLite; Johan, Spring Boot y base del servidor.
Ambos deben probar y comprender el flujo completo. Esta distribución no prueba autoría previa.
La base inicial se preparó con asistencia de IA. No se inventan commits ni aportes de los integrantes.
Cada integrante debe realizar y explicar una mejora real desde su cuenta; ver `docs/aportes.md`.

## Referencias técnicas

- Consigna: Entregables del proyecto Android, plan de cuatro semanas, aportada por el docente.
- Spring Boot: https://docs.spring.io/spring-boot/3.5/system-requirements.html
- Android y SQLite: https://developer.android.com/training/data-storage/sqlite
- Red del emulador: https://developer.android.com/studio/run/emulator-networking
- AGP 8.9: https://developer.android.com/build/releases/agp-8-9-0-release-notes
