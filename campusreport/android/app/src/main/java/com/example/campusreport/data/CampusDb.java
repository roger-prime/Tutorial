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

/** Fuente local de las pantallas. El catálogo se inserta únicamente tras descargarlo. */
public class CampusDb extends SQLiteOpenHelper {
    public CampusDb(Context context) { this(context, "campusreport.db"); }
    public CampusDb(Context context, String databaseName) { super(context, databaseName, null, 1); }
    @Override public void onConfigure(SQLiteDatabase db) { db.setForeignKeyConstraintsEnabled(true); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE lugar (id INTEGER PRIMARY KEY, nombre TEXT NOT NULL)");
        db.execSQL("CREATE TABLE categoria (id INTEGER PRIMARY KEY, nombre TEXT NOT NULL)");
        db.execSQL("CREATE TABLE reporte (uuid TEXT PRIMARY KEY, lugar_id INTEGER NOT NULL REFERENCES lugar(id), categoria_id INTEGER NOT NULL REFERENCES categoria(id), descripcion TEXT NOT NULL CHECK(length(trim(descripcion)) BETWEEN 10 AND 500), fecha_creacion TEXT NOT NULL, sincronizado INTEGER NOT NULL DEFAULT 0 CHECK(sincronizado IN (0,1)))");
        db.execSQL("CREATE INDEX reporte_pendiente ON reporte(sincronizado)");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new IllegalStateException("Definir una migración antes de cambiar la versión de la base");
    }
    public List<NamedItem> lugares() { return readCatalog("lugar"); }
    public List<NamedItem> categorias() { return readCatalog("categoria"); }
    private List<NamedItem> readCatalog(String table) {
        List<NamedItem> items=new ArrayList<>();
        try(Cursor c=getReadableDatabase().query(table,new String[]{"id","nombre"},null,null,null,null,"nombre ASC")) {
            while(c.moveToNext()) items.add(new NamedItem(c.getLong(0),c.getString(1)));
        }
        return items;
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
            if(db.update(table,values,"id=?",new String[]{String.valueOf(id)})==0) {
                values.put("id",id); db.insertOrThrow(table,null,values);
            }
        }
    }
    public String crearReporte(long lugar,long categoria,String descripcion) {
        String clean=descripcion.trim();
        if(clean.length()<10 || clean.length()>500) throw new IllegalArgumentException("Escribí entre 10 y 500 caracteres");
        String uuid=UUID.randomUUID().toString();
        ContentValues values=new ContentValues(); values.put("uuid",uuid);
        values.put("lugar_id",lugar);values.put("categoria_id",categoria);
        values.put("descripcion",clean);values.put("fecha_creacion",Instant.now().toString());
        values.put("sincronizado",0);
        getWritableDatabase().insertOrThrow("reporte",null,values);
        return uuid;
    }
    public List<Report> reportes() { return queryReports(null); }
    public Report reporte(String uuid) {
        List<Report> rows=queryReports(uuid); return rows.isEmpty()?null:rows.get(0);
    }
    private List<Report> queryReports(String uuid) {
        List<Report> rows=new ArrayList<>();
        String sql="SELECT r.uuid,l.nombre,c.nombre,r.descripcion,r.fecha_creacion,r.sincronizado FROM reporte r JOIN lugar l ON l.id=r.lugar_id JOIN categoria c ON c.id=r.categoria_id";
        if(uuid!=null) sql+=" WHERE r.uuid=?";
        sql+=" ORDER BY r.fecha_creacion DESC";
        try(Cursor c=getReadableDatabase().rawQuery(sql,uuid==null?null:new String[]{uuid})) {
            while(c.moveToNext()) rows.add(new Report(c.getString(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getInt(5)==1));
        }
        return rows;
    }
    public long pendientes() {
        try(Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM reporte WHERE sincronizado=0",null)) {
            c.moveToFirst();return c.getLong(0);
        }
    }
}
