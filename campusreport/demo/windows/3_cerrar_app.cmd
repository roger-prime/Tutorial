@echo off
setlocal
call "%~dp0herramientas\configurar_adb.cmd"
if errorlevel 1 goto error
"%CAMPUS_ADB%" -e shell am force-stop com.example.campusreport
if errorlevel 1 goto error
echo CampusReport cerrado por completo. Los datos se conservan.
pause
exit /b 0
:error
pause
exit /b 1
