# CampusReport — contenido técnico contrastado con el código

**Proyecto:** CampusReport · Proyecto Práctico TIC · UPA  
**Integrantes:** Roger Báez y Johan Escurra  
**Base de integración:** rama `campusreport-semana1`, commit `f6293bbbea6f4a43e91d8b3f5a46ce142f49f52f`, versión declarada `0.4.0`.  
**Rama de destino:** `campusreport-integracion-final`.  
**Estado de este archivo:** referencia técnica para la documentación final de Roger. «Implementado» no significa «probado visualmente en Windows».

## 1. Decisión de integración

Se conserva la arquitectura de la versión 0.4.0 de Roger porque ya incluye las capacidades centrales de la rama `johan-backend`: creación offline, UUID, alta de lugares y categorías, envío de reportes y descarga sin duplicados. No se debe fusionar automáticamente `johan-backend`: ambos lados implementan `POST /api/lugares` y `POST /api/categorias` de manera diferente, lo que puede duplicar mappings REST.

Los elementos complementarios de la rama anterior son `GET /api/estado` y `GET /api/reportes/novedades?desde=...`; **no forman parte de esta versión integrada** y no son necesarios para el flujo actual de Android. Su incorporación sería una mejora opcional, no una condición de entrega. La rama original se conserva como evidencia de contribuciones.

## 2. Objetivo y arquitectura

Aplicación Android nativa de registro y consulta de problemas de infraestructura universitaria. Permite trabajar con SQLite sin conexión y sincronizar manualmente con una API REST desarrollada en Java/Spring Boot, con persistencia H2.

```text
Pantallas Android (Java/Views)
  -> CampusApp -> SyncManager -> ApiClient (HTTP + JSON)
       |               |                       |
       v               v                       v
  CampusDb / SQLite  <--- cambios confirmados  Spring Boot REST
                                                   |
                                             Services / JDBC
                                                   |
                                                   v
                                                  H2
```

Tecnologías: Java, SQL, XML, JSON; Android Studio, Android SDK/Emulator, SQLiteOpenHelper, Spring Boot 3.5.6, JDBC/JdbcTemplate, H2, Gradle, Maven, Git y GitHub. La configuración de compilación usa compatibilidad Java 17; el entorno puede ejecutar JDK 21. Android: compileSdk/targetSdk 35 y minSdk 26.

## 3. Funcionalidades visibles y componentes Android

| Pantalla / componente | Función implementada | Archivo |
|---|---|---|
| Inicio | Navegación, contadores, botón **Sincronizar**, resultado de la sincronización | `ui/MainActivity.java` |
| Lugares | Listar lugares y guardar uno nuevo en SQLite | `ui/PlacesActivity.java`, `ui/CatalogActivity.java` |
| Categorías | Listar categorías y guardar una nueva en SQLite | `ui/CategoriesActivity.java`, `ui/CatalogActivity.java` |
| Nuevo reporte | Elegir lugar/categoría, describir problema, anonimato o nombre del autor, **Guardar** | `ui/NewReportActivity.java` |
| Reportes | Listar reportes existentes; indicar pendiente/sincronizado; abrir detalle | `ui/ReportsActivity.java` |
| Detalle | Mostrar lugar, categoría, descripción, autor, estado y fecha | `ui/DetailActivity.java` |

Archivos transversales:
- `ui/BaseActivity.java`: componentes visuales y navegación compartidos.
- `CampusApp.java`: ciclo de vida, executor de trabajo, observadores de UI y coordinación de la sincronización.
- `data/CampusDb.java`: SQLite, tablas, migraciones, transacciones, consultas, altas y estados.
- `data/NamedItem.java`, `data/Report.java`: modelos de representación.
- `sync/SyncManager.java`: orden de envío, reintentos y descarga.
- `sync/SyncTransport.java`: contrato de transporte intercambiable para pruebas.
- `sync/ReportJson.java`: validación de reportes y confirmaciones JSON.
- `network/ApiClient.java`: HTTP GET/POST con `HttpURLConnection`.
- `AndroidManifest.xml`: Activities y permiso de Internet.
- `res/values/strings.xml`, `styles.xml`, layouts/drawables: textos y presentación.

Validaciones principales: lugares/categorías de 3–80 caracteres, descripción de 10–500, autor identificado de 2–80; el modo anónimo guarda autor nulo.

## 4. Modelo de datos SQLite, versión 3

**lugar**: `id INTEGER PRIMARY KEY` (identificador local), `nombre TEXT NOT NULL`, `uuid TEXT UNIQUE`, `server_id INTEGER UNIQUE` (identificador del servidor, inicialmente nulo si no está confirmado).

