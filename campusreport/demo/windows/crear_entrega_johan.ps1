# CampusReport 0.4.0 — paquete de entrega sin mezclar binarios.
# No borra bases SQLite/H2 ni modifica los ejecutables originales.
$ErrorActionPreference = "Stop"
$repo = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..")).Path
$proyecto = Join-Path $repo "campusreport"
$documentos = Join-Path $env:USERPROFILE "Documents"
$destino = Join-Path $documentos "CampusReport_Entrega_Verificada_Auditada"
$zipFinal = Join-Path $documentos "CampusReport_Entrega_Final_0_4_0_Auditada.zip"

$rama = (& git -C $repo branch --show-current).Trim()
if ($rama -ne "campusreport-integracion-final") {
    throw "Rama incorrecta: $rama. Cambia a campusreport-integracion-final."
}
if (Test-Path -LiteralPath $destino) {
    throw "Ya existe $destino. Conserva esa carpeta y cambia su nombre antes de volver a ejecutar."
}

$binariosPrevios = Join-Path $documentos "CampusReport_Entrega\01_ejecutables"
$apkOrigen = Join-Path $binariosPrevios "CampusReport.apk"
$jarOrigen = Join-Path $binariosPrevios "CampusReport_Servidor.jar"
if (-not (Test-Path -LiteralPath $apkOrigen)) {
    $apkOrigen = Join-Path $proyecto "android\app\build\outputs\apk\debug\app-debug.apk"
}
if (-not (Test-Path -LiteralPath $jarOrigen)) {
    $jarOrigen = Join-Path $proyecto "backend\target\campusreport-backend-0.4.0.jar"
}
if (-not (Test-Path -LiteralPath $apkOrigen) -or -not (Test-Path -LiteralPath $jarOrigen)) {
    throw "No se encuentran el APK y/o el JAR compilados."
}

$shaAPK = (Get-FileHash -LiteralPath $apkOrigen -Algorithm SHA256).Hash
$shaJAR = (Get-FileHash -LiteralPath $jarOrigen -Algorithm SHA256).Hash
$esperadoAPK = "018F04D690549E7B8F767DC1910FE79A1E43463B9AD608517235E48AC88B931F"
$esperadoJAR = "D0090F3FA9F71541AAD352E5C3A43D4CACD37439E0957D274C496CFD8AC1E5D8"
if ($shaAPK -ne $esperadoAPK -or $shaJAR -ne $esperadoJAR) {
    throw "Los hashes de los ejecutables no coinciden con la compilacion registrada por Johan. No empaquetar mezclas de versiones."
}

$bin = Join-Path $destino "01_ejecutables"
$fuentes = Join-Path $destino "02_fuentes"
$docs = Join-Path $destino "03_documentacion"
$evidencias = Join-Path $destino "04_evidencias"
@($bin,$fuentes,$docs,$evidencias,(Join-Path $docs "capturas")) |
    ForEach-Object { New-Item -ItemType Directory -Path $_ -Force | Out-Null }

Copy-Item -LiteralPath $apkOrigen -Destination (Join-Path $bin "CampusReport.apk")
Copy-Item -LiteralPath $jarOrigen -Destination (Join-Path $bin "CampusReport_Servidor.jar")
Copy-Item -Path (Join-Path $proyecto "demo\windows\*.cmd") -Destination $bin
Copy-Item -Path (Join-Path $proyecto "demo\windows\herramientas") -Destination $bin -Recurse

