package com.example.campusreport;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.campusreport.data.CampusDb;
import com.example.campusreport.network.ApiClient;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** La descarga sobrevive a rotaciones. Las Activities se suscriben solo mientras están visibles. */
public class CampusApp extends Application {
    public CampusDb db;
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private final Handler main=new Handler(Looper.getMainLooper());
    private final CopyOnWriteArraySet<Runnable> listeners=new CopyOnWriteArraySet<>();
    private boolean descargando;
    private String resultado="Descargá los lugares y categorías para comenzar.";
    @Override public void onCreate() { super.onCreate();db=new CampusDb(this);db.getWritableDatabase(); }
    public boolean descargando() { return descargando; }
    public String resultado() { return resultado; }
    public void observar(Runnable listener) { listeners.add(listener);listener.run(); }
    public void dejarDeObservar(Runnable listener) { listeners.remove(listener); }
    private void avisar() { for(Runnable r:listeners) r.run(); }
    public void descargar() {
        if(descargando) return;
        descargando=true;resultado="Conectando con Spring Boot…";avisar();
        Log.d("CampusReport","Botón Descargar presionado: GET /api/catalogo");
        io.execute(()->{
            String message;
            try {
                db.guardarCatalogo(ApiClient.descargarCatalogo());
                message="Descarga completa. "+db.lugares().size()+" lugares disponibles sin internet.";
                Log.i("CampusReport",message);
            } catch(Exception e) {
                Log.e("CampusReport","Falló la descarga. Se conservan los datos locales.",e);
                message="No se pudo descargar. Verificá Spring Boot en el puerto 8080. Tus datos locales siguen disponibles.";
            }
            final String text=message;
            main.post(()->{descargando=false;resultado=text;avisar();});
        });
    }
}
