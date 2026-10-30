
package com.example.campusreport;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.campusreport.data.CampusDb;
import com.example.campusreport.network.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CampusApp extends Application {

    public CampusDb db;

    private final ExecutorService io =
            Executors.newSingleThreadExecutor();

    private final Handler main =
            new Handler(Looper.getMainLooper());

    private final CopyOnWriteArraySet<Runnable> listeners =
            new CopyOnWriteArraySet<>();

    private boolean descargando = false;
    private boolean sincronizando = false;

    private String resultado =
            "Agregá lugares y categorías o tocá Actualizar.";

    @Override
    public void onCreate() {
        super.onCreate();
        db = new CampusDb(this);
        db.getWritableDatabase();
    }

    public boolean descargando() {
        return descargando;
    }

    public boolean sincronizando() {
        return sincronizando;
    }

    public String resultado() {
        return resultado;
    }

    public void observar(Runnable listener) {
        listeners.add(listener);
        listener.run();
    }

    public void dejarDeObservar(Runnable listener) {
        listeners.remove(listener);
    }

    private void avisar() {
        for (Runnable r : listeners) {
            r.run();
        }
    }

    // Descarga lugares y categorías
    public void descargar() {

        if (descargando) return;

        descargando = true;
        resultado = "Actualizando lugares y categorías...";
        avisar();

        io.execute(() -> {

            String mensaje;

            try {
                db.guardarCatalogo(
                        ApiClient.descargarCatalogo()
                );

                mensaje = "Lugares y categorías actualizados.";

            } catch (Exception e) {

                Log.e("CampusReport",
                        "Error al actualizar catálogo", e);

                mensaje = "No se pudo actualizar el catálogo.";
            }

            final String resultadoFinal = mensaje;

            main.post(() -> {
                descargando = false;
                resultado = resultadoFinal;
                avisar();
            });
        });
    }

    // Sincronización en ambos sentidos
    public void sincronizarReportes() {

        if (sincronizando) return;

        sincronizando = true;
        resultado = "Sincronizando reportes...";
        avisar();

        io.execute(() -> {

            int enviados = 0;
            int descargados = 0;
            String mensaje;

            try {

                // 1. Actualizar lugares y categorías
                db.guardarCatalogo(
                        ApiClient.descargarCatalogo()
                );

                // 2. Obtener reportes pendientes de SQLite
                List<JSONObject> pendientes =
                        db.reportesPendientesParaEnvio();

                // 3. Enviar pendientes al servidor
                for (JSONObject reporte : pendientes) {

                    ApiClient.enviarReporte(reporte);

                    boolean actualizado =
                            db.marcarSincronizado(
                                    reporte.getString("uuid")
                            );

                    if (!actualizado) {
                        throw new IllegalStateException(
                                "No se pudo confirmar el reporte en SQLite"
                        );
                    }

                    enviados++;
                }

                // 4. Descargar todos los reportes del servidor
                JSONArray reportesServidor =
                        ApiClient.descargarReportes();

                // 5. Guardar únicamente los nuevos en SQLite
                descargados =
                        db.guardarReportesServidor(reportesServidor);

                mensaje = "Sincronización completada.\n"
                        + "Enviados: " + enviados
                        + "\nRecibidos nuevos: " + descargados
                        + "\nPendientes: " + db.pendientes();

                Log.i("CampusReport", mensaje);

            } catch (Exception e) {

                Log.e("CampusReport",
                        "Error durante la sincronización", e);

                mensaje = "Sincronización incompleta.\n"
                        + "Enviados confirmados: " + enviados
                        + "\nPendientes: " + db.pendientes()
                        + "\nLos datos locales se conservaron.";
            }

            final String resultadoFinal = mensaje;

            main.post(() -> {
                sincronizando = false;
                resultado = resultadoFinal;
                avisar();
            });
        });
    }
}
