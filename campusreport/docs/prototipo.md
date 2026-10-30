# Pantallas de CampusReport

Roger Báez y Johan Escurra · Versión 0.4.0

Etiquetas de navegación: Inicio, Lugares, Categorías, Nuevo reporte, Reportes y Detalle del reporte.
Acciones comunes: Guardar, Sincronizar y Volver. Se centralizan en res/values/strings.xml.

## Inicio
Resumen de reportes guardados y pendientes, lugares y categorías. Botones Nuevo reporte,
Reportes, Lugares, Categorías y Sincronizar. Sincronizar conserva su etiqueta y se deshabilita
mientras trabaja. El resultado indica reportes enviados, descargados, referencias enviadas,
pendientes y cualquier incidencia. No se ejecutan dos sincronizaciones al mismo tiempo.

## Lugares y Categorías
Cada pantalla tiene Nombre, Guardar y una lista con Pendiente de sincronización o Sincronizado.
Volver regresa a la pantalla anterior. Los registros se guardan aunque no haya conexión.

## Nuevo reporte
Lugar, Categoría, Descripción, Reporte anónimo y Tu nombre solo cuando corresponda.
Guardar valida y persiste; Volver regresa. Al activar Reporte anónimo se descarta el nombre.

## Reportes
Nuevo reporte y lista con lugar, categoría, autor y estado. Al tocar un registro se abre
Detalle del reporte. Los registros recibidos del servidor también aparecen en esta lista.

## Detalle del reporte
Lugar, categoría, descripción, autor, fecha, estado y Volver. Las pantallas de consulta
se actualizan cuando termina la sincronización. No se muestran detalles técnicos en los formularios.
