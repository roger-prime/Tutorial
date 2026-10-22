# Prototipo de CampusReport

Etiquetas de navegación: Inicio, Lugares, Categorías, Nuevo reporte, Reportes y Detalle del reporte.
Acciones comunes: Guardar, Actualizar y Volver. En Android se centralizan en res/values/strings.xml.

## Inicio
Resumen de registros y botones Nuevo reporte, Reportes, Lugares, Categorías y Actualizar.

## Lugares
Campo Nombre, botón Guardar y lista de lugares. Volver regresa a la pantalla anterior.

## Categorías
Campo Nombre, botón Guardar y lista de categorías. Volver regresa a la pantalla anterior.

## Nuevo reporte
Campos Lugar, Categoría y Descripción; casilla Reporte anónimo; campo Tu nombre solo si no es anónimo.
Acciones Guardar y Volver. Al marcar Reporte anónimo, el nombre introducido se descarta.

## Reportes
Botón Nuevo reporte y lista con lugar, categoría y autor (Anónimo o el nombre indicado).
Cada registro muestra Pendiente de sincronización. Al tocarlo se abre Detalle del reporte.

## Detalle del reporte
Lugar, categoría, descripción, autor, fecha y estado Pendiente de sincronización. Acción Volver.

Los formularios y los registros muestran mensajes breves, sin detalles de SQLite, UUID, Spring Boot
ni planificación por semanas. La guía de funcionamiento describe las acciones y la demostración.
