package com.example.campusreport.sync;
import org.json.*;
import java.io.IOException;
public interface SyncTransport {
    JSONObject enviarReferencia(boolean lugar,JSONObject r) throws IOException,JSONException;
    JSONObject enviarReporte(JSONObject r) throws IOException,JSONException;
    JSONObject catalogo() throws IOException,JSONException;
    JSONArray reportes() throws IOException,JSONException;
    class Rechazo extends IOException {
        public final int status;
        public Rechazo(int status) {super("El servidor rechazó el registro (HTTP "+status+").");this.status=status;}
    }
}
