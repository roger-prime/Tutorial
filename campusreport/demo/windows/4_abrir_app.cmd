@echo off
setlocal
call "%~dp0herramientas\configurar_adb.cmd"
if errorlevel 1 goto error
"%CAMPUS_ADB%" -e shell am start -n com.example.campusreport/.ui.MainActivity
if errorlevel 1 goto error
echo Mira el emulador y entra en Reportes.
pause
exit /b 0
:error
pause
exit /b 1