**categoria**: misma estructura que lugar.

**reporte**: `uuid TEXT PRIMARY KEY`; `lugar_id INTEGER NOT NULL` FK a lugar; `categoria_id INTEGER NOT NULL` FK a categoria; `descripcion TEXT NOT NULL` (CHECK entre 10 y 500 caracteres); `fecha_creacion TEXT NOT NULL`; `sincronizado INTEGER NOT NULL` (0 o 1); `anonimo INTEGER NOT NULL` (0 o 1); `autor TEXT`. Índice `reporte_pendiente`.

Relaciones: **lugar 1:N reporte** y **categoria 1:N reporte**. Las claves foráneas SQLite están habilitadas. Los IDs locales de referencias nuevas son negativos; el `server_id` se agrega una vez aceptada la referencia sin necesidad de alterar su ID local. La migración v2→v3 incorpora `server_id` y conserva datos anteriores.

## 5. Modelo de datos H2

**lugar**: `id BIGINT PRIMARY KEY`, `nombre VARCHAR(120) NOT NULL`.

**categoria**: `id BIGINT PRIMARY KEY`, `nombre VARCHAR(120) NOT NULL`.

**reporte**: `uuid VARCHAR(36) PRIMARY KEY`, `lugar_id BIGINT NOT NULL` FK, `categoria_id BIGINT NOT NULL` FK, `descripcion VARCHAR(500) NOT NULL` (CHECK 10–500), `fecha_creacion VARCHAR(40) NOT NULL`, `anonimo BOOLEAN NOT NULL`, `autor VARCHAR(80)`.

**catalog_receipt**: `tipo VARCHAR(10) NOT NULL`, `uuid VARCHAR(36) NOT NULL`, `nombre VARCHAR(80) NOT NULL`, `item_id BIGINT NOT NULL`; PK compuesta `(tipo, uuid)`. Conserva la asociación entre el UUID enviado por un cliente y el ID asignado/reutilizado por el servidor. Su `item_id` representa la referencia; el esquema revisado no declara una FK directa para esa columna.

**sync_lock**: `id BIGINT PRIMARY KEY`; contiene la fila de control usada por `CatalogService` en la asignación de IDs y confirmaciones concurrentes.

En H2 no existe el campo `sincronizado`: ese estado se controla en SQLite. Los datos iniciales de `data.sql` incluyen cuatro lugares y cuatro categorías.

## 6. Backend Java y API REST

Componentes: `CampusReportApplication` inicia Spring Boot; `CatalogController` / `CatalogService` / `CatalogRepository` gestionan referencias; `ReportController` / `ReportService` / `ReportRepository` gestionan reportes; `ApiErrors` maneja errores; `ReportRequest` y modelos Java definen los datos. El backend utiliza **JDBC**, no JPA.

| Método | Ruta | Función |
|---|---|---|
| GET | `/api/catalogo` | Descargar lugares y categorías |
| GET | `/api/lugares` | Listar lugares |
| POST | `/api/lugares` | Registrar/confirmar lugar por UUID |
| GET | `/api/categorias` | Listar categorías |
| POST | `/api/categorias` | Registrar/confirmar categoría por UUID |
| GET | `/api/reportes` | Listar reportes |
| POST | `/api/reportes` | Registrar/confirmar reporte por UUID |
| GET | `/api/reportes/{uuid}` | Consultar un reporte |

Referencia POST: JSON con `uuid` y `nombre`. Reporte POST: `uuid`, `lugarId`, `categoriaId`, `descripcion`, `fechaCreacion`, `anonimo`, `autor`.

HTTP 201 = alta nueva; 200 = consulta correcta o reintento confirmado; 400 = campos inválidos/referencias inexistentes; 404 = reporte no encontrado; 409 = UUID reutilizado con contenido incompatible. El servidor puede asignar a una referencia local el ID de un lugar/categoría ya existente por nombre, evitando duplicar catálogos equivalentes.

## 7. Sincronización y recuperación

1. El usuario crea lugares, categorías y reportes sin red; se guardan en SQLite con identificadores estables.
2. Al pulsar **Sincronizar**, `SyncManager` envía primero referencias pendientes y registra en SQLite los IDs confirmados por el servidor.
3. Envía los reportes pendientes cuyas referencias ya tienen `server_id`.
4. Solo marca un reporte como sincronizado después de validar el contenido confirmado del servidor; no basta con recibir cualquier respuesta HTTP.
5. Descarga reportes y catálogo e incorpora novedades a SQLite de forma transaccional, evitando duplicados por UUID.
6. Ante respuesta perdida, rechazo o fallo de red, conserva los pendientes para reintento. Los resultados anteriores confirmados no se deshacen.

