package com.example.campusreport.network;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;


public final class ApiClient {
    // 10.0.2.2 es la notebook vista desde el emulador Android de Android Studio.
    public static final String BASE_URL="http://10.0.2.2:8080";
    private ApiClient() {}
    public static JSONObject descargarCatalogo() throws Exception {
        HttpURLConnection con=(HttpURLConnection)new URL(BASE_URL+"/api/catalogo").openConnection();
        con.setConnectTimeout(8000);con.setReadTimeout(8000);
        con.setRequestProperty("Accept","application/json");
        try {
            int code=con.getResponseCode();
            if(code!=200) throw new IOException("El servidor respondió HTTP "+code);
            try(InputStream in=con.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()) {
                byte[] buffer=new byte[4096];int read;
                while((read=in.read(buffer))!=-1) {
                    if(out.size()+read>1048576) throw new IOException("Catálogo demasiado grande");
                    out.write(buffer,0,read);
                }
                return new JSONObject(out.toString(StandardCharsets.UTF_8.name()));
            }
        } finally { con.disconnect(); }
    }
    public static void enviarReporte(JSONObject reporte) throws Exception {

        HttpURLConnection con = (HttpURLConnection)
                new URL(BASE_URL + "/api/reportes").openConnection();

        try {
            con.setRequestMethod("POST");
            con.setConnectTimeout(8000);
            con.setReadTimeout(8000);
            con.setDoOutput(true);

            con.setRequestProperty(
                    "Content-Type", "application/json; charset=utf-8"
            );
            con.setRequestProperty("Accept", "application/json");

            byte[] datos = reporte.toString()
                    .getBytes(StandardCharsets.UTF_8);

            try (OutputStream salida = con.getOutputStream()) {
                salida.write(datos);
            }

            int codigo = con.getResponseCode();

            if (codigo != 200 && codigo != 201) {
                throw new IOException(
                        "El servidor rechazó el reporte. HTTP " + codigo
                );
            }

            try (InputStream entrada = con.getInputStream();
                 ByteArrayOutputStream respuesta = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[4096];
                int cantidad;

                while ((cantidad = entrada.read(buffer)) != -1) {
                    respuesta.write(buffer, 0, cantidad);
                }

                JSONObject confirmado = new JSONObject(
                        respuesta.toString(StandardCharsets.UTF_8.name())
                );

                if (!reporte.getString("uuid").equals(
                        confirmado.optString("uuid"))) {
                    throw new IOException(
                            "El servidor no confirmó el UUID del reporte"
                    );
                }
            }

        } finally {
            con.disconnect();
        }
    }

    public static JSONArray descargarReportes() throws Exception {

        HttpURLConnection con = (HttpURLConnection)
                new URL(BASE_URL + "/api/reportes").openConnection();

        try {
            con.setRequestMethod("GET");
            con.setConnectTimeout(8000);
            con.setReadTimeout(8000);
            con.setRequestProperty("Accept", "application/json");

            int codigo = con.getResponseCode();

            if (codigo != 200) {
                throw new IOException(
                        "Error al descargar reportes. HTTP " + codigo
                );
            }

            try (InputStream entrada = con.getInputStream();
                 ByteArrayOutputStream respuesta = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[4096];
                int cantidad;

                while ((cantidad = entrada.read(buffer)) != -1) {
                    if (respuesta.size() + cantidad > 1048576) {
                        throw new IOException("Respuesta demasiado grande");
                    }

                    respuesta.write(buffer, 0, cantidad);
                }

                return new JSONArray(
                        respuesta.toString(StandardCharsets.UTF_8.name())
                );
            }

        } finally {
            con.disconnect();
        }
    }

}
