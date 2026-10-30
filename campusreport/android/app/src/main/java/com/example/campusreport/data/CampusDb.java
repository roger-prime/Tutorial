package com.example.campusreport.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Fuente local de las pantallas: registros propios y catálogos descargados. */
public class CampusDb extends SQLiteOpenHelper {
    public CampusDb(Context context) { this(context, "campusreport.db"); }
    public CampusDb(Context context, String databaseName) { super(context, databaseName, null, 3); }
    @Override public void onConfigure(SQLiteDatabase db) { db.setForeignKeyConstraintsEnabled(true); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE lugar (id INTEGER PRIMARY KEY, nombre TEXT NOT NULL, uuid TEXT UNIQUE, server_id INTEGER UNIQUE)");
        db.execSQL("CREATE TABLE categoria (id INTEGER PRIMARY KEY, nombre TEXT NOT NULL, uuid TEXT UNIQUE, server_id INTEGER UNIQUE)");
        db.execSQL("CREATE TABLE reporte (uuid TEXT PRIMARY KEY, lugar_id INTEGER NOT NULL REFERENCES lugar(id), categoria_id INTEGER NOT NULL REFERENCES categoria(id), descripcion TEXT NOT NULL CHECK(length(trim(descripcion)) BETWEEN 10 AND 500), fecha_creacion TEXT NOT NULL, sincronizado INTEGER NOT NULL DEFAULT 0 CHECK(sincronizado IN (0,1)), anonimo INTEGER NOT NULL DEFAULT 1 CHECK(anonimo IN (0,1)), autor TEXT)");
        db.execSQL("CREATE INDEX reporte_pendiente ON reporte(sincronizado)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion<2) {
            db.execSQL("ALTER TABLE lugar ADD COLUMN uuid TEXT");
            db.execSQL("ALTER TABLE categoria ADD COLUMN uuid TEXT");
            db.execSQL("CREATE UNIQUE INDEX lugar_uuid ON lugar(uuid)");
            db.execSQL("CREATE UNIQUE INDEX categoria_uuid ON categoria(uuid)");
            db.execSQL("ALTER TABLE reporte ADD COLUMN anonimo INTEGER NOT NULL DEFAULT 1 CHECK(anonimo IN (0,1))");
            db.execSQL("ALTER TABLE reporte ADD COLUMN autor TEXT");
        }
        if(oldVersion<3) {migrarReferencias(db,"lugar");migrarReferencias(db,"categoria");}
    }
    private void migrarReferencias(SQLiteDatabase db,String table) {
        db.execSQL("ALTER TABLE "+table+" ADD COLUMN server_id INTEGER");
        db.execSQL("CREATE UNIQUE INDEX "+table+"_server_id ON "+table+"(server_id)");
        db.execSQL("UPDATE "+table+" SET server_id=id WHERE id>0");
        try(Cursor c=db.rawQuery("SELECT id FROM "+table+" WHERE id<0 AND uuid IS NULL",null)) {
            while(c.moveToNext()) {ContentValues v=new ContentValues();v.put("uuid",UUID.randomUUID().toString());db.update(table,v,"id=?",new String[]{c.getString(0)});}
        }
    }
    public List<NamedItem> lugares() { return readCatalog("lugar"); }
    public List<NamedItem> categorias() { return readCatalog("categoria"); }
    private List<NamedItem> readCatalog(String table) {
        List<NamedItem> items=new ArrayList<>();
        try(Cursor c=getReadableDatabase().query(table,new String[]{"id","nombre","server_id"},null,null,null,null,"nombre ASC")) {
            while(c.moveToNext()) items.add(new NamedItem(c.getLong(0),c.getString(1),!c.isNull(2)));
        }
        return items;
    }

    public long crearLugar(String nombre) { return crearReferencia("lugar",nombre); }
    public long crearCategoria(String nombre) { return crearReferencia("categoria",nombre); }
    private long crearReferencia(String table,String nombre) {
        String clean=nombre.trim();
        if(clean.length()<3||clean.length()>80) throw new IllegalArgumentException("Escribí entre 3 y 80 caracteres");
        SQLiteDatabase db=getWritableDatabase();db.beginTransaction();
        try {
            for(NamedItem item:readCatalog(table))
                if(item.nombre.equalsIgnoreCase(clean)) throw new IllegalArgumentException("Ese nombre ya está guardado");
            // IDs negativos para registros propios: nunca chocan con los IDs positivos del servidor.
            long id;
            try(Cursor c=db.rawQuery("SELECT MIN(COALESCE(MIN(id),0),0)-1 FROM "+table,null)) {c.moveToFirst();id=c.getLong(0);}
            ContentValues values=new ContentValues();values.put("id",id);values.put("nombre",clean);
            values.put("uuid",UUID.randomUUID().toString());db.insertOrThrow(table,null,values);
            db.setTransactionSuccessful();return id;
        } finally { db.endTransaction(); }
    }

