package com.example.campusreport;
import android.app.Application;
import android.content.Context;
import com.example.campusreport.data.CampusDb;
import com.example.campusreport.network.ApiClient;
import com.example.campusreport.sync.SyncManager;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import java.net.ServerSocket;
import java.nio.file.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;
/** SQLite real + transporte HTTP real + proceso Spring Boot con H2 temporal. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=28,application=Application.class) @SQLiteMode(SQLiteMode.Mode.NATIVE)
public class SyncHttpIntegrationTest {
 @Test public void completeCycleAgainstRealBackend() throws Exception {
  String jar=System.getenv("CAMPUS_BACKEND_JAR");Assume.assumeTrue("Configurar CAMPUS_BACKEND_JAR para la prueba HTTP integrada",jar!=null&&Files.isRegularFile(Paths.get(jar)));
  int port;try(ServerSocket socket=new ServerSocket(0)){port=socket.getLocalPort();}
  Path dir=Files.createTempDirectory("campus-http-");String executable=System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT).contains("win")?"java.exe":"java";String javaExe=System.getProperty("java.home")+"/bin/"+executable;
  Process server=new ProcessBuilder(javaExe,"-jar",jar,"--server.port="+port,"--spring.datasource.url=jdbc:h2:file:"+dir.resolve("db")).redirectErrorStream(true).redirectOutput(dir.resolve("server.log").toFile()).start();
  Context context=RuntimeEnvironment.getApplication();String name="http-test.db";context.deleteDatabase(name);CampusDb db=new CampusDb(context,name);
  try {
   ApiClient api=new ApiClient("http://127.0.0.1:"+port);boolean ready=false;
   for(int i=0;i<150;i++){try {api.catalogo();ready=true;break;}catch(Exception e){if(!server.isAlive()) break;Thread.sleep(200);}}
   assertTrue("El servidor debe iniciar; revisar "+dir.resolve("server.log"),ready);
   SyncManager manager=new SyncManager(db,api);manager.sincronizar();assertEquals(4,db.lugares().size());
   long p=db.crearLugar("Sala HTTP"),c=db.crearCategoria("Equipo HTTP");
   String first=null;for(int i=0;i<3;i++){String id=db.crearReporte(p,c,"El equipo no funciona "+i,i!=1,i==1?"Roger Báez":null);if(first==null)first=id;}
   db.close();db=new CampusDb(context,name);assertEquals(3,db.pendientes());manager=new SyncManager(db,api);
   assertEquals(3,manager.sincronizar().enviados);assertEquals(0,db.pendientes());assertEquals(3,api.reportes().length());
   // Reenvío HTTP explícito, además de pulsar Sincronizar otra vez.
   JSONObject sent=api.reportes().getJSONObject(0);assertEquals(sent.getString("uuid"),api.enviarReporte(sent).getString("uuid"));
   assertEquals(0,manager.sincronizar().enviados);assertEquals(3,api.reportes().length());
   JSONObject fresh=new JSONObject(sent.toString()).put("uuid",UUID.randomUUID().toString()).put("descripcion","Dato creado directamente en el servidor");api.enviarReporte(fresh);
   assertEquals(1,manager.sincronizar().descargados);assertEquals(0,manager.sincronizar().descargados);assertEquals(4,db.reportes().size());
   db.close();db=new CampusDb(context,name);assertEquals(4,db.reportes().size());assertTrue(db.reporte(fresh.getString("uuid")).sincronizado);
  }finally {
   db.close();context.deleteDatabase(name);server.destroy();if(!server.waitFor(10,TimeUnit.SECONDS)) server.destroyForcibly().waitFor();
   try(var paths=Files.walk(dir)){paths.sorted(java.util.Comparator.reverseOrder()).forEach(p->{try{Files.delete(p);}catch(Exception ignored){}});}
  }
 }
}
