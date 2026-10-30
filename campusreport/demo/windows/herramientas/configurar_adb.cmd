@echo off
set "CAMPUS_ADB="
if defined ANDROID_HOME if exist "%ANDROID_HOME%\platform-tools\adb.exe" set "CAMPUS_ADB=%ANDROID_HOME%\platform-tools\adb.exe"
if not defined CAMPUS_ADB if defined ANDROID_SDK_ROOT if exist "%ANDROID_SDK_ROOT%\platform-tools\adb.exe" set "CAMPUS_ADB=%ANDROID_SDK_ROOT%\platform-tools\adb.exe"
if not defined CAMPUS_ADB if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" set "CAMPUS_ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
if not defined CAMPUS_ADB for /f "delims=" %%A in ('where adb.exe 2^>nul') do if not defined CAMPUS_ADB set "CAMPUS_ADB=%%A"
if not defined CAMPUS_ADB (
 echo No se encontro adb. Instala Android SDK Platform-Tools desde SDK Manager.
 echo Si tu SDK esta en otra carpeta, configura ANDROID_HOME con esa ruta.
 exit /b 1
)
"%CAMPUS_ADB%" -e get-state >nul 2>&1
if errorlevel 1 (
 echo Abre exactamente un emulador en Device Manager y espera la pantalla de inicio.
 echo Si aparece offline, espera a que termine de iniciar.
 "%CAMPUS_ADB%" devices
 exit /b 1
)
set "CAMPUS_BOOT_FILE=%TEMP%\campusreport_boot_%RANDOM%.txt"
"%CAMPUS_ADB%" -e shell getprop sys.boot_completed > "%CAMPUS_BOOT_FILE%"
set "CAMPUS_BOOT="
set /p CAMPUS_BOOT=<"%CAMPUS_BOOT_FILE%"
del "%CAMPUS_BOOT_FILE%" >nul 2>&1
if not "%CAMPUS_BOOT%"=="1" (
 echo El emulador todavia esta iniciando. Espera la pantalla de inicio y vuelve a intentar.
 exit /b 1
)
exit /b 0
