# Definición de CampusReport

Proyecto Práctico TIC · Roger y Johan · Actualización del 20 de octubre de 2026

## Problema y objetivo

Los desperfectos de aulas y espacios comunes pueden comunicarse de manera informal y quedar sin registro. CampusReport permite registrar problemas de mantenimiento de la facultad, organizar los lugares y las categorías y consultar lo guardado, incluso sin conexión.

## Usuarios

Alumnos, docentes y personal de la facultad. La aplicación permite registrar un reporte anónimo o indicar un nombre. Los registros pertenecen al dispositivo utilizado; no hace falta crear una cuenta.

## Funcionalidades de la aplicación

- Lugares: escribir un nombre, guardarlo y consultar la lista de lugares.

- Categorías: escribir un nombre, guardarlo y consultar la lista de categorías.

- Nuevo reporte: seleccionar un lugar y una categoría, describir el problema y guardar el reporte.

- Reporte anónimo: elegir si se guarda sin nombre o con el nombre indicado por la persona.

- Reportes: consultar los registros guardados y abrir el detalle de cada uno.

- Actualizar: incorporar los lugares y las categorías disponibles en el servidor.

- Conservar los datos guardados para consultarlos después de cerrar y abrir la aplicación.

## Una forma sencilla de usarla

La persona entra en Lugares y guarda “Laboratorio”. Después entra en Categorías y guarda “Electricidad”. En Nuevo reporte selecciona esos datos, describe el problema, elige si desea identificarse y pulsa Guardar. El registro aparece en Reportes.

## Identificación del autor

La opción Reporte anónimo está activada inicialmente. Si se mantiene activada, no se guarda el nombre y el detalle muestra “Anónimo”. Si se desactiva, aparece Tu nombre y es obligatorio completarlo. Es un nombre declarado por el usuario, no una identidad verificada.

# Arquitectura y datos

La interfaz utiliza palabras cotidianas. La organización técnica mantiene separadas las pantallas, el almacenamiento del teléfono y la comunicación con el servidor.

| Componente | Tecnología y función |
| --- | --- |
| Aplicación Android | Java, Views y XML para las pantallas, los formularios y la navegación. |
| Base del dispositivo | SQLite para guardar lugares, categorías y reportes. |
| Comunicación | API REST con JSON; las operaciones de red se ejecutan fuera del hilo principal. |
| Servidor | Spring Boot en Java y consultas mediante JdbcTemplate. |
| Base del servidor | H2 persistente en archivo para los datos de referencia. |

## Modelo de datos

| Entidad | Datos principales |
| --- | --- |
| Lugar | Identificador, nombre y UUID para los registros creados en el dispositivo. |
| Categoría | Identificador, nombre y UUID para los registros creados en el dispositivo. |
| Reporte | UUID, lugar, categoría, descripción, fecha, opción de anonimato y nombre del autor cuando corresponda. |

Cada reporte pertenece a un lugar y a una categoría. Los registros nuevos reciben un UUID estable. SQLite conserva además un campo interno de sincronización. Los identificadores locales de lugares y categorías no se superponen con los del servidor.

## Guardado y actualización

Guardar registra el formulario en SQLite y permite consultarlo desde las pantallas. Actualizar descarga lugares y categorías desde Spring Boot y los incorpora a SQLite. La actualización mantiene los reportes y los registros locales existentes. Los formularios pueden utilizarse sin descargar previamente datos del servidor.

## Consultas REST

GET /api/catalogo devuelve las listas de lugares y categorías y es la consulta que utiliza Android. GET /api/lugares y GET /api/categorias permiten consultar cada lista por separado. Cada elemento contiene un identificador y un nombre.

## Reglas de validación

Los nombres de lugares y categorías tienen entre 3 y 80 caracteres y no pueden repetirse en la lista actual al guardarlos. La descripción tiene entre 10 y 500 caracteres. Si el reporte no es anónimo, el nombre tiene entre 2 y 80 caracteres. Un reporte anónimo no almacena un nombre de autor.

# Pantallas y navegación

Los botones de navegación utilizan exactamente el mismo nombre que la pantalla de destino. Los formularios de Lugares, Categorías y Nuevo reporte comparten la acción Guardar.

| Pantalla | Contenido y acciones |
| --- | --- |
| Inicio | Resumen de registros y acceso a Nuevo reporte, Reportes, Lugares, Categorías y Actualizar. |
| Lugares | Campo Nombre, botón Guardar y lista de lugares. |
| Categorías | Campo Nombre, botón Guardar y lista de categorías. |
| Nuevo reporte | Lugar, categoría, descripción, Reporte anónimo, Tu nombre cuando corresponda y Guardar. |
| Reportes | Lista de reportes y acceso a Nuevo reporte y al detalle de cada registro. |
| Detalle del reporte | Lugar, categoría, descripción, autor y fecha. |

## Etiquetas comunes

Inicio · Lugares · Categorías · Nuevo reporte · Reportes · Detalle del reporte. Las acciones son Guardar, Actualizar y Volver. Actualizar conserva su nombre mientras trabaja; el resultado se comunica con un mensaje breve.

## Demostración

- Abrir Lugares, escribir un nombre y pulsar Guardar. Repetir el proceso en Categorías.

- Abrir Nuevo reporte, completar el formulario y guardar con Reporte anónimo activado.

- Guardar otro reporte desactivando Reporte anónimo e indicando un nombre.

- Abrir Reportes y comprobar que el detalle muestra Anónimo o el nombre elegido.

- Cerrar y abrir la aplicación para comprobar que los registros permanecen guardados.

- Con Spring Boot en ejecución, pulsar Actualizar y consultar los datos incorporados.

## Organización del equipo

Roger trabaja en Android y SQLite; Johan, en Spring Boot y la base del servidor. Ambos participan en las pruebas y en la explicación del funcionamiento. Cada integrante registra sus cambios reales en Git.

Base de referencia: consigna del Proyecto Práctico TIC y código de CampusReport. Las comprobaciones técnicas y las instrucciones de ejecución se mantienen en el repositorio.
