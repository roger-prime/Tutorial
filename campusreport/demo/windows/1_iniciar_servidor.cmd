@echo off
setlocal
cd /d "%~dp0"
set "JAVA_EXE="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not defined JAVA_EXE if exist "%ProgramFiles%\Android\Android Studio\jbr\bin\java.exe" set "JAVA_EXE=%ProgramFiles%\Android\Android Studio\jbr\bin\java.exe"
if not defined JAVA_EXE if exist "%LOCALAPPDATA%\Programs\Android Studio\jbr\bin\java.exe" set "JAVA_EXE=%LOCALAPPDATA%\Programs\Android Studio\jbr\bin\java.exe"
if not defined JAVA_EXE for /f "delims=" %%J in ('where java.exe 2^>nul') do if not defined JAVA_EXE set "JAVA_EXE=%%J"
if not defined JAVA_EXE (
 echo No se encontro Java. Usa JDK 17 o 21 o configura JAVA_HOME.
 pause
 exit /b 1
)
if not exist "CampusReport_Servidor.jar" (
 echo Falta CampusReport_Servidor.jar. Extrae el ZIP completo en una carpeta.
 pause
 exit /b 1
)
echo CampusReport - servidor local
echo Espera Started CampusReportApplication y conserva esta ventana abierta.
echo Para detenerlo presiona Ctrl+C. No borres la carpeta data.
"%JAVA_EXE%" -jar "CampusReport_Servidor.jar"
echo El servidor se detuvo. Si hubo un error, revisa el mensaje anterior.
pause
