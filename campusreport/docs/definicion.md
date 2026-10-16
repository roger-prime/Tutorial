# Definición de CampusReport

Proyecto Práctico TIC · Semana 1 · Equipo Roger y Johan · 16 de octubre de 2026

## Problema y objetivo

En la facultad, los desperfectos de aulas y espacios comunes pueden comunicarse de manera informal,
lo que dificulta conservar un registro. CampusReport propone registrar problemas de mantenimiento
en Android, aun sin conexión, y centralizarlos mediante sincronización con un servidor.
Este problema es la hipótesis del proyecto académico, no el resultado de una investigación de campo.
El alcance debe ser aprobado por el docente.

## Usuarios y alcance

Alumnos, docentes y personal que detectan desperfectos. La primera versión no requiere cuentas de usuario:
Mis reportes corresponde a registros guardados en ese dispositivo, no a una cuenta compartida.

Incluido en el producto de cuatro semanas: descargar lugares y categorías; consultarlos desde SQLite;
crear reportes con descripción válida y UUID estable; consultar listado y detalle; enviar registros
pendientes manualmente; descargar novedades y evitar duplicados por UUID en el servidor.

Excluido: login, fotografías, GPS, notificaciones push, chat, asignación de técnicos, seguimiento de
reparaciones, edición o eliminación de información compartida. Sincronizado no significa reparado.

## Información descargada y registros offline

El servidor entrega lugares (ID y nombre) y categorías (ID y nombre). Android los guarda en SQLite
antes de mostrarlos. Sin internet se generan reportes nuevos: UUID, lugar, categoría, descripción y
fecha de creación. Un campo local marca si el servidor confirmó su recepción. En la semana 1 todos
los reportes quedan pendientes; no existe todavía un endpoint de envío.

## Arquitectura

Android nativo en Java con Views y XML, SQLiteOpenHelper para persistencia local y HttpURLConnection
en un ExecutorService para red. Spring Boot en Java 17 expone REST JSON; JdbcTemplate consulta una
base H2 persistente en archivo, alojada en la máquina que ejecuta el servidor. H2 no es la SQLite del teléfono.
El emulador accede al backend local mediante http://10.0.2.2:8080. HTTP se limita a los hosts de desarrollo;
un despliegue externo requeriría HTTPS y revisar autenticación y acceso.

Flujo: servidor H2 → API REST → descarga Android → transacción SQLite → consultas de pantalla.
El flujo de escritura previsto: formulario → reporte SQLite pendiente → POST futuro → confirmación
del servidor → marcado local. Ante error se conserva el pendiente y se reintenta con el mismo UUID.

## Modelo de datos

| Entidad | Campos | Relación |
|---|---|---|
| Lugar | id PK, nombre | Un lugar tiene muchos reportes |
| Categoria | id PK, nombre | Una categoría tiene muchos reportes |
| Reporte | uuid PK, lugar_id FK, categoria_id FK, descripcion, fecha_creacion | Pertenece a un lugar y una categoría |

Solo SQLite añade `sincronizado` (0/1). Descripción obligatoria entre 10 y 500 caracteres.
UUID generado una única vez; fecha en UTC; claves foráneas activadas. La descarga actualiza
por ID dentro de una transacción y no borra reportes existentes. El catálogo inicial se obtiene del servidor.

## Contrato inicial de API

GET /api/catalogo devuelve {lugares: [...], categorias: [...]} y es el endpoint consumido por Android.
GET /api/lugares y GET /api/categorias permiten consultar cada lista. Cada elemento tiene id y nombre.
El POST /api/reportes es una operación prevista para semana 3, no implementada en semana 1.
Se planifica aceptar reintentos idénticos con el mismo UUID sin volver a insertar, y rechazar contenido
distinto con el mismo UUID. Un fallo no debe marcar el reporte como sincronizado.

## Pantallas

1. Inicio: cantidad de pendientes, navegación y descarga de catálogos con estado de resultado.
2. Lugares: listado local de lugares y categorías, con mensaje si todavía no se descargaron.
3. Nuevo reporte: selectores, descripción, validación y guardado offline.
4. Mis reportes: listado local y acceso a cada detalle.
5. Detalle: lugar, categoría, descripción, fecha, UUID y estado de sincronización.

## Entrega de la semana 1 y planificación

La base de esta entrega incluye código Android y backend, tablas principales, endpoints JSON,
navegación, descarga y prototipo visual. También adelanta el guardado local de reportes.
La validación ejecutada y los pasos que requieren emulador se detallan por separado en validacion.md.
La demostración consiste en arrancar ambos proyectos, descargar el catálogo y mostrarlo desde SQLite.

Semana 2: reforzar formularios, consultas y pruebas de persistencia offline. Semana 3: implementar envío,
confirmación, reintentos y control de duplicados. Semana 4: estabilizar, probar, documentar y defender.
Reparto sugerido: Roger, Android y SQLite; Johan, backend y base del servidor. Ambos participan en integración.
Cada integrante debe registrar aportes reales en Git y poder explicar su código.
