package com.example.campusreport.network;
import com.example.campusreport.sync.SyncTransport;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
public final class ApiClient implements SyncTransport {
    public static final String BASE_URL="http://10.0.2.2:8080";
    private final String base;
    public ApiClient() {this(BASE_URL);}
    public ApiClient(String base) {this.base=base;}
    public JSONObject catalogo() throws IOException,JSONException {return new JSONObject(request("/api/catalogo",null));}
    public JSONArray reportes() throws IOException,JSONException {return new JSONArray(request("/api/reportes",null));}
    public JSONObject enviarReferencia(boolean lugar,JSONObject r) throws IOException,JSONException {return new JSONObject(request(lugar?"/api/lugares":"/api/categorias",r));}
    public JSONObject enviarReporte(JSONObject r) throws IOException,JSONException {return new JSONObject(request("/api/reportes",r));}
    private String request(String path,JSONObject body) throws IOException {
        HttpURLConnection c=(HttpURLConnection)new URL(base+path).openConnection();
        c.setConnectTimeout(8000);c.setReadTimeout(8000);c.setInstanceFollowRedirects(false);
        c.setRequestProperty("Accept","application/json");
        try {
            if(body!=null) {
                c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json; charset=UTF-8");
                byte[] bytes=body.toString().getBytes(StandardCharsets.UTF_8);c.setFixedLengthStreamingMode(bytes.length);
                try(OutputStream out=c.getOutputStream()) {out.write(bytes);}
            }
            int status=c.getResponseCode();if(status!=200&&status!=201) throw new SyncTransport.Rechazo(status);
            try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()) {
                byte[] buffer=new byte[4096];int n;
                while((n=in.read(buffer))!=-1) {if(out.size()+n>4194304) throw new IOException("Respuesta demasiado grande");out.write(buffer,0,n);}
                return out.toString(StandardCharsets.UTF_8.name());
            }
        }finally {c.disconnect();}
    }
}
