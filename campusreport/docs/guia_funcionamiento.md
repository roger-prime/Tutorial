# CampusReport funcionamiento y demostración

Proyecto Práctico TIC · Roger Báez y Johan Escurra · Versión 0.3.0 · 22 de octubre de 2026

CampusReport permite registrar problemas de mantenimiento de la facultad y consultarlos desde el teléfono. Los lugares, las categorías y los reportes se guardan en SQLite. Una vez descargados los datos, la consulta y la creación de reportes funcionan aunque el servidor esté detenido y no haya internet.

Esta guía explica las funciones, la organización del código y la preparación de una demostración desde Windows con el emulador de Android Studio. La entrega del viernes 23 de octubre consiste en descargar datos, desconectar, crear dos reportes y comprobar que siguen guardados después de cerrar y abrir la aplicación.

## Funcionalidades de la aplicación

| Pantalla | Acciones |
| --- | --- |
| Inicio | Ver el resumen y abrir Nuevo reporte, Reportes, Lugares o Categorías. Actualizar descarga los datos del servidor. |
| Lugares | Escribir Nombre, pulsar Guardar y consultar los lugares almacenados. |
| Categorías | Escribir Nombre, pulsar Guardar y consultar las categorías almacenadas. |
| Nuevo reporte | Elegir Lugar y Categoría, escribir Descripción, elegir Reporte anónimo y pulsar Guardar. |
| Reportes | Consultar la lista y abrir Detalle del reporte. Crear otro mediante Nuevo reporte. |
| Detalle del reporte | Consultar lugar, categoría, descripción, autor, fecha y estado. |

## Etiquetas comunes

Cada sección mantiene el mismo nombre en su botón y en su título. Los formularios de Lugares, Categorías y Nuevo reporte usan Guardar. Actualizar siempre descarga información y Volver regresa a la pantalla anterior.

## Reporte anónimo o con nombre

Reporte anónimo está activado inicialmente. Al desactivarlo aparece Tu nombre. Al activarlo de nuevo, ese campo se vacía y se oculta. Un reporte anónimo guarda autor nulo; el detalle muestra Anónimo. El nombre indicado es una declaración del usuario y no requiere una cuenta.

# Guardado local y organización

## Cómo circulan los datos

Actualizar consulta GET /api/catalogo. La respuesta JSON se incorpora a SQLite dentro de una transacción. Las pantallas consultan SQLite para mostrar los datos. Guardar valida el formulario y escribe el reporte directamente en SQLite, sin hacer una solicitud al servidor.

| Componente | Responsabilidad |
| --- | --- |
| Android Java | Activities, formularios, navegación, mensajes y validación de campos. |
| CampusDb y SQLite | Lugares, categorías y reportes persistentes en el dispositivo. |
| ApiClient y CampusApp | Descarga fuera del hilo principal y actualización del resultado en la pantalla. |
| Spring Boot y JDBC | Controladores REST, servicio de reportes, repositorios y base H2 persistente. |

## Datos y validación

Lugar y Categoría tienen identificador y nombre. Los registros creados en el dispositivo también reciben un UUID y usan identificadores negativos para no coincidir con los identificadores positivos del servidor.

Reporte conserva UUID, lugar, categoría, descripción, fecha de creación, anonimato, autor y sincronizado. Las claves foráneas impiden guardar un reporte con un lugar o una categoría inexistentes.

• Nombre de lugar o categoría: entre 3 y 80 caracteres; se rechaza un nombre ya guardado, sin distinguir mayúsculas y minúsculas.

• Descripción: entre 10 y 500 caracteres después de quitar espacios al principio y al final.

• Tu nombre: entre 2 y 80 caracteres cuando Reporte anónimo está desactivado.

• Si falta un lugar o una categoría, la aplicación ofrece entrar en Lugares y Categorías para crearlos.

## Pendiente de sincronización

Cada reporte nuevo se guarda con sincronizado = 0. En Reportes y Detalle del reporte se muestra Pendiente de sincronización. El registro ya está seguro en el dispositivo; ese estado indica que todavía no se ha confirmado su recepción en el servidor.

Actualizar no envía reportes ni cambia ese estado. El backend ya acepta reportes JSON, pero la aplicación conserva el guardado local y no ejecuta sincronización automática. Cerrar y abrir la app mantiene tanto los datos como su estado.

La actualización de SQLite conserva los registros anteriores. Descargar de nuevo actualiza las referencias del servidor sin eliminar los lugares y categorías locales ni los reportes.

# Backend y recepción de reportes

Las entidades de dominio Lugar, Categoria y Reporte representan los datos. CatalogRepository y ReportRepository consultan H2 con JdbcTemplate. ReportService valida los reportes y controla los reenvíos; los controladores exponen las rutas REST. La base se guarda en data/campusreport.mv.db.

