# CampusReport funcionamiento y sincronización

Roger Báez y Johan Escurra

Proyecto Práctico TIC · Versión 0.4.0 · Octubre de 2026

CampusReport permite registrar problemas de mantenimiento de la facultad, consultarlos sin conexión y compartirlos con el servidor cuando vuelve a estar disponible. Esta guía explica qué hace la aplicación, cómo ejecutar la nueva versión en Windows y cómo demostrar el ciclo completo de guardado y sincronización.

## Funcionalidades de la aplicación

En Lugares y Categorías se escribe un nombre y se pulsa Guardar. En Nuevo reporte se eligen Lugar y Categoría, se completa Descripción y se decide si el reporte será anónimo. Guardar conserva el registro en el teléfono, aunque no haya internet.

Reportes muestra los registros almacenados. Al tocar uno se abre Detalle del reporte, con lugar, categoría, descripción, autor, fecha y estado. El estado inicial es Pendiente de sincronización; cambia a Sincronizado después de una confirmación válida del servidor. Los reportes descargados también aparecen en esta lista.

Sincronizar envía primero los lugares y categorías pendientes y luego los reportes. Después descarga las novedades y las incorpora a SQLite. El botón se deshabilita durante la operación. El resultado indica cuántos reportes se enviaron y descargaron y cuántos registros siguen pendientes.

## Qué es cada componente

| Componente | Función en CampusReport |
|---|---|
| Frontend Android | Pantallas y formularios que utiliza la persona. Está escrito en Java con Views y XML. |
| SQLite | Base local del emulador o teléfono. Guarda lugares, categorías, reportes y sus estados. |
| Backend Spring Boot | Programa Java que valida solicitudes y consulta o guarda información en el servidor. |
| H2 | Base persistente del backend. Se guarda en data/campusreport.mv.db. |
| API REST y JSON | Rutas HTTP para intercambiar datos; JSON es el formato de esos mensajes. |
| Android Studio y Eclipse | Herramientas para editar y ejecutar los proyectos; no son las bases de datos. |

La aplicación consulta SQLite para mostrar sus pantallas. La sincronización comunica esa base local con Spring Boot. El servidor de esta demostración se ejecuta en la computadora; no hace falta contratar un servicio externo.

<!-- page -->
# Preparar la computadora

## 1 Conservar los datos anteriores

Cerrar el servidor anterior con Ctrl+C. Conservar su carpeta y el emulador que contiene los datos. Si el backend se ejecutaba desde Eclipse, detener esa ejecución también. Debe haber un solo servidor en el puerto 8080.

## 2 Extraer el paquete nuevo

Descargar CampusReport_Demostracion.zip, hacer clic derecho y elegir Extraer todo. Elegir una carpeta nueva, por ejemplo Escritorio\CampusReport_Sincronizacion. Ejecutar los accesos desde esa carpeta extraída, no desde la vista del ZIP.

Si se quieren conservar los datos anteriores del servidor, copiar su carpeta data completa dentro de la carpeta nueva, con ambos servidores detenidos. Si no se copia, el nuevo servidor crea una base inicial con sus lugares y categorías. No mezclar archivos de dos bases distintas.

## 3 Iniciar el emulador

Abrir Android Studio, entrar en Tools → Device Manager y pulsar el triángulo del dispositivo que ya funciona. Esperar a ver la pantalla de inicio de Android. Dejar abierto un solo emulador. Puede utilizarse el API 37 ya instalado; no hace falta descargar otra imagen para ejecutar el APK.

## 4 Respaldar la aplicación anterior

Si CampusReport ya contiene datos importantes, hacer doble clic en 0_respaldar_datos_android.cmd. Este acceso cierra la app y copia su SQLite a una carpeta con fecha y hora. Conservar también el emulador original. En una instalación nueva se puede omitir este paso.

## 5 Iniciar el backend

Hacer doble clic en 1_iniciar_servidor.cmd. Esperar Started CampusReportApplication y dejar esa ventana abierta. Abrir http://localhost:8080/api/catalogo en el navegador de Windows: deben aparecer lugares y categorías.

## 6 Instalar la aplicación

Hacer doble clic en 2_instalar_app.cmd. Esperar Success y comprobar que CampusReport se abre en el emulador. El acceso intenta actualizar conservando los datos. Necesita Java y Android SDK Platform-Tools, que normalmente ya están disponibles con Android Studio.

Si aparece UPDATE_INCOMPATIBLE, la firma de la app anterior es distinta. Conservar el dispositivo anterior y crear otro desde Device Manager → Create Virtual Device, usando la misma imagen ya descargada. Cerrar el primero, iniciar el nuevo y repetir la instalación. No usar Wipe Data ni desinstalar una app cuyos datos se quieran conservar.

