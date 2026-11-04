# Pruebas funcionales de CampusReport
Roger Báez y Johan Escurra · Proyecto Práctico TIC · Versión 0.4.0
### Criterios de aceptación
La tabla relaciona cada requisito con el resultado automatizado y una prueba localizable en las fuentes. Los resultados de la ejecución están en docs/evidencias. Las capturas del emulador deben obtenerse en la computadora del equipo durante el ensayo.
| Escenario | Comprobación y resultado |
| --- | --- |
| Consulta sin conexión | Tras descargar el catálogo, SQLite conserva lugares, categorías y detalles para su consulta. La lista abre el reporte por UUID sin llamar al servidor. |
| Creación y persistencia | Se crearon tres reportes sin red. Tras cerrar y reabrir SQLite permanecieron los tres con su estado pendiente y la identificación elegida. |
| Envío exitoso | Con el servidor disponible, el cliente envió las referencias y tres reportes. El backend confirmó los tres y la app dejó cero reportes pendientes. |
| Descarga de novedades | Un reporte creado en el servidor se incorporó a SQLite. La segunda descarga agregó cero copias y el detalle persistió al reabrir la base. |
| Reintento sin duplicación | Se repitió el mismo POST con el mismo UUID y contenido. El servidor confirmó el registro existente y el total permaneció en tres. |
| Error de conexión | Se simuló la pérdida de respuesta después de guardar en el servidor. El reporte permaneció pendiente; reintentar con su UUID lo confirmó una sola vez. |
### Evidencia que respalda la tabla
Las pruebas de Android están en android/app/src/test/java/com/example/campusreport. CampusDbLocalTest comprueba persistencia; SyncManagerTest cubre la red interrumpida, confirmaciones incorrectas y descargas sin duplicar. SyncHttpIntegrationTest usa SQLite y el cliente HTTP contra un JAR real de Spring Boot.
El 4 de noviembre aprobaron las 30 pruebas: 17 de Android y 13 del backend, sin errores ni pruebas omitidas. OfflineScreensTest abre la lista y el detalle de un reporte después de reabrir SQLite y sin red activa. La prueba se realizó con Robolectric; no es una captura del emulador de Windows. Los casos y resultados están identificados en docs/evidencias/cierre-pruebas.json.
En GitHub, docs/evidencias/cierre-http.json registra el ensayo del 4 de noviembre contra el JAR entregado. Incluye tres altas, reenvíos sin duplicados, conflicto por UUID y persistencia al reiniciar H2. El laboratorio no sustituye el ensayo visual en Windows.
<!-- page -->
# Demostración en vivo de 15 minutos
### Antes de entrar al aula
Extraer el paquete completo. Tener abierto un solo emulador y un solo servidor. Ensayar con la misma instalación que se usará el viernes. Conservar la base del servidor y anotar el total inicial de reportes antes de la demostración.
Si ya se ensayó el dato nuevo del servidor, renombrar evidencias_demo/dato_servidor.json antes de la presentación para generar un UUID nuevo. Repetir el acceso sin renombrar el archivo es un reintento y no crea un dato nuevo.
### Orden de la presentación
0:00–1:30 · Roger. Presentar el problema de la facultad y el caso de la silla rota. Explicar qué puede hacer la persona sin conexión.
1:30–3:00 · Roger. Mostrar que Lugares, Categorías y Reportes consultan SQLite. Guardar nunca espera una respuesta del servidor.
3:00–4:30 · Johan. Explicar qué recibe Spring Boot, dónde guarda los datos y por qué un reintento usa el mismo UUID.
4:30–8:00 · Roger. Pulsar Sincronizar y mostrar las referencias descargadas. Desconectar el Wi-Fi y detener el backend con Ctrl+C para cortar también el acceso local. En Nuevo reporte, guardar tres descripciones distintas; dejar una anónima y otra con nombre. Pulsar Sincronizar sin servidor. Abrir Reportes y mostrar que siguen pendientes. Usar 3_cerrar_app.cmd y 4_abrir_app.cmd; abrir un detalle.
8:00–11:30 · Johan. Recuperar la conexión y ejecutar 1_iniciar_servidor.cmd. Esperar Started CampusReportApplication y pulsar Sincronizar. Mostrar los tres UUID con 5_ver_reportes_servidor.cmd. Repetir Sincronizar: el total del servidor no debe aumentar. Ejecutar 6_crear_dato_servidor.cmd, sincronizar y abrir el reporte nuevo en Android.
11:30–12:30 · Ambos. Mostrar los resultados de pruebas y distinguir el ensayo automatizado de lo observado en el equipo.
12:30–15:00 · Ambos. Responder una pregunta individual por integrante y cerrar mostrando la versión ejecutable y el repositorio.
### Capturas para acompañar la entrega
Tomar capturas de los tres pendientes, de un detalle al reabrir, del estado Sincronizado, del total estable en el servidor y del dato nuevo descargado. Usar ejemplos de demostración sin información personal innecesaria.
<!-- page -->
# Defensa individual
El siguiente reparto sirve para preparar la exposición. Cada integrante debe vincular su explicación con el trabajo que realmente realizó y con un commit propio que pueda mostrar.
### Roger · Android y SQLite
Abrir ui/NewReportActivity.java y data/CampusDb.java. Seguir el recorrido desde Guardar hasta crearReporte. Mostrar dónde se valida la descripción, se genera el UUID y se guarda sincronizado=0. Explicar por qué el reporte anónimo guarda autor nulo y por qué la lista sigue funcionando tras apagar el servidor.
Pregunta: ¿por qué se envían primero los lugares y categorías? Un reporte depende de ellos. El ID local permite guardar sin conexión; server_id permite referenciarlos correctamente en el JSON cuando el servidor ya confirmó su recepción.
Pregunta: ¿por qué no marcar el reporte apenas se pulsa Sincronizar? El envío puede fallar o la respuesta puede perderse. CampusDb.confirmarReporte compara el contenido confirmado antes de cambiar el estado.
### Johan · Spring Boot y confirmaciones
Abrir report/ReportController.java, ReportService.java y ReportRepository.java. Seguir POST /api/reportes: recibir JSON, validar las referencias, buscar el UUID y guardar en H2. Mostrar la clave primaria de reporte en schema.sql.
Pregunta: ¿qué diferencia hay entre 201, 200 y 409? Un reporte nuevo crea una fila y devuelve 201. Repetirlo idéntico devuelve 200 y la misma fila. Reutilizar su UUID con otro contenido devuelve 409 y conserva el original.
Pregunta: ¿cómo llega un reporte del servidor a Android? SyncManager descarga los reportes y el catálogo; CampusDb.guardarNovedades incorpora los nuevos por UUID dentro de una transacción y conserva los pendientes locales.
### Aportes identificables
En el clon con historial, ejecutar git log --format="%h %an %s" -- campusreport. Cada integrante debe mostrar el commit de su aporte real. Un reparto de la exposición por temas no demuestra autoría.
Si falta evidencia individual, Roger puede registrar su ensayo offline con los resultados y capturas obtenidos por él. Johan puede documentar su ensayo del backend con el UUID, los códigos de respuesta y el total observado. Cada uno sube su propio archivo desde su cuenta y explica su trabajo. No atribuir commits ni resultados al compañero.
### Si falla algo durante la presentación
Si Windows no abre localhost:8080/api/catalogo, revisar la ventana del servidor y el puerto 8080. Si Windows sí abre el catálogo pero la app no conecta, comprobar que se usa el emulador correcto y que el backend sigue abierto. Tras un fallo, mostrar los pendientes, restablecer el servidor y reintentar sin borrar datos.