    public void guardarCatalogo(JSONObject json) throws JSONException {
        JSONArray lugares=json.getJSONArray("lugares"), categorias=json.getJSONArray("categorias");
        SQLiteDatabase db=getWritableDatabase();
        db.beginTransaction();
        try {
            upsert(db,"lugar",lugares); upsert(db,"categoria",categorias);
            db.setTransactionSuccessful();
        } finally { db.endTransaction(); }
    }
    private void upsert(SQLiteDatabase db,String table,JSONArray items) throws JSONException {
        for(int i=0;i<items.length();i++) {
            JSONObject obj=items.getJSONObject(i);
            long id=obj.getLong("id"); String nombre=obj.getString("nombre").trim();
            if(id<=0 || nombre.isEmpty()) throw new JSONException("Catálogo inválido");
            ContentValues values=new ContentValues(); values.put("nombre",nombre);
            // UPDATE + INSERT conserva las referencias; REPLACE borraría la fila anterior.
            if(db.update(table,values,"server_id=?",new String[]{String.valueOf(id)})==0) {
                values.put("id",id); values.put("server_id",id); db.insertOrThrow(table,null,values);
            }
        }
    }
    public String crearReporte(long lugar,long categoria,String descripcion) {
        return crearReporte(lugar,categoria,descripcion,true,null);
    }
    public String crearReporte(long lugar,long categoria,String descripcion,boolean anonimo,String autor) {
        String clean=descripcion.trim();
        if(clean.length()<10 || clean.length()>500) throw new IllegalArgumentException("Escribí entre 10 y 500 caracteres");
        String nombre=anonimo?null:(autor==null?"":autor.trim());
        if(!anonimo&&(nombre.length()<2||nombre.length()>80))
            throw new IllegalArgumentException("Escribí tu nombre entre 2 y 80 caracteres");
        String uuid=UUID.randomUUID().toString();
        ContentValues values=new ContentValues(); values.put("uuid",uuid);
        values.put("lugar_id",lugar);values.put("categoria_id",categoria);
        values.put("descripcion",clean);values.put("fecha_creacion",Instant.now().toString());
        values.put("sincronizado",0);values.put("anonimo",anonimo?1:0);
        if(anonimo) values.putNull("autor");else values.put("autor",nombre);
        getWritableDatabase().insertOrThrow("reporte",null,values);
        return uuid;
    }
    public List<Report> reportes() { return queryReports(null); }
    public Report reporte(String uuid) {
        List<Report> rows=queryReports(uuid); return rows.isEmpty()?null:rows.get(0);
    }
    private List<Report> queryReports(String uuid) {
        List<Report> rows=new ArrayList<>();
        String sql="SELECT r.uuid,l.nombre,c.nombre,r.descripcion,r.fecha_creacion,r.sincronizado,r.anonimo,r.autor FROM reporte r JOIN lugar l ON l.id=r.lugar_id JOIN categoria c ON c.id=r.categoria_id";
        if(uuid!=null) sql+=" WHERE r.uuid=?";
        sql+=" ORDER BY r.fecha_creacion DESC";
        try(Cursor c=getReadableDatabase().rawQuery(sql,uuid==null?null:new String[]{uuid})) {
            while(c.moveToNext()) rows.add(new Report(c.getString(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getInt(5)==1,c.getInt(6)==1,c.isNull(7)?null:c.getString(7)));
        }
        return rows;
    }
    public long pendientes() {
        try(Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM reporte WHERE sincronizado=0",null)) {
            c.moveToFirst();return c.getLong(0);
        }
    }

    public List<JSONObject> referenciasPendientes(boolean lugar) throws JSONException {
        List<JSONObject> rows=new ArrayList<>();
        try(Cursor c=getReadableDatabase().rawQuery("SELECT uuid,nombre FROM "+(lugar?"lugar":"categoria")+" WHERE server_id IS NULL ORDER BY id",null)) {
            while(c.moveToNext()) rows.add(new JSONObject().put("uuid",c.getString(0)).put("nombre",c.getString(1)));
        }return rows;
    }
    public long referenciasPendientesCount() {
        try(Cursor c=getReadableDatabase().rawQuery("SELECT (SELECT COUNT(*) FROM lugar WHERE server_id IS NULL)+(SELECT COUNT(*) FROM categoria WHERE server_id IS NULL)",null)) {c.moveToFirst();return c.getLong(0);}
    }
    public void confirmarReferencia(boolean lugar,JSONObject sent,JSONObject ack) throws JSONException {
        long serverId=ack.getLong("id");
        if(serverId<=0||!sent.getString("uuid").equals(ack.getString("uuid"))||!sent.getString("nombre").equals(ack.getString("nombre"))) throw new JSONException("Confirmación de referencia inválida");
        String table=lugar?"lugar":"categoria",fk=lugar?"lugar_id":"categoria_id";
        SQLiteDatabase db=getWritableDatabase();db.beginTransaction();
        try(Cursor c=db.rawQuery("SELECT id,nombre FROM "+table+" WHERE uuid=? AND server_id IS NULL",new String[]{sent.getString("uuid")})) {
            if(c.moveToFirst()) {
                long localId=c.getLong(0);
                if(!c.getString(1).equals(sent.getString("nombre"))) throw new JSONException("La referencia cambió");
                try(Cursor match=db.rawQuery("SELECT id FROM "+table+" WHERE server_id=?",new String[]{String.valueOf(serverId)})) {
                    ContentValues v=new ContentValues();
                    if(match.moveToFirst()) {
                        v.put(fk,match.getLong(0));db.update("reporte",v,fk+"=?",new String[]{String.valueOf(localId)});
                        db.delete(table,"id=?",new String[]{String.valueOf(localId)});
                    } else {v.put("server_id",serverId);db.update(table,v,"id=?",new String[]{String.valueOf(localId)});}
                }
            }
            db.setTransactionSuccessful();
        } finally {db.endTransaction();}
    }
    public List<String> reportesPendientes() {
        List<String> ids=new ArrayList<>();
        try(Cursor c=getReadableDatabase().rawQuery("SELECT uuid FROM reporte WHERE sincronizado=0 ORDER BY fecha_creacion,uuid",null)) {while(c.moveToNext()) ids.add(c.getString(0));}return ids;
    }
    public JSONObject reporteParaEnviar(String uuid) throws JSONException {
        String sql="SELECT r.uuid,l.server_id,c.server_id,r.descripcion,r.fecha_creacion,r.anonimo,r.autor FROM reporte r JOIN lugar l ON l.id=r.lugar_id JOIN categoria c ON c.id=r.categoria_id WHERE r.uuid=? AND r.sincronizado=0";
        try(Cursor c=getReadableDatabase().rawQuery(sql,new String[]{uuid})) {
            if(!c.moveToFirst()||c.isNull(1)||c.isNull(2)) return null;
            return new JSONObject().put("uuid",c.getString(0)).put("lugarId",c.getLong(1)).put("categoriaId",c.getLong(2))
                .put("descripcion",c.getString(3)).put("fechaCreacion",c.getString(4)).put("anonimo",c.getInt(5)==1).put("autor",c.isNull(6)?JSONObject.NULL:c.getString(6));
        }
    }
    public void confirmarReporte(JSONObject sent,JSONObject ack) throws JSONException {
        com.example.campusreport.sync.ReportJson.confirmar(sent,ack);
        SQLiteDatabase db=getWritableDatabase();db.beginTransaction();
        try {
            JSONObject current=reporteParaEnviar(sent.getString("uuid"));
            if(current!=null) {
                com.example.campusreport.sync.ReportJson.confirmar(current,ack);
                ContentValues v=new ContentValues();v.put("sincronizado",1);db.update("reporte",v,"uuid=?",new String[]{sent.getString("uuid")});
            }
            db.setTransactionSuccessful();
        }finally {db.endTransaction();}
    }
    private long localId(SQLiteDatabase db,String table,long serverId) throws JSONException {
        try(Cursor c=db.rawQuery("SELECT id FROM "+table+" WHERE server_id=?",new String[]{String.valueOf(serverId)})) {
            if(!c.moveToFirst()) throw new JSONException("Falta una referencia del reporte");return c.getLong(0);
        }
    }
    public int guardarNovedades(JSONObject catalog,JSONArray reports) throws JSONException {
        SQLiteDatabase db=getWritableDatabase();db.beginTransaction();int added=0;
        try {
            upsert(db,"lugar",catalog.getJSONArray("lugares"));upsert(db,"categoria",catalog.getJSONArray("categorias"));
            for(int i=0;i<reports.length();i++) {
                JSONObject r=reports.getJSONObject(i);com.example.campusreport.sync.ReportJson.validar(r);
                String uuid=r.getString("uuid");
                // Un GET nunca confirma ni sustituye un reporte pendiente; solo el POST confirmado.
                if(reporte(uuid)!=null) continue;
                ContentValues v=new ContentValues();v.put("uuid",uuid);
                v.put("lugar_id",localId(db,"lugar",r.getLong("lugarId")));v.put("categoria_id",localId(db,"categoria",r.getLong("categoriaId")));
                v.put("descripcion",r.getString("descripcion"));v.put("fecha_creacion",r.getString("fechaCreacion"));
                v.put("anonimo",r.getBoolean("anonimo")?1:0);v.put("sincronizado",1);
                if(r.getBoolean("anonimo")) v.putNull("autor");else v.put("autor",r.getString("autor"));
                db.insertOrThrow("reporte",null,v);added++;
            }
            db.setTransactionSuccessful();return added;
        }finally {db.endTransaction();}
    }
}
