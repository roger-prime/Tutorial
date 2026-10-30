
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

    // Descargar lugares y categorías del servidor
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

    // Sincronización completa de CampusReport
    public void sincronizarReportes() {

        if (sincronizando) return;

        sincronizando = true;
        resultado = "Sincronizando datos...";
        avisar();

        io.execute(() -> {

            int referenciasEnviadas = 0;
            int reportesEnviados = 0;
            int reportesDescargados = 0;

            String mensaje;

            try {

                // 1. Descargar catálogo del servidor
                db.guardarCatalogo(
                        ApiClient.descargarCatalogo()
                );

                // 2. Buscar lugares y categorías locales
                List<JSONObject> referencias =
                        db.referenciasLocalesPendientes();

                // 3. Enviar referencias al servidor
                for (JSONObject referencia : referencias) {

                    JSONObject respuesta =
                            ApiClient.enviarReferenciaLocal(
                                    referencia
                            );

                    // Actualizar el ID temporal en SQLite
                    db.confirmarReferenciaServidor(
                            referencia.getString("tipo"),
                            referencia.getLong("idLocal"),
                            respuesta.getLong("id"),
                            referencia.getString("uuid"),
                            referencia.getString("nombre")
                    );

                    referenciasEnviadas++;
                }

                // 4. Buscar los reportes pendientes
                List<JSONObject> pendientes =
                        db.reportesPendientesParaEnvio();

                // 5. Enviar los reportes al servidor
                for (JSONObject reporte : pendientes) {

                    ApiClient.enviarReporte(reporte);

                    boolean confirmado =
                            db.marcarSincronizado(
                                    reporte.getString("uuid")
                            );

                    if (!confirmado) {
                        throw new IllegalStateException(
                                "No se pudo confirmar el reporte"
                        );
                    }

                    reportesEnviados++;
                }

                // 6. Descargar reportes del servidor
                JSONArray reportesServidor =
                        ApiClient.descargarReportes();

                // 7. Guardar reportes nuevos sin duplicarlos
                reportesDescargados =
                        db.guardarReportesServidor(
                                reportesServidor
                        );

                mensaje = "Sincronización completada.\n"
                        + "Lugares/categorías enviados: "
                        + referenciasEnviadas
                        + "\nReportes enviados: "
                        + reportesEnviados
                        + "\nReportes recibidos nuevos: "
                        + reportesDescargados
                        + "\nPendientes: "
                        + db.pendientes();

                Log.i("CampusReport", mensaje);

            } catch (Exception e) {

                Log.e("CampusReport",
                        "Error durante la sincronización", e);

                mensaje = "Sincronización incompleta.\n"
                        + "Referencias confirmadas: "
                        + referenciasEnviadas
                        + "\nReportes confirmados: "
                        + reportesEnviados
                        + "\nPendientes: "
                        + db.pendientes()
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