| Método | Ruta | Resultado |
| --- | --- | --- |
| GET | /api/catalogo | Lugares y categorías en un JSON. |
| GET | /api/lugares | Lista de lugares del servidor. |
| GET | /api/categorias | Lista de categorías del servidor. |
| GET | /api/reportes | Reportes recibidos por el servidor. |
| GET | /api/reportes/{uuid} | Detalle de un reporte recibido. |
| POST | /api/reportes | Validación y guardado de un reporte JSON. |

## Ejemplo de JSON

```text
{
  "uuid": "de5fc77a-4fda-4bed-844a-f6c39b7c9fa2",
  "lugarId": 1,
  "categoriaId": 1,
  "descripcion": "La luz del aula no enciende",
  "fechaCreacion": "2026-10-22T12:00:00Z",
  "anonimo": true,
  "autor": null
}
```

Un reporte nuevo devuelve HTTP 201 y su ubicación. Repetir el mismo UUID con los mismos datos devuelve HTTP 200 y no agrega otra fila. El mismo UUID con otro contenido devuelve HTTP 409. Un formulario inválido devuelve HTTP 400 y un mensaje; un reporte inexistente devuelve HTTP 404.

El servidor exige un lugar y una categoría existentes en su catálogo. Los lugares y categorías creados únicamente en el dispositivo siguen siendo locales y no se envían mediante este contrato. Si anonimo es true, el servidor descarta cualquier nombre recibido.

## Comprobación opcional del servicio

Con el servidor iniciado, abrir PowerShell en la carpeta extraída y ejecutar el comando siguiente. El archivo reporte_ejemplo.json ya viene incluido. Esta prueba crea un reporte en H2; no envía los reportes del emulador.

```text
$json = Get-Content .\reporte_ejemplo.json -Raw -Encoding UTF8
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/reportes `
  -ContentType 'application/json; charset=utf-8' `
  -Body ([System.Text.Encoding]::UTF8.GetBytes($json))
```

# Preparación en la computadora

Preparar hoy la instalación y ensayar el recorrido completo. Para la demostración se utiliza Windows, Android Studio con un emulador instalado y Java 17 o 21. El paquete incluye el APK y el JAR compilados; no necesita descargar dependencias para ejecutarlos.

## 1 Extraer el paquete

Descargar CampusReport_Demostracion.zip. Hacer clic derecho y elegir Extraer todo. Usar una carpeta fácil de encontrar, por ejemplo Escritorio\CampusReport. Ejecutar los archivos desde la carpeta extraída, no desde la vista del ZIP.

## 2 Abrir el emulador

Abrir Android Studio. Entrar en Tools → Device Manager. Iniciar un único dispositivo con Android API 26 o superior y esperar a ver su pantalla de inicio. El emulador debe estar completamente iniciado antes de instalar la app.

## 3 Iniciar el servidor

Hacer doble clic en 1_iniciar_servidor.cmd. Esperar el mensaje Started CampusReportApplication y dejar esa ventana abierta. El acceso busca Java en JAVA_HOME, en la instalación habitual de Android Studio y en PATH. H2 se crea automáticamente; no hace falta instalar otra base de datos.

Abrir http://localhost:8080/api/catalogo en el navegador de Windows. Deben aparecer lugares y categorías. Si la página no abre, resolver primero el inicio del servidor.

## 4 Instalar y abrir la app

Hacer doble clic en 2_instalar_app.cmd. Debe aparecer Success y abrirse CampusReport en el emulador. El instalador usa adb del SDK de Android y solicita que haya exactamente un emulador conectado. Como alternativa, arrastrar CampusReport.apk sobre la pantalla del emulador.

## 5 Descargar y comprobar

En Inicio, pulsar Actualizar. Esperar Lugares y categorías actualizados. Entrar en Lugares y Categorías para ver los datos. En una instalación nueva del servidor se incluyen cuatro lugares y cuatro categorías. Las listas pueden tener más elementos si ya guardaste datos propios.

## Direcciones y conservación de datos

En el navegador de Windows se utiliza localhost. La app del emulador usa http://10.0.2.2:8080 para llegar al mismo servidor. El paquete está configurado para esta demostración local, no para conectar un teléfono físico por Wi-Fi.

Conservar la carpeta data del servidor. En el emulador no usar Borrar datos, Wipe Data ni desinstalar la app durante la demostración: esas acciones eliminan información. El acceso 3_cerrar_app.cmd cierra el proceso sin borrar sus registros.

# Demostración frente al profesor

## 1 Mostrar la descarga con conexión

Con el servidor abierto y el equipo conectado, mostrar la respuesta de /api/catalogo en el navegador. En CampusReport, pulsar Actualizar y enseñar las listas de Lugares y Categorías. Explicar que esos datos se incorporaron a SQLite.

