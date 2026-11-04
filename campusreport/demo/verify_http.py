"""Prueba el JAR real en un puerto y H2 temporales; nunca modifica la base del usuario."""
from datetime import date
import argparse,json,os,socket,subprocess,tempfile,time,uuid,urllib.request,urllib.error
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--jar',type=Path,required=True);p.add_argument('--java',default='java');p.add_argument('--output',type=Path);a=p.parse_args()
jar=a.jar.resolve();checks={};opener=urllib.request.build_opener(urllib.request.ProxyHandler({}))
with socket.socket() as s:s.bind(('127.0.0.1',0));port=s.getsockname()[1]
base=f'http://127.0.0.1:{port}'
def call(path,body=None):
 req=urllib.request.Request(base+path,data=None if body is None else json.dumps(body).encode(),headers={'Content-Type':'application/json'})
 try:
  with opener.open(req,timeout=5) as r:return r.status,json.load(r)
 except urllib.error.HTTPError as e:return e.code,json.load(e)
def item(name):return {'uuid':str(uuid.uuid4()),'nombre':name}
def report(place,cat,description):return dict(uuid=str(uuid.uuid4()),lugarId=place,categoriaId=cat,descripcion=description,fechaCreacion='2026-10-30T12:00:00Z',anonimo=True,autor=None)
with tempfile.TemporaryDirectory(prefix='campus-http-') as temp:
 folder=Path(temp);log=(folder/'server.log').open('w');process=None
 def start():
  proc=subprocess.Popen([a.java,'-jar',str(jar),f'--server.port={port}',f'--spring.datasource.url=jdbc:h2:file:{folder / "db"}'],stdout=log,stderr=subprocess.STDOUT)
  for _ in range(150):
   try:
    if call('/api/catalogo')[0]==200:return proc
   except Exception:pass
   if proc.poll() is not None:raise RuntimeError((folder/'server.log').read_text())
   time.sleep(.2)
  proc.terminate();proc.wait();raise RuntimeError('Servidor no inició')
 def stop(proc):
  proc.terminate()
  try:proc.wait(timeout=10)
  except subprocess.TimeoutExpired:proc.kill();proc.wait()
 try:
  process=start();place=item('Sala HTTP');category=item('Equipo HTTP')
  status,pl=call('/api/lugares',place);assert status==201
  status,cat=call('/api/categorias',category);assert status==201
  assert call('/api/lugares',place)==(200,pl);assert call('/api/categorias',category)==(200,cat)
  checks['referencias_y_reenvios']=True
  assert call('/api/lugares',{**place,'nombre':'Contenido diferente'})[0]==409;checks['conflicto_referencia']=True
  records=[report(pl['id'],cat['id'],f'El equipo no funciona reporte {n}') for n in range(3)]
  for r in records:assert call('/api/reportes',r)==(201,r)
  checks['tres_reportes_recibidos']=True
  for r in records:assert call('/api/reportes',r)==(200,r)
  assert len(call('/api/reportes')[1])==3;checks['reenvio_sin_duplicados']=True
  assert call('/api/reportes',{**records[0],'descripcion':'Cambio de contenido prohibido'})[0]==409;checks['conflicto_reporte']=True
  assert call('/api/reportes',{**report(999,1,'Referencia inexistente'),'descripcion':'corta'})[0]==400;checks['validacion']=True
  fresh=report(1,1,'Registro creado en el servidor');assert call('/api/reportes',fresh)[0]==201
  assert len(call('/api/reportes')[1])==4;checks['novedad_descargable']=True
  stop(process);process=None;process=start();assert len(call('/api/reportes')[1])==4;assert call('/api/lugares',place)==(200,pl);checks['persistencia_tras_reinicio']=True
 finally:
  if process is not None:stop(process)
  log.close()
result={'version':'0.4.0','fecha':date.today().isoformat(),'entorno':'HTTP real y H2 temporal persistente','comprobaciones':checks}
if a.output:a.output.parent.mkdir(parents=True,exist_ok=True);a.output.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n')
print(json.dumps(result,ensure_ascii=False,indent=2))
