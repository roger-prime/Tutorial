@echo off
setlocal
call "%~dp0herramientas\configurar_adb.cmd"
if errorlevel 1 goto error
"%CAMPUS_ADB%" -e shell am force-stop com.example.campusreport
if errorlevel 1 goto error
for /f %%T in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd_HHmmss"') do set "CAMPUS_STAMP=%%T"
if not defined CAMPUS_STAMP goto error
set "CAMPUS_BACKUP=%~dp0respaldo_android_%CAMPUS_STAMP%"
mkdir "%CAMPUS_BACKUP%"
"%CAMPUS_ADB%" -e exec-out run-as com.example.campusreport cat databases/campusreport.db > "%CAMPUS_BACKUP%\campusreport.db"
if errorlevel 1 goto error
"%CAMPUS_ADB%" -e exec-out run-as com.example.campusreport cat databases/campusreport.db-wal > "%CAMPUS_BACKUP%\campusreport.db-wal" 2>nul
"%CAMPUS_ADB%" -e exec-out run-as com.example.campusreport cat databases/campusreport.db-shm > "%CAMPUS_BACKUP%\campusreport.db-shm" 2>nul
echo Copia de SQLite guardada en %CAMPUS_BACKUP%.
echo Si no habia archivos wal o shm, pueden quedar vacios. No borres el emulador anterior.
pause
exit /b 0
:error
echo No se pudo completar el respaldo. Conserva la app y sus datos originales.
pause
exit /b 1
