# CampusReport
## Documentación técnica de la versión 0.4.0
Roger Báez y Johan Escurra · Universidad Paraguayo Alemana
Proyecto Práctico TIC · Entrega final · 4 de noviembre de 2026

### Problema
Un desperfecto en un aula puede quedar sin registrar si la persona que lo detecta no tiene conexión. Un aviso informal tampoco garantiza que el reporte siga disponible cuando se quiera consultarlo.
### Objetivo
Registrar el problema en el momento, conservarlo en el teléfono y compartirlo con el servidor cuando vuelva la conexión. Un reintento no debe producir un segundo reporte.
### Alcance
El proyecto está dirigido a alumnos, docentes y personal de una facultad. Permite guardar lugares, categorías y reportes, consultar sus detalles y elegir si se indica el nombre del autor. Otras instituciones educativas u oficinas podrían adaptar el catálogo a sus instalaciones; esta versión opera con una sola institución.
### Arquitectura
Android utiliza Java, Views y XML. Las seis pantallas son Inicio, Lugares, Categorías, Nuevo reporte, Reportes y Detalle del reporte. Todas consultan SQLite. Guardar escribe en el dispositivo; Sincronizar inicia el intercambio con el servidor fuera del hilo de la interfaz.
En el backend, los controladores de Spring Boot reciben solicitudes HTTP, los servicios validan los datos y los repositorios usan JDBC para consultar H2. La instalación de demostración ejecuta el servidor en la misma computadora que el emulador.
### Modelo de datos
Lugar y Categoría tienen una relación de uno a muchos con Reporte: un reporte pertenece a un lugar y a una categoría. En SQLite, cada referencia tiene id local, nombre, uuid y server_id. Este último relaciona el registro local con su equivalente en el servidor.
Reporte conserva uuid como clave primaria, las dos claves foráneas, descripción, fecha de creación, estado de sincronización, opción de anonimato y autor. Si es anónimo, el nombre queda nulo. La migración a SQLite versión 3 conserva los datos anteriores.
H2 contiene lugar, categoria y reporte. catalog_receipt relaciona cada UUID de referencia recibido con su ID definitivo; sync_lock coordina las altas concurrentes. La clave primaria del reporte impide insertar dos filas con el mismo UUID.
<!-- page -->
# Contrato de la API y sincronización
### Rutas del servidor
| Método | Ruta | Operación |
| --- | --- | --- |
| GET | /api/catalogo | Consultar lugares y categorías juntos. |
| GET / POST | /api/lugares | Listar o guardar un lugar. |
| GET / POST | /api/categorias | Listar o guardar una categoría. |
| GET / POST | /api/reportes | Listar o guardar un reporte. |
| GET | /api/reportes/{uuid} | Consultar el detalle de un reporte. |
Los POST reciben JSON. Lugar y categoría usan uuid y nombre. Un reporte envía uuid, lugarId, categoriaId, descripcion, fechaCreacion en UTC, anonimo y autor. Un nombre de referencia tiene entre 3 y 80 caracteres; una descripción, entre 10 y 500; el autor identificado, entre 2 y 80.
### Envío y confirmación
1. Al guardar un reporte, Android asigna un UUID una sola vez y conserva el estado Pendiente de sincronización en SQLite.
2. Al pulsar Sincronizar, la app envía primero los lugares y las categorías pendientes. Guarda el ID que confirma el servidor antes de enviar los reportes que los utilizan.
3. El backend devuelve 201 para un alta o 200 para un reenvío idéntico. Android comprueba el UUID y el contenido confirmado antes de marcar el reporte como Sincronizado.
4. Si el mismo UUID llega con contenido diferente, el servidor devuelve 409; los campos inválidos producen 400. Ante rechazo, pérdida de respuesta o fallo de red, el reporte queda pendiente. Las confirmaciones anteriores se conservan.
### Descarga de novedades
La app consulta los reportes y el catálogo; incorpora ambos a SQLite en una transacción. Identifica las referencias por su ID de servidor y los reportes por UUID. Repetir una descarga no agrega copias. Una descarga tampoco confirma ni reemplaza un reporte local pendiente.
Si el servidor guardó un reporte pero se perdió su respuesta, el próximo envío usa el UUID original. El servidor devuelve el registro que ya tenía y Android puede confirmar el pendiente sin duplicarlo.
<!-- page -->
# Instalación y límites
### Ejecutar desde Android Studio en Windows
1. Extraer el ZIP final en una carpeta nueva. Abrir Android Studio → File → Open y elegir 02_fuentes/android, la carpeta con settings.gradle. En el repositorio, la ruta equivalente es campusreport/android. El Tutorial de la raíz es otro proyecto.
2. Esperar a que termine Gradle Sync. Usar JDK 17 o 21 para Gradle. El proyecto compila con Android SDK Platform 35; instalar esa plataforma si se solicita. Es independiente de la imagen del emulador: el dispositivo API 37 ya instalado sirve.
3. Abrir Tools → Device Manager. Iniciar un solo dispositivo y esperar su pantalla de inicio. Antes de actualizar una instalación con datos, conservar el emulador anterior y usar el respaldo del paquete si corresponde.
4. Cerrar cualquier backend previo. Desde 01_ejecutar, abrir 1_iniciar_servidor.cmd. Esperar Started CampusReportApplication. En el navegador de Windows abrir http://localhost:8080/api/catalogo y comprobar que aparecen lugares y categorías.
5. En la barra superior de Android Studio, elegir el dispositivo y la configuración app. Pulsar Run ▶. En CampusReport, pulsar Sincronizar. Como alternativa para la exposición, 2_instalar_app.cmd instala el APK ya compilado: usar una sola vía de instalación.
### Crear y conservar la base del servidor
El JAR incluye application.properties, schema.sql y data.sql. Sus fuentes están en 02_fuentes/backend/src/main/resources. Al arrancar se crea la carpeta data con campusreport.mv.db; el esquema crea las tablas y el script de datos agrega las referencias iniciales ausentes. No hace falta instalar MySQL ni ejecutar SQL manualmente.
El servidor usa jdbc:h2:file:./data/campusreport, usuario sa y contraseña vacía para la demo local. El punto representa la carpeta de trabajo del servidor. Para conservar una base previa, copiar la carpeta data completa con ambos servidores detenidos. Desde las fuentes también se puede iniciar el mismo backend con mvnw.cmd spring-boot:run en 02_fuentes/backend.
### Limitaciones
El emulador accede a http://10.0.2.2:8080 y Windows usa localhost:8080. El backend escucha en la interfaz local. Un teléfono físico o un despliegue público requieren otra configuración de red. La versión comparte altas y consultas, sin cuentas, permisos por usuario, edición compartida ni borrado de reportes. El nombre del autor es declarado; no se verifica su identidad.
La sincronización es manual y descarga las listas completas, adecuado para el volumen de esta demostración. Si aparece UPDATE_INCOMPATIBLE al instalar, conservar el emulador original y usar otro con la misma imagen ya descargada. No borrar los datos para resolver diferencias de firma.
