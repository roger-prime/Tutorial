package com.example.campusreport;
import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.example.campusreport.data.CampusDb;
import com.example.campusreport.sync.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import java.io.IOException;
import java.util.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=28,application=Application.class) @SQLiteMode(SQLiteMode.Mode.NATIVE)
public class SyncManagerTest {
 Context context;CampusDb db;Fake api;static final String NAME="sync-test.db";
 @Before public void setup() throws Exception {context=RuntimeEnvironment.getApplication();context.deleteDatabase(NAME);db=new CampusDb(context,NAME);api=new Fake();db.guardarCatalogo(api.catalogo());}
 @After public void cleanup() {db.close();context.deleteDatabase(NAME);}
 SyncManager.Result sync(){return new SyncManager(db,api).sincronizar();}
 String report(){return db.crearReporte(1,1,"La lámpara no funciona");}
 static JSONObject copy(JSONObject r) throws JSONException {return new JSONObject(r.toString());}
 static class Fake implements SyncTransport {
  Map<String,JSONObject> stored=new LinkedHashMap<>();Map<String,JSONObject> receipts=new HashMap<>();
  JSONArray places=new JSONArray(),categories=new JSONArray();String reject="";boolean loseReply,wrongAck,rejectRefs,offline,badDownload;
  Fake() throws Exception {places.put(new JSONObject().put("id",1).put("nombre","Aula 1"));categories.put(new JSONObject().put("id",1).put("nombre","Electricidad"));}
  void check() throws IOException {if(offline) throw new IOException("offline");}
  public JSONObject enviarReferencia(boolean lugar,JSONObject r) throws IOException,JSONException {
   check();if(rejectRefs) throw new Rechazo(400);String key=(lugar?"l":"c")+r.getString("uuid");if(receipts.containsKey(key)) return copy(receipts.get(key));
   JSONArray rows=lugar?places:categories;long id=rows.length()+1;
   for(int i=0;i<rows.length();i++) if(rows.getJSONObject(i).getString("nombre").equalsIgnoreCase(r.getString("nombre"))) id=rows.getJSONObject(i).getLong("id");
   JSONObject ack=copy(r).put("id",id);if(id>rows.length()) rows.put(new JSONObject().put("id",id).put("nombre",r.getString("nombre")));receipts.put(key,copy(ack));return ack;
  }
  public JSONObject enviarReporte(JSONObject r) throws IOException,JSONException {
   check();String uuid=r.getString("uuid");if(uuid.equals(reject)) throw new Rechazo(409);
   if(stored.containsKey(uuid)) ReportJson.confirmar(r,stored.get(uuid));else stored.put(uuid,copy(r));
   if(loseReply){loseReply=false;throw new IOException("reply lost");}
   JSONObject ack=copy(stored.get(uuid));if(wrongAck) ack.put("descripcion","Contenido diferente confirmado");return ack;
  }
  public JSONObject catalogo() throws IOException,JSONException {check();return new JSONObject().put("lugares",places).put("categorias",categories);}
  public JSONArray reportes() throws IOException,JSONException {check();JSONArray rows=new JSONArray();for(JSONObject r:stored.values()) rows.put(copy(r));if(badDownload) rows.put(new JSONObject().put("uuid","mal"));return rows;}
 }
 @Test public void offlineThreeRecordsSyncAndReopenWithoutDuplicates() throws Exception {
  long p=db.crearLugar("Sala nueva"),c=db.crearCategoria("Equipo nuevo");
  for(int i=0;i<3;i++) db.crearReporte(p,c,"El equipo no funciona "+i,i!=1,i==1?"Roger Báez":null);
  api.offline=true;sync();assertEquals(3,db.pendientes());assertEquals(2,db.referenciasPendientesCount());
  db.close();db=new CampusDb(context,NAME);api.offline=false;assertEquals(3,sync().enviados);
  assertEquals(0,db.pendientes());assertEquals(3,api.stored.size());assertEquals(0,sync().enviados);assertEquals(3,db.reportes().size());
  db.close();db=new CampusDb(context,NAME);assertEquals(0,db.pendientes());assertEquals(3,db.reportes().size());
 }
 @Test public void lostResponseRetriesSameUuid() throws Exception {
  String id=report();api.loseReply=true;sync();assertFalse(db.reporte(id).sincronizado);assertEquals(1,api.stored.size());
  assertEquals(1,sync().enviados);assertTrue(db.reporte(id).sincronizado);assertEquals(1,api.stored.size());
 }
 @Test public void rejectedOneDoesNotBlockOtherAndDownloadCannotOverwritePending() throws Exception {
  String bad=report(),good=report();api.reject=bad;JSONObject remote=db.reporteParaEnviar(bad);remote.put("descripcion","Otro contenido desde el servidor");api.stored.put(bad,remote);
  assertEquals(1,sync().enviados);assertFalse(db.reporte(bad).sincronizado);assertTrue(db.reporte(good).sincronizado);assertEquals("La lámpara no funciona",db.reporte(bad).descripcion);
 }
 @Test public void invalidAcknowledgmentNeverConfirmsEvenIfGetReturnsRecord() {
  String id=report();api.wrongAck=true;assertEquals(1,sync().rechazados);assertFalse(db.reporte(id).sincronizado);
 }
 @Test public void serverRecordDownloadedOnceAndNeverReuploaded() throws Exception {
  String id=report();JSONObject remote=db.reporteParaEnviar(id);db.getWritableDatabase().delete("reporte","uuid=?",new String[]{id});api.stored.put(id,remote);
  assertEquals(1,sync().descargados);assertTrue(db.reporte(id).sincronizado);assertEquals(0,sync().descargados);assertEquals(0,sync().enviados);assertEquals(1,db.reportes().size());
 }
 @Test public void rejectedReferencesPreserveDependentReport() {
  long p=db.crearLugar("Sala nueva"),c=db.crearCategoria("Equipo nuevo");String id=db.crearReporte(p,c,"El equipo no funciona");api.rejectRefs=true;sync();assertFalse(db.reporte(id).sincronizado);assertEquals(0,api.stored.size());assertEquals(2,db.referenciasPendientesCount());
 }
 @Test public void matchingServerReferenceMergesWithoutBreakingReports() throws Exception {
  long p=db.crearLugar("Sala nueva");String id=db.crearReporte(p,1,"La lámpara no funciona");
  api.places.put(new JSONObject().put("id",2).put("nombre","Sala nueva"));db.guardarCatalogo(api.catalogo());
  sync();assertEquals(2,db.lugares().size());assertTrue(db.reporte(id).sincronizado);assertEquals(2,api.stored.get(id).getLong("lugarId"));
 }
 @Test public void malformedDownloadRollsBackEntireDownload() throws Exception {
  api.places.put(new JSONObject().put("id",2).put("nombre","Sala nueva"));api.badDownload=true;sync();assertEquals(1,db.lugares().size());
 }
 @Test public void migrationFromV2PreservesStableUuidAndSyncs() throws Exception {
  db.close();context.deleteDatabase(NAME);
  try(SQLiteDatabase old=context.openOrCreateDatabase(NAME,0,null)) {
   old.execSQL("CREATE TABLE lugar(id INTEGER PRIMARY KEY,nombre TEXT NOT NULL,uuid TEXT UNIQUE)");old.execSQL("CREATE TABLE categoria(id INTEGER PRIMARY KEY,nombre TEXT NOT NULL,uuid TEXT UNIQUE)");
   old.execSQL("CREATE TABLE reporte(uuid TEXT PRIMARY KEY,lugar_id INTEGER NOT NULL REFERENCES lugar(id),categoria_id INTEGER NOT NULL REFERENCES categoria(id),descripcion TEXT NOT NULL,fecha_creacion TEXT NOT NULL,sincronizado INTEGER NOT NULL DEFAULT 0,anonimo INTEGER NOT NULL DEFAULT 1,autor TEXT)");
   old.execSQL("INSERT INTO lugar VALUES(-1,'Sala nueva','11111111-1111-4111-8111-111111111111')");old.execSQL("INSERT INTO categoria VALUES(1,'Electricidad',NULL)");
   old.execSQL("INSERT INTO reporte VALUES('22222222-2222-4222-8222-222222222222',-1,1,'La lámpara no funciona','2026-10-30T12:00:00Z',0,1,NULL)");old.setVersion(2);
  }
  db=new CampusDb(context,NAME);assertEquals("11111111-1111-4111-8111-111111111111",db.referenciasPendientes(true).get(0).getString("uuid"));
  assertEquals(1,sync().enviados);assertTrue(db.reporte("22222222-2222-4222-8222-222222222222").sincronizado);
 }
}