## 7 Comprobar la nueva versión

En Inicio debe aparecer Sincronizar. Pulsarlo y esperar el resultado. Entrar en Lugares y Categorías. Si todavía aparece Actualizar, se está abriendo la versión anterior o un proyecto diferente.

<!-- page -->
# Demostrar el funcionamiento

## 1 Descargar con conexión

Con el servidor abierto, pulsar Sincronizar y mostrar Lugares y Categorías. Abrir 5_ver_reportes_servidor.cmd y anotar cuántos reportes había antes de empezar; podrían existir registros de ensayos anteriores.

## 2 Desconectar realmente el servidor

Desactivar el Wi-Fi de Windows y desconectar el cable de red si se utiliza. Además, detener el backend con Ctrl+C en su ventana. Esto es necesario porque la conexión local con la computadora puede seguir funcionando aunque se apague internet. Pulsar Sincronizar: debe informar que no completó la conexión y conservar los datos.

## 3 Crear tres reportes sin conexión

Entrar en Nuevo reporte, completar los campos y pulsar Guardar. Repetir hasta tener tres registros nuevos. Ejemplos: “La luz del aula no enciende”, “Una silla de la biblioteca está rota” y “Hay una pérdida de agua en el patio”. Elegir lugares y categorías adecuados de las listas.

Guardar uno con Reporte anónimo activado y otro desactivándolo e indicando el nombre. Mostrar Pendiente de sincronización en Reportes. Para demostrar la validación, intentar primero guardar una descripción vacía y luego corregirla.

## 4 Comprobar la persistencia

Con el servidor todavía detenido, ejecutar 3_cerrar_app.cmd y luego 4_abrir_app.cmd. Abrir Reportes y comprobar que los tres registros conservan sus datos y siguen pendientes.

## 5 Reconectar y enviar

Restablecer la conexión de Windows. Ejecutar 1_iniciar_servidor.cmd, esperar el arranque y pulsar Sincronizar en la app. Los tres reportes deben cambiar a Sincronizado. Si había otros pendientes, también pueden enviarse.

## 6 Comprobar el servidor y los reintentos

Ejecutar 5_ver_reportes_servidor.cmd. Buscar las tres descripciones y sus UUID en el JSON. Pulsar Sincronizar otra vez y recargar el navegador: no deben aparecer copias ni aumentar el total por ese reintento.

## 7 Descargar un dato creado en el servidor

Ejecutar 6_crear_dato_servidor.cmd. Se crea un reporte con la descripción “Reporte creado directamente en el servidor para la demostracion”. Antes de sincronizar todavía no estará en la app. Pulsar Sincronizar, buscarlo en Reportes y abrir su detalle. Cerrar y abrir para comprobar que quedó en SQLite.

El acceso guarda su JSON en evidencias_demo/dato_servidor.json. Repetirlo reutiliza el UUID y no crea duplicados. Para preparar otra demostración independiente, conservar ese archivo con otro nombre antes de ejecutar nuevamente el acceso.

## Explicación breve para el profesor

“Guardamos los datos en SQLite y podemos trabajar sin conexión. Al sincronizar, enviamos los pendientes como JSON. Solo los marcamos como sincronizados cuando el servidor confirma su contenido. El UUID evita duplicaciones al reintentar. También descargamos los registros nuevos del servidor y los conservamos localmente.”

<!-- page -->
# Organización del proyecto y los datos

## Problema objetivo y alcance

El problema es que los desperfectos de la facultad pueden comunicarse informalmente y quedar sin registro, especialmente cuando falta conexión. El objetivo es registrar, consultar y compartir esos problemas conservando la información durante las interrupciones de red.

El alcance actual es una facultad y una demostración local en computadora y emulador. Los lugares y categorías permiten adaptar el uso a otras instituciones educativas u oficinas de empresas. Operar varias organizaciones con datos separados requeriría autenticación, permisos y un despliegue seguro adicional.

## Estructura del código

En campusreport/android, ui contiene las pantallas; data administra SQLite; network realiza HTTP; sync coordina los envíos, confirmaciones y descargas. CampusApp mantiene una sola sincronización en un ejecutor separado del hilo de la interfaz.

En campusreport/backend, catalog y report separan controladores REST, servicios y repositorios. Los servicios validan y coordinan las operaciones; JdbcTemplate consulta H2. schema.sql crea o amplía tablas y data.sql agrega los ejemplos iniciales sin borrar datos existentes.

## Modelo de SQLite

| Tabla | Campos principales |
|---|---|
| lugar y categoria | id local, nombre, uuid único y server_id único cuando existe confirmación. |
| reporte | uuid único, lugar_id, categoria_id, descripcion, fecha_creacion, sincronizado, anonimo y autor. |

