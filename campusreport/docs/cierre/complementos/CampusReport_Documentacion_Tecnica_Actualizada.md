# CampusReport — Documentación técnica actualizada (versión textual)

**Universidad Paraguayo Alemana · Proyecto Práctico TIC · Johan Escurra y Roger Báez**

Versión de código: 0.4.0. Rama base: campusreport-integracion-final (misma implementación funcional del commit f6293bb). Esta transcripción conserva el contenido del Word actualizado; los diagramas se consultan en [diagramas_y_cobertura.md](../diagramas_y_cobertura.md). El Word con diseño de cuatro páginas existe separadamente y no está reemplazado por este archivo.

## 1. Problema, objetivo y alcance

Los desperfectos en aulas y espacios comunes pueden comunicarse de manera informal y perder seguimiento. CampusReport permite registrar el lugar, la categoría y una descripción del problema; conservar el reporte sin conexión; y compartirlo con un servidor mediante una sincronización manual.

El usuario puede registrar reportes anónimos o con un nombre declarado, consultar su estado y abrir el detalle. No existen cuentas, perfiles ni funciones para editar, eliminar o marcar problemas como resueltos.

## 2. Arquitectura y tecnologías

Android nativo con Java, Views y recursos XML. SQLite conserva la información del dispositivo; CampusApp coordina operaciones asíncronas; SyncManager organiza el intercambio HTTP/JSON con ApiClient. Spring Boot 3.5.6 valida solicitudes REST y utiliza JDBC/JdbcTemplate para persistir en H2. Gradle compila Android y Maven compila el backend; Git/GitHub controla versiones.

> Android / SQLite ⇄ HTTP + JSON ⇄ Spring Boot / JDBC / H2

## 3. Componentes Android y funciones visibles

| Pantalla / clase | Función y controles |
| --- | --- |
| Inicio · MainActivity | Navegar a Nuevo reporte, Reportes, Lugares, Categorías; consultar contadores; botón Sincronizar. |
| Lugares · PlacesActivity | Listado local; campo Nombre, botón Guardar y Volver. |
| Categorías · CategoriesActivity | Listado local; campo Nombre, botón Guardar y Volver. |
| Nuevo reporte · NewReportActivity | Selectores Lugar y Categoría; campo Descripción; casilla Reporte anónimo; campo Tu nombre al desmarcar; Guardar y Volver. |
| Reportes · ReportsActivity | Listado con estado Pendiente/Sincronizado; botón Nuevo reporte; abrir cada detalle; Volver. |
| Detalle · DetailActivity | Lugar, categoría, descripción, autor, fecha y estado; Volver. |

Clases de soporte: BaseActivity (presentación), CatalogActivity (formulario compartido), CampusDb (persistencia y migraciones), ApiClient (peticiones HTTP), SyncManager (sincronización), SyncTransport (interfaz para pruebas), ReportJson (validación y confirmaciones) y modelos Report/NamedItem.

## 4. Base de datos SQLite — versión 3

SQLite es la fuente de datos de las pantallas. El registro local de lugar o categoría tiene un id propio y, después de la confirmación del servidor, un server_id distinto. Esta separación evita reescribir los IDs locales al sincronizar.

**Figura 1.** Esquema SQLite construido a partir de CampusDb.java. Consultar [diagrama editable](../diagramas_y_cobertura.md#sqlite--campusdbjava-esquema-versión-3).

### Relaciones y restricciones

Un lugar puede tener muchos reportes y una categoría puede clasificar muchos reportes. reporte.lugar_id y reporte.categoria_id son claves foráneas; reporte.uuid es su clave primaria. Las descripciones admiten 10–500 caracteres; sincronizado y anonimo valen 0 o 1. La aplicación activa las claves foráneas y crea un índice para los reportes pendientes.

La migración de versión 2 a 3 incorpora server_id a las referencias, conserva los datos previos y mantiene los UUID existentes. No se debe limpiar la base ni desinstalar la app para demostrar persistencia.

## 5. Base de datos H2 — servidor

El servidor conserva lugares, categorías y reportes. catalog_receipt registra las confirmaciones de referencias enviadas por UUID; sync_lock serializa operaciones de alta del catálogo. Estas dos tablas tienen finalidades distintas y no representan usuarios de la aplicación.

**Figura 2.** Esquema H2 construido a partir de schema.sql. Las relaciones de catalog_receipt son asociaciones lógicas, no claves foráneas SQL declaradas. Consultar [diagrama editable](../diagramas_y_cobertura.md#h2--schemasql-versión-040).

### Identificadores y consistencia

La clave primaria reporte.uuid impide duplicar un reporte con el mismo identificador. Sus claves foráneas deben apuntar a lugares y categorías del servidor. catalog_receipt usa clave primaria compuesta (tipo, uuid) para devolver una confirmación estable cuando se reintenta el envío de referencias. H2 no almacena el estado sincronizado: ese indicador pertenece a SQLite.

## 6. Contrato REST y sincronización

| Método | Ruta | Resultado |
| --- | --- | --- |
| GET | /api/catalogo | Lugares y categorías conjuntamente. |
| GET / POST | /api/lugares | Listar / registrar o confirmar lugar. |
| GET / POST | /api/categorias | Listar / registrar o confirmar categoría. |
| GET / POST | /api/reportes | Listar / registrar o confirmar reporte. |
| GET | /api/reportes/{uuid} | Consultar un reporte concreto. |

Referencia POST: uuid y nombre (3–80 caracteres). Reporte POST: uuid, lugarId, categoriaId, descripcion (10–500), fechaCreacion ISO 8601 UTC, anonimo y autor (2–80 cuando se identifica). Respuestas principales: HTTP 201 alta, 200 consulta o reenvío idéntico, 400 datos inválidos, 404 reporte ausente y 409 UUID asociado a otro contenido.

### Proceso real

1. Guardar escribe en SQLite incluso sin conexión.
2. Sincronizar envía primero lugares y categorías pendientes; al confirmar guarda server_id.
3. Envía los reportes que ya tienen referencias del servidor.
4. Comprueba UUID y contenido confirmado antes de marcar sincronizado.
5. Descarga reportes y catálogo; registra novedades en una transacción, sin duplicar y sin sobrescribir pendientes.
6. Si se corta la conexión o se pierde una respuesta, conserva los datos y permite reintentar con el mismo UUID.

## 7. Configuración técnica del servidor

- Spring Boot · puerto 8080 · server.address=127.0.0.1
- spring.datasource.url=jdbc:h2:file:./data/campusreport

La ruta de H2 es relativa a la carpeta de ejecución. Los archivos de esquema y datos son backend/src/main/resources/schema.sql y data.sql. Android usa http://10.0.2.2:8080 desde el emulador. Esta configuración demuestra una integración local de desarrollo, no un despliegue público. Un entorno externo requeriría configuración de red y HTTPS.

## 8. Limitaciones verificables

Sin autenticación ni permisos, sin edición o eliminación de reportes, sin estados de resolución, sin sincronización periódica, sin gestión de conflictos entre modificaciones remotas y sin paginación: al sincronizar, Android descarga listas completas. El autor nominal no está autenticado.

## 9. Pruebas y trazabilidad

La rama 0.4.0 registra 17 pruebas Android, 13 backend y 8 comprobaciones HTTP (evidencias del 4/11/2026). Además, en la rama de integración Johan registró compilación de APK/JAR y una prueba SyncHttpIntegrationTest aprobada; no se dispone aquí de capturas visuales definitivas del emulador. La matriz de cobertura y la lista exacta de capturas necesarias están en el anexo aparte.
