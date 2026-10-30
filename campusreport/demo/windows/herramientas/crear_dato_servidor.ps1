$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080'
$folder = Join-Path (Split-Path $PSScriptRoot -Parent) 'evidencias_demo'
$file = Join-Path $folder 'dato_servidor.json'
try {
    if (-not (Test-Path $file)) {
        $catalog = Invoke-RestMethod -Uri "$base/api/catalogo" -TimeoutSec 10
        if ($catalog.lugares.Count -eq 0 -or $catalog.categorias.Count -eq 0) { throw 'Faltan lugares o categorias.' }
        $record = [ordered]@{
            uuid = [guid]::NewGuid().ToString()
            lugarId = $catalog.lugares[0].id
            categoriaId = $catalog.categorias[0].id
            descripcion = 'Reporte creado directamente en el servidor para la demostracion'
            fechaCreacion = [DateTime]::UtcNow.ToString('o')
            anonimo = $true
            autor = $null
        }
        New-Item -ItemType Directory -Path $folder -Force | Out-Null
        [IO.File]::WriteAllText($file, ($record | ConvertTo-Json), [Text.UTF8Encoding]::new($false))
    }
    $body = [IO.File]::ReadAllText($file, [Text.Encoding]::UTF8)
    Invoke-RestMethod -Method Post -Uri "$base/api/reportes" -ContentType 'application/json; charset=utf-8' -Body ([Text.Encoding]::UTF8.GetBytes($body)) -TimeoutSec 10 | Format-List
    Write-Host 'Confirmado. Repetir este acceso reutiliza el UUID y no duplica el reporte.'
    Start-Process "$base/api/reportes"
} catch {
    Write-Host $_.Exception.Message
    exit 1
}
