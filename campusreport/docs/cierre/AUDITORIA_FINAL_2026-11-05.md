# Auditoría de entrega — CampusReport 0.4.0
**Fecha:** 5 de noviembre de 2026  
**Integrantes:** Johan Escurra y Roger Báez  
**Rama auditada:** `campusreport-integracion-final` (código funcional derivado de `campusreport-semana1`, `f6293bb`).

## Resultado
**Proyecto funcionalmente coherente y paquete parcialmente verificado, con observaciones documentales que impiden declarar una verificación total.** No se encontraron inconsistencias de rutas, esquema de tablas ni configuración en el código fuente consultado. No se recompiló ni ejecutó la aplicación dentro de esta auditoría; se consultaron el código, los registros de pruebas y las evidencias anteriores.

## Comprobaciones positivas
- Android: Java, `minSdk 26`, `compileSdk 35`, `targetSdk 35`, `versionName 0.4.0` y SQLite versión 3.
- Spring Boot 3.5.6 / Java 17 con API REST para catálogo, lugares, categorías y reportes.
- H2 persistente con `jdbc:h2:file:./data/campusreport`, puerto 8080 y escucha local `127.0.0.1`; emulador Android usa `10.0.2.2:8080`.
- Modelo SQLite: lugar, categoria, reporte; FK de reporte a ambas referencias, UUID estable y campo `server_id` separado del identificador local.
- Modelo H2: lugar, categoria, reporte, catalog_receipt, sync_lock; claves FK declaradas para reporte, asociación lógica para catalog_receipt.
- Registros de pruebas existentes: Roger (17 Android, 13 backend, 8 HTTP; 4/11/2026); Johan (1 prueba HTTP integrada aprobada). Las evidencias son archivos de resultados documentados, no una nueva ejecución efectuada en esta auditoría.
- Documento técnico Word actualizado: **4 páginas**, renderizado e inspeccionado sin cortes visibles; dos diagramas incluidos. Matriz Word: 1 página.
- Seis imágenes PNG del emulador presentes: `01_inicio.png`, `02_lugares.png`, `03_categorias.png`, `04_nuevo_reporte.png`, `06_detalle_reporte.png`, `08_reporte_sincronizado.png`.
- El usuario mostró una verificación local del ZIP con 35 entradas, seis capturas y cero archivos esenciales ausentes. **Esta auditoría no obtuvo los bytes de ese ZIP local**; el conteo no prueba por sí solo que todos los documentos y ejecutables abran o funcionen.

## Observación prioritaria — Evidencias y hashes de ejecutables
Los hashes anotados por Roger en `docs/evidencias/cierre-pruebas.json` son:
- APK: `578af92a84da5ecbb70f663c45dce9d57bf5f205637c0df7314d8e7551f74927`
- JAR: `079e20969d37dbc84ead02015c8097b8c01c77449ff82a60950916247813ceb9`

Los ejecutables compilados por Johan, según `docs/evidencias/compilacion_johan.md`, son:
- APK: `018f04d690549e7b8f767dc1910fe79a1e43463b9ad608517235e48ac88b931f`
- JAR: `d0090f3fa9f71541aad352e5c3a43d4cacd37439e0957d274c496cfd8ac1e5d8`

**No son los mismos archivos.** Las pruebas históricas de Roger pueden referirse al mismo código 0.4.0, pero no certifican los bytes concretos del APK/JAR de Johan. Por eso se actualizó `demo/windows/crear_entrega_johan.ps1`, que ahora distingue los registros de prueba y verifica los hashes de los dos ejecutables *dentro del ZIP generado*. Este nuevo script no ha sido ejecutado en Windows como parte de esta auditoría.

## Observación visual — Capturas omitidas
No se aportaron `05_reporte_pendiente.png` ni `07_sincronizacion.png`. Se decidió continuar con seis imágenes. No se deben afirmar ocho capturas ni decir que esas dos transiciones cuentan con captura específica. La validación de sincronización queda respaldada también por pruebas automáticas, pero estas no sustituyen la evidencia visual.

## Observación documental — Word actualizado
El Word publicado inicialmente afirmaba que no había capturas definitivas y la matriz exigía una captura de pendiente como si ya existiera. Se prepararon correcciones de ambos Word manteniendo cuatro páginas y una página, respectivamente, para reflejar seis capturas y la distinta procedencia de los hashes. **Estas versiones corregidas se suministran al integrante en un ZIP aparte, y deben reemplazar las anteriores en GitHub antes de volver a generar el paquete final.** La versión Markdown y los README ya reflejan las seis imágenes.

## Observación de defensa y autoría
El código 0.4.0 procede de la rama de Roger, mientras que Johan conserva su trabajo anterior en `johan-backend` y registra compilación, pruebas, capturas, documentación y empaquetado en la rama de integración. Se actualizó `docs/aportes.md` para vincular aportes a commits verificables y evitar afirmar que ambos programaron las mismas clases o que la defensa por temas equivale a autoría.

## Revisión no concluyente
- **PowerPoint original:** se verificó que el archivo `CampusReport_Presentacion_Final.pptx` existe en GitHub y el guion de defensa textual cubre 15 minutos. No fue posible abrir y renderizar las diapositivas del archivo binario mediante la conexión disponible; no corresponde afirmar que su apariencia, ortografía o consistencia visual se revisaron diapositiva por diapositiva.
- **ZIP final situado en Windows:** sólo se ha visto la salida de la comprobación de entradas aportada por el integrante. Su contenido binario no se descargó aquí y no fue ejecutado en una instalación independiente.
- **Emulador:** seis capturas están en GitHub; la inspección detallada de su calidad visual no se reprodujo mediante la conexión.
- **Script PowerShell auditado:** revisión estática, pendiente ejecución final en Windows.

## Cierre recomendado
1. Reemplazar los dos DOCX con las versiones corregidas (se entregaron en `CampusReport_Word_Corregidos_Revision_Final.zip`).
2. Hacer `git pull --ff-only origin campusreport-integracion-final` y generar un paquete **nuevo** con `crear_entrega_johan.ps1`. No se debe reutilizar sin cambios el ZIP creado antes de esta auditoría.
3. Confirmar en la terminal que el script indique hashes del APK/JAR verificados dentro del ZIP y seis capturas.
4. Leer las diapositivas de PowerPoint en su equipo y confirmar que su contenido coincide con la versión 0.4.0 y el guion de 15 minutos.
5. Entregar seis capturas, no ocho; respetar la limitación escogida.

**Dictamen:** apto para cierre **condicionado a** los cuatro pasos documentales/técnicos anteriores. No se afirma una certificación de calidad total.
