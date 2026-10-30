# Definición de CampusReport

Roger Báez y Johan Escurra · Proyecto Práctico TIC · Versión 0.4.0

## Problema

Los desperfectos de aulas y espacios comunes se comunican informalmente y pueden quedar sin
un registro consultable. La falta de conexión dificulta registrar el problema en el momento.

## Objetivo

Permitir registrar, organizar y consultar problemas de mantenimiento de la facultad, incluso
sin conexión, y compartir los registros con un servidor al recuperar la conexión.

## Alcance del proyecto

La implementación actual está dirigida a una facultad: alumnos, docentes y personal pueden
registrar problemas de sus instalaciones. Se demuestra en una computadora y un emulador,
con un servidor local. El catálogo permite adaptar los lugares y categorías a otras facultades,
instituciones educativas u oficinas de una empresa. Extenderlo a varias organizaciones con
datos separados requeriría autenticación, permisos, separación por organización y despliegue
seguro; esas capacidades no están implementadas en esta versión.

## Funcionalidades de la aplicación

Guardar y consultar Lugares y Categorías; crear reportes con validación; elegir Reporte anónimo
o indicar un nombre; consultar Reportes y Detalle del reporte; conservar datos al cerrar y abrir;
enviar pendientes y descargar novedades mediante Sincronizar; visualizar el resultado y estado.

## Arquitectura

Android usa Java, Views y XML. Las pantallas consultan CampusDb, que administra SQLite.
CampusApp coordina una tarea de sincronización fuera del hilo principal. SyncManager envía
los pendientes usando ApiClient y guarda las confirmaciones y descargas en SQLite.
Spring Boot recibe JSON en controladores REST, valida en servicios y consulta H2 mediante
repositorios JDBC. H2 persiste en archivo. Android Studio y Eclipse son herramientas de
edición y ejecución, no bases de datos ni servidores.

## Modelo local SQLite

| Tabla | Campos |
|---|---|
| lugar | id local PK, nombre, uuid UNIQUE, server_id UNIQUE nullable |
| categoria | id local PK, nombre, uuid UNIQUE, server_id UNIQUE nullable |
| reporte | uuid PK, lugar_id FK, categoria_id FK, descripcion, fecha_creacion, sincronizado, anonimo, autor nullable |

Un lugar tiene muchos reportes y una categoría tiene muchos reportes. Cada reporte pertenece
a un lugar y una categoría. Los nuevos lugares y categorías reciben un ID local negativo y
un UUID; server_id permanece nulo hasta recibir confirmación. Los reportes pendientes usan
sincronizado=0 y los confirmados/descargados, sincronizado=1. La migración a versión 3 es aditiva.

## Modelo del servidor H2

| Tabla | Campos |
|---|---|
| lugar | id PK, nombre |
| categoria | id PK, nombre |
| reporte | uuid PK, lugar_id FK, categoria_id FK, descripcion, fecha_creacion, anonimo, autor nullable |
| catalog_receipt | tipo y uuid como PK compuesta, nombre, item_id |
| sync_lock | id PK; fila técnica para serializar altas de referencias |

catalog_receipt conserva la confirmación de cada UUID de lugar/categoría, incluso cuando varios
UUID corresponden a un nombre ya existente. Evita duplicar y permite detectar reenvíos con
contenido distinto. El UUID del reporte es único en ambas bases.

## Validación y anonimato

Nombre de lugar/categoría: 3–80 caracteres y sin repetirse en la lista local. Descripción:
10–500 caracteres. Nombre del autor: 2–80 si no es anónimo. Deben existir lugar y categoría.
Un reporte anónimo guarda autor nulo y muestra Anónimo. El nombre es declarado, no verificado;
la persona debe evitar incluir datos identificatorios en la descripción si busca anonimato.

## Sincronización

Guardar siempre escribe en SQLite. Sincronizar envía referencias pendientes y luego reportes,
validando la respuesta de cada POST. Solo confirma el contenido efectivamente aceptado.
Si se corta la red o el servidor rechaza un registro, permanece pendiente. Un GET no confirma
un pendiente. Las novedades se incorporan en una transacción por ID/UUID, sin duplicar ni
borrar datos locales. Se comparten altas y consultas; no se editan reportes ya guardados.
