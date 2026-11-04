# Validación de CampusReport

Versión 0.4.0 · 4 de noviembre de 2026

## Resultados ejecutados

- APK debug y JAR ejecutable compilados con éxito usando JDK 17.
- Backend: 13 pruebas de integración aprobadas, sin fallos ni omisiones.
- Android: 17 pruebas aprobadas, sin fallos ni omisiones, con Robolectric y SQLite nativo.
- Ensayo HTTP adicional: 8 comprobaciones aprobadas con H2 persistente temporal.

## Cobertura

| Grupo | Casos |
|---|---|
| SQLite local, 6 pruebas | Persistencia, validación, anonimato, nombre, migración inicial y descarga transaccional. |
| Sincronización Android, 9 pruebas | Tres reportes offline, referencias locales, reinicio, respuesta perdida, rechazo parcial, confirmación incorrecta, novedades sin duplicar, fusión de referencias, rollback de descarga y migración v2 a v3. |
| Pantallas offline, 1 prueba | Listado y detalle reales leen SQLite reabierta sin una red activa y conservan el estado pendiente. |
| HTTP integrado, 1 prueba | CampusDb y ApiClient reales conectados a un JAR Spring Boot real: tres altas, reenvío explícito, nueva descarga y reapertura de SQLite. |
| Backend, 13 pruebas | Consultas, claves foráneas, validación, anonimato, UUID idempotente, conflictos, altas de referencias y reutilización de nombres existentes. |
| Ensayo del JAR | POST y GET reales, reenvíos, conflictos, nuevo registro y persistencia de reportes y confirmaciones al reiniciar el proceso. |

Resultados por suite: `evidencias/cierre-pruebas.json`.
Ensayo HTTP: `evidencias/cierre-http.json`.
Los archivos de evidencia de la versión anterior se conservan como historial y no describen esta ejecución.

## Reproducir

En `campusreport/backend`:

```powershell
.\mvnw.cmd test package
```

En `campusreport/android`, indicando el JAR para ejecutar también la integración HTTP:

```powershell
$env:CAMPUS_BACKEND_JAR = (Resolve-Path ..\backend\target\campusreport-backend-0.4.0.jar).Path
.\gradlew.bat assembleDebug testDebugUnitTest
```

Sin `CAMPUS_BACKEND_JAR`, la prueba HTTP se omite de forma explícita; las otras 16 pruebas Android siguen ejecutándose.
`SyncHttpIntegrationTest` inicia el Java de la misma JVM de pruebas y selecciona `java.exe` en Windows.

Desde `campusreport`, con Python 3 y Java en PATH:

```powershell
python demo/verify_http.py --jar backend/target/campusreport-backend-0.4.0.jar
```

Las pruebas usan bases separadas o directorios temporales. No cambian la base de demostración.

## Comprobación pendiente en la computadora del equipo

No se ejecutaron aquí el emulador gráfico ni los accesos `.cmd`/PowerShell en Windows.
Roger y Johan deben comprobar instalación, presentación y recorrido de `guia_funcionamiento.md`.
No se debe usar desinstalación ni Wipe Data para probar persistencia.

La actualización de una app existente requiere una firma compatible. El APK de este paquete puede
estar firmado con otra clave debug: si Android rechaza la actualización, conservar el dispositivo
anterior y usar otro AVD con la misma imagen descargada. Las migraciones SQLite fueron verificadas;
una incompatibilidad de firma no se resuelve borrando los datos.

Referencias técnicas: [Android offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first)
y [transacciones Spring](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html).
