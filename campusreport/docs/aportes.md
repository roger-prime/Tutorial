# Aportes y trazabilidad — CampusReport 0.4.0

Integrantes: Roger Báez y Johan Escurra. Este documento **registra evidencias de GitHub**, no asigna autoría por temas de exposición. Los desarrollos asistidos por IA deben distinguirse del trabajo que cada integrante ejecutó, comprendió y defendió.

## Roger — rama principal 0.4.0

La rama `campusreport-semana1` contiene el código funcional utilizado en esta entrega. Sus commits identificables bajo `roger-prime` incluyen:
- `351e4566`: desarrollo offline, persistencia local y preparación de demo.
- `ef603b95`: sincronización bidireccional con UUID estables, pruebas e idempotencia.
- `f6293bbb`: pruebas offline, documentación técnica, PowerPoint, defensa y empaquetado original.

La versión 0.4.0 registra en `docs/evidencias/cierre-pruebas.json` 17 pruebas Android y 13 backend, además de 8 comprobaciones HTTP en `cierre-http.json`, ejecutadas y documentadas el 4 de noviembre. Los hashes que constan allí pertenecen a los ejecutables de ese registro histórico.

## Johan — desarrollo anterior e integración final

- Rama histórica `johan-backend` (código 0.3.0): avances anteriores de API y persistencia. **No se hizo una fusión ciega de sus controladores sobre 0.4.0**, porque la nueva rama ya implementaba las rutas y la sincronización.
- `48005a9`: registro de APK y JAR compilados en Windows con sus respectivos hashes SHA-256.
- `0e34dcc`: registro de una ejecución aprobada de `SyncHttpIntegrationTest` (1 prueba, sin fallos).
- `c2ff288`, `8aa27df`, `4b946d2`: carga de documentos, diagramas y seis capturas reales del emulador.
- `2daabb0` y posteriores: script PowerShell para generar la entrega, comprobaciones y documentación de trazabilidad.

Los documentos técnicos y los complementos se elaboraron **con asistencia de ChatGPT** y se incorporaron mediante GitHub; esto no equivale a reclamar autoría exclusiva del código 0.4.0.

## Cómo demostrar los aportes sin falsear la autoría

En GitHub se pueden consultar las ramas y commits indicados. Para obtener el historial desde un clon:

```powershell
git log --format="%h %an %s" -- campusreport
git log --oneline johan-backend -- campusreport
git log --oneline campusreport-semana1 -- campusreport
```

En la defensa, Roger puede explicar el recorrido Android → SQLite → Sincronización y Johan el backend, el contrato REST, las confirmaciones por UUID y las verificaciones que ejecutó. **Explicar una parte de la arquitectura no demuestra por sí solo que se programó toda esa parte**: vincular cada afirmación de autoría con su commit.

## Distinción obligatoria entre binarios y pruebas

Los hashes de la compilación de Johan son distintos de los del registro de Roger. Los resultados históricos de Roger respaldan su código y sus ejecutables; no certifican la identidad de los binarios compilados por Johan. La entrega auditada incluye `04_evidencias/ORIGEN_Y_HASHES_DE_PRUEBAS.txt` para dejarlo explícito.

Quedan sin captura específica la pantalla de reporte pendiente y el mensaje de Sincronizar. El repositorio incluye seis capturas reales, sin inventar pruebas visuales adicionales.
