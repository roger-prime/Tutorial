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
    public CampusDb(Context context, String databaseName) { super(context, databaseName, null, 2); }
    @Override public void onConfigure(SQLiteDatabase db) { db.setForeignKeyConstraintsEnabled(true); }
    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE lugar (id INTEGER PRIMARY KEY, nombre TEXT NOT NULL, uuid TEXT UNIQUE)");
        db.execSQL("CREATE TABLE categoria (id INTEGER PRIMARY KEY, nombre TEXT NOT NULL, uuid TEXT UNIQUE)");
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
            if(db.update(table,values,"id=?",new String[]{String.valueOf(id)})==0) {
                values.put("id",id); db.insertOrThrow(table,null,values);
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
}
