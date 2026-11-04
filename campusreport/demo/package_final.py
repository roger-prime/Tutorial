"""Arma la entrega desde un commit y binarios verificados. No incluye datos del usuario."""
import argparse, hashlib, json, subprocess, zipfile
from pathlib import Path

root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument('--demo', type=Path, required=True, help='ZIP ejecutable CampusReport 0.4.0')
parser.add_argument('--documentos', type=Path, help='Usar documentos externos en lugar de los del commit')
parser.add_argument('--output', type=Path, required=True)
args = parser.parse_args()

def git(*args):
    return subprocess.check_output(['git', *args], cwd=root)
commit = git('rev-parse', 'HEAD').decode().strip()
prefix = git('rev-parse', '--show-prefix').decode().strip()
read = lambda p: git('show', commit+':'+prefix+p)
sha = lambda data: hashlib.sha256(data).hexdigest()
files = {}
expected = json.loads(read('docs/evidencias/cierre-pruebas.json'))['binarios_entregados']
with zipfile.ZipFile(args.demo) as demo:
    for name in ('CampusReport.apk', 'CampusReport_Servidor.jar'):
        if sha(demo.read(name)) != expected[name]:
            raise ValueError('Versión distinta del binario probado: '+name)
        files['01_ejecutar/'+name] = demo.read(name)
listing = git('ls-tree', '-rz', '--full-tree', '--name-only', commit+':'+prefix.rstrip('/')).decode().rstrip('\0').split('\0')
assert {'android/gradlew.bat','android/gradle/wrapper/gradle-wrapper.jar','backend/mvnw.cmd','backend/src/main/resources/schema.sql'}.issubset(listing)
for name in listing:
    if name.startswith('docs/cierre/archivos/'):
        continue
    files['02_fuentes/'+name] = read(name)
    if name.startswith('demo/windows/') and name.endswith(('.cmd','.ps1')):
        content = files['02_fuentes/'+name]
        if name.endswith('.cmd'): content = content.replace(b'\r\n',b'\n').replace(b'\n',b'\r\n')
        files['01_ejecutar/'+name[len('demo/windows/'):]] = content
for name in ('CampusReport_Documentacion_Tecnica.docx','CampusReport_Pruebas_y_Defensa.docx','CampusReport_Presentacion_Final.pptx'):
    files['03_documentos/'+name] = (args.documentos/name).read_bytes() if args.documentos else read('docs/cierre/archivos/'+name)
for name in ('cierre-http.json','cierre-pruebas.json'):
    files['04_resultados/'+name] = read('docs/evidencias/'+name)
intro = f'''CAMPUSREPORT · ENTREGA FINAL
Roger Báez y Johan Escurra · Proyecto Práctico TIC
Versión ejecutable: 0.4.0. Fuentes de Git: {commit}
https://github.com/roger-prime/Tutorial/tree/{commit}/campusreport

1. Extraer todo en una carpeta nueva. Conservar el emulador anterior y la carpeta data.
2. Fuentes: Android Studio > File > Open > 02_fuentes/android (contiene settings.gradle).
   Esperar Gradle Sync. Gradle JDK 17 o 21; SDK de compilación 35.
   El emulador puede seguir siendo API 37. Dejar un solo emulador iniciado.
3. En 01_ejecutar, doble clic en 1_iniciar_servidor.cmd. Esperar Started CampusReportApplication.
   En Windows, comprobar http://localhost:8080/api/catalogo y dejar el servidor abierto.
4. En Android Studio seleccionar app + el dispositivo y pulsar Run (triángulo).
   Alternativa sin compilar: en 01_ejecutar, doble clic en 2_instalar_app.cmd.
   Si la instalación anterior tiene datos, se puede respaldar antes con el acceso 0.
   Si hay diferencia de firma: conservar ese emulador y usar otro con la misma imagen.
5. Abrir CampusReport y pulsar Sincronizar. Para el ensayo seguir 03_documentos:
   Documentación técnica, Pruebas y defensa, Presentación final.
6. Para trabajar con Git: clonar la rama campusreport-semana1. La carpeta 02_fuentes
   contiene el código completo, sin historial Git. No abrir el proyecto Tutorial de la raíz.
7. Las bases de datos se crean al ejecutar. SQL y configuración en
   02_fuentes/backend/src/main/resources. Solo un servidor puede usar el puerto 8080.
   Para trasladar datos de un servidor anterior, copiar su carpeta data completa al nuevo
   directorio 01_ejecutar con ambos servidores detenidos.
8. Las pruebas automatizadas están en 04_resultados. El ensayo y las capturas de Windows
   corresponden a los integrantes. Conservar evidencia de los aportes individuales reales.
'''
files['LEER_PRIMERO.txt'] = intro.replace('\n','\r\n').encode('utf-8-sig')
files['SHA256SUMS.txt'] = ''.join(f'{sha(data)}  {name}\n' for name,data in sorted(files.items())).encode()
args.output.parent.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(args.output,'w',zipfile.ZIP_DEFLATED) as z:
    for name,data in sorted(files.items()): z.writestr(name,data)
with zipfile.ZipFile(args.output) as z:
    assert z.testzip() is None
    assert all(sha(z.read(n))==h for h,n in (x.split('  ',1) for x in z.read('SHA256SUMS.txt').decode().splitlines()))
print(f'{args.output}: {len(files)} archivos verificados; commit {commit}.')