$originales = Join-Path $proyecto "docs\cierre\archivos"
$complementos = Join-Path $proyecto "docs\cierre\complementos"
$documentosNecesarios = @(
    (Join-Path $originales "CampusReport_Presentacion_Final.pptx"),
    (Join-Path $originales "CampusReport_Pruebas_y_Defensa.docx"),
    (Join-Path $complementos "CampusReport_Documentacion_Tecnica_Actualizada.docx"),
    (Join-Path $complementos "CampusReport_Matriz_y_Capturas_Pendientes.docx")
)
foreach ($archivo in $documentosNecesarios) {
    if (-not (Test-Path -LiteralPath $archivo)) { throw "Falta documento: $archivo" }
    Copy-Item -LiteralPath $archivo -Destination $docs
}
Copy-Item -Path (Join-Path $complementos "CampusReport_Diagrama_*") -Destination $docs
Copy-Item -Path (Join-Path $proyecto "docs\cierre\capturas\*.png") -Destination (Join-Path $docs "capturas")
Copy-Item -LiteralPath (Join-Path $proyecto "docs\cierre\capturas\README.md") -Destination (Join-Path $docs "capturas")
Copy-Item -LiteralPath (Join-Path $proyecto "docs\guia_funcionamiento.md") -Destination $docs
Copy-Item -LiteralPath (Join-Path $proyecto "docs\evidencias\compilacion_johan.md") -Destination $evidencias
Copy-Item -LiteralPath (Join-Path $proyecto "docs\evidencias\integracion_http_johan.md") -Destination $evidencias
Copy-Item -LiteralPath (Join-Path $proyecto "docs\evidencias\cierre-pruebas.json") -Destination $evidencias
Copy-Item -LiteralPath (Join-Path $proyecto "docs\evidencias\cierre-http.json") -Destination $evidencias

@"
ACLARACION SOBRE EL ORIGEN DE LAS PRUEBAS — CampusReport 0.4.0

Este paquete contiene ejecutables compilados por Johan.
APK SHA256: $shaAPK
JAR SHA256: $shaJAR
Fuente funcional: rama campusreport-semana1, commit f6293bb (sin cambios de codigo
posteriores en campusreport-integracion-final).

Los archivos cierre-pruebas.json y cierre-http.json contienen resultados historicos
registrados por Roger el 04/11/2026. Sus binarios tienen OTROS HASHES:
APK (Roger): 578AF92A84DA5ECBB70F663C45DCE9D57BF5F205637C0DF7314D8E7551F74927
JAR (Roger): 079E20969D37DBC84EAD02015C8097B8C01C77449FF82A60950916247813CEB9

Las 17 pruebas Android, 13 de backend y 8 verificaciones HTTP de Roger
respaldan ese codigo fuente y su compilacion original, pero NO fueron
re-ejecutadas sobre los ejecutables con hashes de Johan.
Johan registro en integracion_http_johan.md una prueba HTTP integrada
aprobada y los hashes de su compilacion en compilacion_johan.md.
Esta distincion evita atribuir resultados de un ejecutable a otro.
"@ | Set-Content -Path (Join-Path $evidencias "ORIGEN_Y_HASHES_DE_PRUEBAS.txt") -Encoding UTF8


$capturas = @(Get-ChildItem -Path (Join-Path $docs "capturas") -Filter "*.png")
if ($capturas.Count -ne 6) {
    throw "Se esperaban seis capturas reales, pero hay $($capturas.Count). Revisa el directorio antes de entregar."
}

$codigoZip = Join-Path $fuentes "CampusReport_Fuentes.zip"
& git -C $repo archive --format=zip "--output=$codigoZip" "HEAD:campusreport"
if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $codigoZip)) {
    throw "No se pudo crear el ZIP con las fuentes del commit."
}
$commit = (& git -C $repo rev-parse HEAD).Trim()

@"
CAMPUSREPORT 0.4.0 - ENTREGA VERIFICADA
Autores: Johan Escurra y Roger Baez
Codigo: rama campusreport-integracion-final, commit $commit
Binarios: compilacion de Johan, fuente funcional commit 4d5d910 (cambios posteriores: documentacion)

APK SHA256: $shaAPK
JAR SHA256: $shaJAR

Incluye APK, JAR, scripts de demostracion, fuentes, Word principal, matriz,
PowerPoint de Roger, documento de pruebas, diagramas, seis capturas y evidencias.

NO SE INCLUYEN capturas separadas 05_reporte_pendiente ni 07_sincronizacion.
La evidencia automatizada de integracion no debe describirse como captura visual.

