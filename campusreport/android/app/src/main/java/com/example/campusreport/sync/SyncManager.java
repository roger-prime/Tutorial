package com.example.campusreport.sync;
import com.example.campusreport.data.CampusDb;
import org.json.*;
import java.io.IOException;
public final class SyncManager {
    private final CampusDb db;private final SyncTransport api;
    public SyncManager(CampusDb db,SyncTransport api) {this.db=db;this.api=api;}
    public static final class Result {
        public int referencias,enviados,descargados,rechazados;public long pendientes;public String error="";
        public String mensaje() {
            return "Reportes enviados: "+enviados+". Reportes descargados: "+descargados+". Lugares y categorías enviados: "+referencias+". Pendientes: "+pendientes+"."
                +(rechazados>0?" Hay "+rechazados+" registros sin confirmar; se conservan para reintentar.":"")+(error.isEmpty()?"":" "+error);
        }
    }
    public Result sincronizar() {
        Result result=new Result();
        try {
            for(boolean lugar:new boolean[]{true,false}) for(JSONObject r:db.referenciasPendientes(lugar)) {
                try {db.confirmarReferencia(lugar,r,api.enviarReferencia(lugar,r));result.referencias++;}
                catch(SyncTransport.Rechazo|JSONException e) {result.rechazados++;}
            }
            for(String uuid:db.reportesPendientes()) {
                JSONObject r=db.reporteParaEnviar(uuid);if(r==null) continue;
                try {db.confirmarReporte(r,api.enviarReporte(r));result.enviados++;}
                catch(SyncTransport.Rechazo|JSONException e) {result.rechazados++;}
            }
            // Primero reportes: el catálogo posterior contiene sus referencias nuevas.
            JSONArray reports=api.reportes();JSONObject catalog=api.catalogo();
            result.descargados=db.guardarNovedades(catalog,reports);
        }catch(IOException e) {result.error="No se completó la conexión con el servidor. Tus datos siguen guardados.";}
         catch(Exception e) {result.error="No se pudieron incorporar todos los datos. Se conservan los pendientes.";}
        result.pendientes=db.pendientes()+db.referenciasPendientesCount();return result;
    }
}
