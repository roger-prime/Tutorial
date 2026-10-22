@echo off
setlocal
call "%~dp0herramientas\configurar_adb.cmd"
if errorlevel 1 goto error
"%CAMPUS_ADB%" -e install -r "%~dp0CampusReport.apk"
if errorlevel 1 (
 echo No se pudo instalar. Si aparece UPDATE_INCOMPATIBLE, las firmas no coinciden.
 echo Usa un emulador nuevo para esta demostracion y conserva el anterior con sus datos.
 goto error
)
"%CAMPUS_ADB%" -e shell am start -n com.example.campusreport/.ui.MainActivity
if errorlevel 1 goto error
echo CampusReport instalado. Mira la pantalla del emulador.
pause
exit /b 0
:error
pause
exit /b 1