Para ejecutar:
1. Extraer el ZIP completo y abrir 01_ejecutables\1_iniciar_servidor.cmd.
2. Esperar a que Spring Boot inicie; abrir Android Studio y el emulador.
3. Ejecutar 01_ejecutables\2_instalar_app.cmd; no desinstalar una app con datos anteriores.
4. Si la firma del APK no coincide con la instalada, usar otro emulador y conservar el anterior.
5. Para conservar datos H2 antiguos: detener los servidores y copiar la carpeta data completa
   al nuevo 01_ejecutables, antes de iniciar el JAR. La ruta de H2 es relativa al directorio
   del proceso que ejecuta el servidor.
6. Abrir 03_documentacion para los documentos y las capturas.
7. Abrir 02_fuentes\CampusReport_Fuentes.zip y luego Android Studio > Open > android.

El servidor usa 127.0.0.1:8080; el emulador accede a 10.0.2.2:8080.
No se ha demostrado despliegue publico.
"@ | Set-Content -Path (Join-Path $destino "LEER_PRIMERO.txt") -Encoding UTF8

if (Test-Path -LiteralPath $zipFinal) {
    throw "Ya existe $zipFinal. Conserva la version previa y renombrala antes de crear otra."
}
Compress-Archive -Path (Join-Path $destino "*") -DestinationPath $zipFinal
# Verificacion del ZIP final: rutas normalizadas y hashes del APK/JAR dentro del archivo.
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zipAbierto = [System.IO.Compression.ZipFile]::OpenRead($zipFinal)
try {
    $nombres = @($zipAbierto.Entries | ForEach-Object { $_.FullName -replace '\\','/' })
    $requeridos = @(
        "01_ejecutables/CampusReport.apk",
        "01_ejecutables/CampusReport_Servidor.jar",
        "02_fuentes/CampusReport_Fuentes.zip",
        "03_documentacion/CampusReport_Documentacion_Tecnica_Actualizada.docx",
        "03_documentacion/CampusReport_Presentacion_Final.pptx",
        "03_documentacion/CampusReport_Pruebas_y_Defensa.docx",
        "04_evidencias/ORIGEN_Y_HASHES_DE_PRUEBAS.txt"
    )
    $faltantes = @($requeridos | Where-Object { $_ -notin $nombres })
    if ($faltantes.Count -gt 0) { throw "ZIP incompleto: $($faltantes -join ', ')" }
    $inZipCapturas = @($nombres | Where-Object { $_ -match '^03_documentacion/capturas/.*\.png
Write-Host ""
Write-Host "PAQUETE FINAL PREPARADO:" -ForegroundColor Green
Write-Host $zipFinal
Write-Host "Commit GitHub: $commit"
Write-Host "Capturas: $($capturas.Count)"
Write-Host "Archivos esenciales: 0 faltantes; hashes APK/JAR dentro del ZIP comprobados."
Write-Host "SHA256 del ZIP: $zipHash"
 })
    if ($inZipCapturas.Count -ne 6) { throw "Cantidad de capturas incorrecta: $($inZipCapturas.Count)" }
    foreach ($par in @(
        @("01_ejecutables/CampusReport.apk", $esperadoAPK),
        @("01_ejecutables/CampusReport_Servidor.jar", $esperadoJAR)
    )) {
        $item = $zipAbierto.Entries | Where-Object {
            ($_.FullName -replace '\\','/') -eq $par[0]
        } | Select-Object -First 1
        if (-not $item) { throw "Falta dentro del ZIP: $($par[0])" }
        $flujo = $item.Open()
        try {
            $sha256 = [System.Security.Cryptography.SHA256]::Create()
            try { $huella = [System.BitConverter]::ToString($sha256.ComputeHash($flujo)).Replace('-','') }
            finally { $sha256.Dispose() }
        }
        finally { $flujo.Dispose() }
        if ($huella -ne $par[1]) { throw "Hash incorrecto dentro del ZIP: $($par[0])" }
    }
}
finally { $zipAbierto.Dispose() }

$zipHash = (Get-FileHash -LiteralPath $zipFinal -Algorithm SHA256).Hash
Write-Host ""
Write-Host "PAQUETE FINAL PREPARADO:" -ForegroundColor Green
Write-Host $zipFinal
Write-Host "Commit GitHub: $commit"
Write-Host "Capturas: $($capturas.Count)"
Write-Host "SHA256 del ZIP: $zipHash"
