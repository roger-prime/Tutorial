package com.example.campusreport.sync;
import org.json.*;
import java.time.Instant;
import java.util.UUID;
public final class ReportJson {
    public static void validar(JSONObject r) throws JSONException {
        try {
            String uuid=r.getString("uuid");
            if(!UUID.fromString(uuid).toString().equals(uuid)) throw new IllegalArgumentException();
            Instant.parse(r.getString("fechaCreacion"));
            String d=r.getString("descripcion");
            if(d.length()<10||d.length()>500||r.getLong("lugarId")<=0||r.getLong("categoriaId")<=0) throw new IllegalArgumentException();
            if(r.getBoolean("anonimo")) {if(!r.has("autor")||!r.isNull("autor")) throw new IllegalArgumentException();}
            else {String n=r.getString("autor");if(n.length()<2||n.length()>80) throw new IllegalArgumentException();}
        }catch(RuntimeException e) {throw new JSONException("Reporte recibido inválido");}
    }
    public static void confirmar(JSONObject sent,JSONObject ack) throws JSONException {
        validar(ack);
        for(String key:new String[]{"uuid","lugarId","categoriaId","descripcion","anonimo","autor"})
            if(!sent.get(key).toString().equals(ack.get(key).toString())) throw new JSONException("El servidor confirmó otro contenido");
        if(!Instant.parse(sent.getString("fechaCreacion")).equals(Instant.parse(ack.getString("fechaCreacion")))) throw new JSONException("Fecha de confirmación diferente");
    }
}
