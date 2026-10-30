package com.example.campusreport;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.campusreport.data.CampusDb;
import com.example.campusreport.network.ApiClient;
import com.example.campusreport.sync.SyncManager;
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
    private String resultado="Guardá tus datos y tocá Sincronizar cuando tengas conexión.";
    @Override public void onCreate() { super.onCreate();db=new CampusDb(this);db.getWritableDatabase(); }
    public boolean descargando() { return descargando; }
    public String resultado() { return resultado; }
    public void observar(Runnable listener) { listeners.add(listener);listener.run(); }
    public void dejarDeObservar(Runnable listener) { listeners.remove(listener); }
    private void avisar() { for(Runnable r:listeners) r.run(); }
    public void sincronizar() {
        if(descargando) return;
        descargando=true;resultado="Sincronizando…";avisar();
        Log.d("CampusReport","Botón Sincronizar presionado");
        io.execute(()->{
            SyncManager.Result result=new SyncManager(db,new ApiClient()).sincronizar();
            final String text=result.mensaje();Log.i("CampusReport",text);
            main.post(()->{descargando=false;resultado=text;avisar();});
        });
    }
}