## 2 Dejar el servidor fuera de alcance

Desactivar el Wi-Fi de Windows y desconectar el cable de red si se está usando. Además, detener el servidor con Ctrl+C en su ventana; si pide confirmar el cierre, aceptarlo. Esto es necesario porque localhost sigue funcionando aunque no haya internet.

En la app, pulsar Actualizar una vez. Debe mostrarse No pudimos actualizar. Podés seguir usando tus datos guardados. Esperar unos segundos si es necesario. Entrar en Lugares y Categorías para mostrar que las listas permanecen disponibles.

## 3 Mostrar la validación

Abrir Nuevo reporte y pulsar Guardar con Descripción vacía o escribir solo Luz. Aparece el error Escribí entre 10 y 500 caracteres. Completar después el formulario con un texto válido; ese intento inválido no crea un reporte.

## 4 Crear dos reportes sin conexión

| Campo | Primer reporte | Segundo reporte |
| --- | --- | --- |
| Lugar | Aula 1 | Biblioteca |
| Categoría | Electricidad | Mobiliario |
| Descripción | La luz del aula no enciende | Una silla de la biblioteca está rota |
| Reporte anónimo | Activado | Desactivado |
| Tu nombre | No aparece | Roger Báez |

Pulsar Guardar en cada formulario. Revisar el detalle y volver a Inicio para crear el siguiente. En Reportes deben verse los dos registros nuevos con Pendiente de sincronización. Si existen registros anteriores, comprobar que el total aumentó en dos.

## 5 Cerrar de verdad y volver a abrir

Con el servidor todavía detenido y la red desconectada, ejecutar 3_cerrar_app.cmd. La aplicación se cierra por completo. Después ejecutar 4_abrir_app.cmd. Entrar en Reportes y abrir los dos registros: deben conservar descripción, autor y Pendiente de sincronización.

## Qué explicar al terminar

“Descargamos los lugares y categorías del servidor y los guardamos en SQLite. Después desconectamos la red y detuvimos el servidor. Pudimos consultar los datos y crear dos reportes. Al cerrar por completo y abrir la aplicación, los reportes siguieron guardados. Su estado pendiente también se conserva.”

# Verificación y resolución de problemas

## Comprobaciones realizadas

| Comprobación | Resultado |
| --- | --- |
| Android | APK 0.3.0 compilado; paquete com.example.campusreport; API mínima 26. |
| SQLite en Android | 6 pruebas aprobadas: persistencia, anonimato, migración, validación, dos reportes pendientes y descarga transaccional. |
| Backend | 9 pruebas de integración aprobadas: catálogo, referencias, recepción JSON, validaciones y reenvíos. |
| Servidor ejecutable | Arranque real del JAR, GET y POST comprobados por HTTP; el reporte permanece tras reiniciar H2 en archivo. |

Las pruebas de Android se ejecutaron con Robolectric y SQLite nativo. No se ejecutó aquí un emulador Android ni los accesos de Windows; el ensayo en la computadora del equipo debe comprobar la presentación visual, la instalación y el recorrido descrito.

## Si algo no funciona

• No se encontró Java: usar JDK 17 o 21 o revisar JAVA_HOME. El script también busca el Java incluido en Android Studio en su ubicación habitual.

• Puerto 8080 ocupado: detener la otra instancia de Spring Boot. Mantener un solo servidor abierto, ya sea desde Eclipse o desde el acceso incluido.

• No se pudo actualizar: comprobar primero /api/catalogo en Windows. Si abre allí, verificar que el emulador haya iniciado y que el servidor siga abierto. Al detener el servidor para la prueba, ese mensaje es el resultado esperado.

• UPDATE_INCOMPATIBLE al instalar: existe una app firmada con otra clave. Usar un emulador nuevo para este paquete y conservar el anterior con sus datos. No desinstalar una versión que contenga información que se quiera conservar.

• No se encontró adb: instalar Android SDK Platform-Tools desde SDK Manager. Si el SDK está en otra carpeta, configurar ANDROID_HOME con su ruta.

## Abrir y comprobar el código

En el repositorio roger-prime/Tutorial, usar la rama campusreport-semana1. Abrir campusreport/android en Android Studio. Para el backend, importar campusreport/backend en Eclipse mediante File → Import → Maven → Existing Maven Projects. El Tutorial de la raíz es un proyecto distinto.

```text
Backend: .\mvnw.cmd test package
Android: .\gradlew.bat assembleDebug testDebugUnitTest
```

Las evidencias de ejecución y las instrucciones también están en campusreport/docs. En Logcat puede filtrarse por CampusReport para ver las acciones; no se registran nombres de autores ni descripciones.

Referencias técnicas: developer.android.com/studio/run/emulator-install-add-files y docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/requestbody.html.
