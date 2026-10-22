"""Empaqueta los binarios compilados y los accesos de Windows, sin bases de datos personales."""
from pathlib import Path
import argparse
import hashlib
import zipfile

root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument('--guide', type=Path, help='Guía Word opcional')
parser.add_argument('--output', type=Path, default=root / 'dist/CampusReport_Demostracion.zip')
args = parser.parse_args()
files = {
    'CampusReport.apk': root / 'android/app/build/outputs/apk/debug/app-debug.apk',
    'CampusReport_Servidor.jar': root / 'backend/target/campusreport-backend-0.3.0.jar',
    'LEEME.txt': root / 'demo/LEEME.txt',
    'reporte_ejemplo.json': root / 'demo/reporte_ejemplo.json',
}
for path in sorted((root / 'demo/windows').rglob('*.cmd')):
    files[path.relative_to(root / 'demo/windows').as_posix()] = path
if args.guide:
    files['CampusReport_Guia_Funcionamiento.docx'] = args.guide
for path in files.values():
    if not path.is_file():
        raise SystemExit(f'Falta el archivo compilado o solicitado: {path}')
args.output.parent.mkdir(parents=True, exist_ok=True)
with zipfile.ZipFile(args.output, 'w', zipfile.ZIP_DEFLATED) as archive:
    checksums = []
    for name, path in files.items():
        data = path.read_bytes()
        archive.writestr(name, data)
        checksums.append(f'{hashlib.sha256(data).hexdigest()}  {name}')
    archive.writestr('SHA256SUMS.txt', '\n'.join(checksums) + '\n')
with zipfile.ZipFile(args.output) as archive:
    if archive.testzip():
        raise SystemExit('El ZIP no pasó la comprobación de integridad')
print(args.output)
