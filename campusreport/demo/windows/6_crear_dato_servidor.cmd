@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0herramientas\crear_dato_servidor.ps1"
if errorlevel 1 (
 echo No se pudo crear el dato. Comprueba que el servidor este iniciado.
 pause
 exit /b 1
)
echo Ahora pulsa Sincronizar en CampusReport y busca el reporte recibido.
pause