La sincronización se inicia **manualmente**. La descarga actual obtiene listas completas; no hay sincronización periódica automática ni cuentas por usuario.

## 8. Configuración del servidor: hechos del código

`application.properties` configura el servidor HTTP sobre `127.0.0.1:8080` y la base H2 persistente mediante `jdbc:h2:file:./data/campusreport`. El emulador Android usa `http://10.0.2.2:8080` para comunicarse con el equipo anfitrión. Son **valores de la configuración de desarrollo**, no evidencia de despliegue público. No indicar que existe un servidor remoto, dominio o acceso externo si no se implementó. La configuración técnica debe mencionarse correctamente aunque en la descripción general se evite repetir «local».

Archivos de configuración: `backend/src/main/resources/application.properties`, `schema.sql`, `data.sql`; `android/app/src/main/res/xml/network_security_config.xml`.

## 9. Limitaciones comprobables en el código

- No hay login, cuentas ni permisos de usuario; el autor identificado es texto declarado.
- No hay edición ni eliminación de reportes o sincronización de modificaciones concurrentes.
- La sincronización requiere pulsar el botón; no funciona automáticamente al recuperar conexión.
- No existe una función implementada para marcar reportes como «resueltos».
- El backend actual solo escucha en la interfaz local de desarrollo; la app no demuestra despliegue remoto.
- La descarga de reportes obtiene la lista completa y no dispone de paginación.
- La información central no está separada por cuentas o identidades de dispositivos.

## 10. Evidencia y estado de comprobación

- **Comprobación del equipo de Johan durante esta sesión:** `assembleDebug testDebugUnitTest` terminó en **BUILD SUCCESSFUL**. El backend Spring Boot arrancó y mostró conexión a H2 y Tomcat en puerto 8080.
- **No confirmados visualmente en esta sesión:** prueba manual completa de creación offline → reinicio → sincronización → comprobación H2; ejecución final de `clean test` para backend tras eliminar clases compiladas de otra rama; instalación definitiva del APK y evidencia visual de la versión integrada.
- **Registros documentados en la rama 0.4.0 de Roger:** 17 pruebas Android, 13 pruebas backend y 8 comprobaciones HTTP, con ejecución declarada el 4/11/2026 en Linux. Su evidencia está en `docs/evidencias/cierre-pruebas.json` y `docs/evidencias/cierre-http.json`. No presentar estos resultados como pruebas propias ejecutadas en Windows.
- Un `BUILD SUCCESSFUL` confirma éxito de las tareas ejecutadas, no por sí solo todas las pruebas opcionales: `SyncHttpIntegrationTest` se omite si no se configura `CAMPUS_BACKEND_JAR`.

## 11. Relación entre código y documento final

Roger debe incluir en el Word final **todas** las pantallas, botones y acciones de la tabla de la sección 3, los cuatro grupos de tablas descritos (SQLite: 3; H2: 5), los ocho endpoints de la sección 6, la secuencia real de sincronización, las validaciones y las limitaciones. Adjuntar capturas de la **misma versión y commit** de los ejecutables finales; las capturas anteriores son preliminares.

La documentación principal tendrá un máximo de cinco páginas según la instrucción recibida. Capturas ampliadas, manual de uso, evidencias y guion pueden entregarse como anexos o documentos independientes.

## 12. Cambios respecto a nuestra versión 0.3.0 (`johan-backend`)

- Versión declarada de Android: 0.3.0 → 0.4.0.
- SQLite: versión 2 → 3; incorpora `server_id` en lugar de sustituir IDs locales.
- Sincronización separada en `SyncManager`, `SyncTransport` y `ReportJson` en lugar de gestionarla íntegramente desde `CampusApp`.
- Inicio: un solo botón **Sincronizar**, en lugar de controles separados para actualizar catálogo y enviar reportes.
- Backend: `CatalogService` y `catalog_receipt`/`sync_lock` sustituyen la estrategia de `LocalCatalogController` y secuencias de nuestra implementación anterior.
- La base 0.4.0 **no incluye** `GET /api/estado` ni `GET /api/reportes/novedades` de nuestra rama antigua.
- La versión 0.4.0 agrega pruebas automatizadas y guías para entrega.
- No afirmar que las ramas se fusionaron con Git: **se seleccionó la implementación 0.4.0 como base**, dejando las alternativas anteriores en su rama histórica.
