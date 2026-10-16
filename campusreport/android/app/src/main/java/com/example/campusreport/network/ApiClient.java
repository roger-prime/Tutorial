package com.example.campusreport.network;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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
}