Cada reporte pertenece a un lugar y una categoría. Un lugar o categoría puede tener muchos reportes. Los registros propios de referencia tienen ID local negativo; server_id conserva su correspondencia con el servidor. La migración a SQLite versión 3 agrega esa correspondencia sin borrar registros anteriores.

## Modelo de H2 y API REST

H2 contiene lugar, categoria y reporte, con las mismas relaciones. catalog_receipt conserva las confirmaciones por tipo y UUID de lugares y categorías. sync_lock permite asignar sus identificadores dentro de una transacción.

GET /api/catalogo descarga lugares y categorías. GET /api/reportes consulta los reportes y GET /api/reportes/{uuid} consulta un detalle. POST /api/lugares, POST /api/categorias y POST /api/reportes reciben registros JSON. GET /api/lugares y GET /api/categorias permiten consultar cada lista.

Un alta devuelve HTTP 201. Repetir el mismo UUID y contenido devuelve 200 sin duplicar. El mismo UUID con otro contenido devuelve 409. Los datos inválidos devuelven 400. Android comprueba la respuesta antes de cambiar el estado local; ante un rechazo conserva el pendiente. Una descarga tampoco sobrescribe un reporte pendiente.

Esta versión sincroniza altas y consultas. No incluye edición o eliminación de reportes ni cuentas de usuario. Un nombre indicado en el formulario no representa una identidad verificada.

<!-- page -->
# Comprobación y entrega

## Pruebas ejecutadas

Se compilaron el APK y el JAR 0.4.0. Aprobaron 16 pruebas de Android y 13 pruebas del backend, sin fallos ni omisiones. Cubren persistencia, validaciones, migraciones, tres reportes sin conexión, respuestas perdidas, rechazos, confirmaciones, ausencia de duplicados y descarga de novedades. Un ensayo HTTP adicional confirmó ocho comprobaciones, incluida la persistencia después de reiniciar el servidor.

Las pruebas locales de Android utilizan Robolectric y SQLite nativo. La prueba integrada utiliza el cliente HTTP real y un proceso real de Spring Boot. No se ejecutaron aquí las ventanas del emulador ni los accesos de Windows; el ensayo en la computadora del equipo debe confirmar instalación, presentación visual y funcionamiento de esos accesos.

## Qué entregar

Compartir el enlace a la rama campusreport-semana1 del repositorio roger-prime/Tutorial, dentro de la carpeta campusreport. Adjuntar CampusReport_Demostracion.zip y esta guía Word. El ZIP contiene el APK, el JAR y los accesos de demostración; el repositorio conserva fuentes, pruebas y documentación.

Agregar capturas propias del ensayo: tres reportes pendientes sin conexión, los mismos reportes sincronizados, sus UUID en el servidor, el total sin cambios después de repetir y el dato creado en el servidor visible en Android. No reemplazar esas evidencias por capturas de otro proyecto.

## Abrir las fuentes para continuar

Guardar o commitear los cambios propios antes de actualizar el repositorio. En Android Studio abrir campusreport/android, no Tutorial de la raíz. Para el backend, importar campusreport/backend como Existing Maven Projects en Eclipse, o usar otro IDE Java. Ejecutar un solo backend a la vez.

Desde backend: .\mvnw.cmd test package. Desde android: .\gradlew.bat assembleDebug testDebugUnitTest. La prueba HTTP integrada necesita CAMPUS_BACKEND_JAR con la ruta absoluta del JAR compilado. Las instrucciones completas están en README.md y docs/validacion.md.

## Si algo no funciona

Puerto 8080 ocupado: cerrar la otra ejecución del backend. Si se desea detenerlo, usar Ctrl+C en su ventana; no eliminar la carpeta data.

No se completa la conexión: comprobar http://localhost:8080/api/catalogo en Windows. Si no abre, revisar el servidor. La app del emulador usa http://10.0.2.2:8080; localhost dentro del emulador sería otro equipo.

Emulador offline o iniciando: esperar su pantalla de inicio y dejar un solo dispositivo activo. Si sigue trabado, cerrarlo desde Device Manager o reiniciar la computadora conservando sus datos. No usar Wipe Data como reinicio.

Un reporte sigue pendiente: comprobar que el backend sea el incluido en este paquete y pulsar Sincronizar. Los rechazos conservan el registro; no conviene borrar datos para ocultar el problema. Para diagnosticar, mostrar el mensaje de Inicio y filtrar Logcat por CampusReport.

La configuración está preparada para el emulador de Android Studio en la misma computadora que el backend. Un teléfono físico o un servidor externo requieren configurar la dirección y la conexión correspondientes.
