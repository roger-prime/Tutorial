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
    public boolean marcarSincronizado(String uuid) {
        ContentValues values = new ContentValues();
        values.put("sincronizado", 1);

        return getWritableDatabase().update(
                "reporte",
                values,
                "uuid=?",
                new String[]{uuid}
        ) == 1;
    }
    public List<JSONObject> reportesPendientesParaEnvio()
            throws JSONException {

        List<JSONObject> pendientes = new ArrayList<>();

        String sql = "SELECT uuid, lugar_id, categoria_id, " +
                "descripcion, fecha_creacion, anonimo, autor " +
                "FROM reporte WHERE sincronizado=0 " +
                "AND lugar_id>0 AND categoria_id>0 " +
                "ORDER BY fecha_creacion ASC";

        try (Cursor c = getReadableDatabase().rawQuery(sql, null)) {

            while (c.moveToNext()) {
                JSONObject reporte = new JSONObject();

                reporte.put("uuid", c.getString(0));
                reporte.put("lugarId", c.getLong(1));
                reporte.put("categoriaId", c.getLong(2));
                reporte.put("descripcion", c.getString(3));
                reporte.put("fechaCreacion", c.getString(4));
                reporte.put("anonimo", c.getInt(5) == 1);
                reporte.put("autor", c.isNull(6)
                        ? JSONObject.NULL : c.getString(6));

                pendientes.add(reporte);
            }
        }

        return pendientes;
    }

    public int guardarReportesServidor(JSONArray reportes)
            throws JSONException {

        SQLiteDatabase db = getWritableDatabase();
        int nuevos = 0;

        db.beginTransaction();

        try {
            for (int i = 0; i < reportes.length(); i++) {

                JSONObject reporte = reportes.getJSONObject(i);
                String uuid = reporte.getString("uuid");

                boolean existe;

                try (Cursor c = db.rawQuery(
                        "SELECT uuid FROM reporte WHERE uuid=?",
                        new String[]{uuid})) {

                    existe = c.moveToFirst();
                }

                if (existe) {
                    ContentValues estado = new ContentValues();
                    estado.put("sincronizado", 1);

                    db.update(
                            "reporte",
                            estado,
                            "uuid=?",
                            new String[]{uuid}
                    );

                    continue;
                }

                ContentValues values = new ContentValues();

                values.put("uuid", uuid);
                values.put("lugar_id", reporte.getLong("lugarId"));
                values.put("categoria_id", reporte.getLong("categoriaId"));
                values.put("descripcion",
                        reporte.getString("descripcion"));
                values.put("fecha_creacion",
                        reporte.getString("fechaCreacion"));
                values.put("anonimo",
                        reporte.getBoolean("anonimo") ? 1 : 0);

                if (reporte.isNull("autor")) {
                    values.putNull("autor");
                } else {
                    values.put("autor", reporte.getString("autor"));
                }

                values.put("sincronizado", 1);

                db.insertOrThrow("reporte", null, values);
                nuevos++;
            }

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }

        return nuevos;
    }

    public List<JSONObject> referenciasLocalesPendientes()
            throws JSONException {

        List<JSONObject> pendientes = new ArrayList<>();

        for (String tipo : new String[]{"lugar", "categoria"}) {

            try (Cursor c = getReadableDatabase().query(
                    tipo,
                    new String[]{"id", "uuid", "nombre"},
                    "id < 0 AND uuid IS NOT NULL",
                    null, null, null, "id ASC")) {

                while (c.moveToNext()) {

                    JSONObject referencia = new JSONObject();

                    referencia.put("tipo", tipo);
                    referencia.put("idLocal", c.getLong(0));
                    referencia.put("uuid", c.getString(1));
                    referencia.put("nombre", c.getString(2));

                    pendientes.add(referencia);
                }
            }
        }

        return pendientes;
    }

    public void confirmarReferenciaServidor(
            String tipo,
            long idLocal,
            long idServidor,
            String uuid,
            String nombre) {

        if ((!tipo.equals("lugar") && !tipo.equals("categoria"))
                || idLocal >= 0 || idServidor <= 0
                || uuid == null || nombre == null) {

            throw new IllegalArgumentException(
                    "Referencia invalida"
            );
        }

        SQLiteDatabase sql = getWritableDatabase();
        sql.beginTransaction();

        try {

            // Verificar que la referencia local sea la correcta
            try (Cursor c = sql.query(
                    tipo,
                    new String[]{"uuid"},
                    "id=?",
                    new String[]{String.valueOf(idLocal)},
                    null, null, null)) {

                if (!c.moveToFirst()
                        || !uuid.equals(c.getString(0))) {

                    throw new IllegalStateException(
                            "No coincide la referencia local"
                    );
                }
            }

            // Comprobar si el ID del servidor ya está en SQLite
            boolean existe;
            String uuidExistente = null;

            try (Cursor c = sql.query(
                    tipo,
                    new String[]{"uuid"},
                    "id=?",
                    new String[]{String.valueOf(idServidor)},
                    null, null, null)) {

                existe = c.moveToFirst();

                if (existe) {
                    uuidExistente = c.getString(0);
                }
            }

            if (existe && uuidExistente != null
                    && !uuidExistente.equals(uuid)) {

                throw new IllegalStateException(
                        "El ID del servidor pertenece a otra referencia"
                );
            }

            // Crear el registro con ID positivo si todavía no existe
            if (!existe) {

                ContentValues nuevo = new ContentValues();
                nuevo.put("id", idServidor);
                nuevo.put("nombre", nombre);

                sql.insertOrThrow(tipo, null, nuevo);
            }

            // Actualizar los reportes para que usen el ID positivo
            String columna = tipo.equals("lugar")
                    ? "lugar_id" : "categoria_id";

            ContentValues cambio = new ContentValues();
            cambio.put(columna, idServidor);

            sql.update(
                    "reporte",
                    cambio,
                    columna + "=?",
                    new String[]{String.valueOf(idLocal)}
            );

            // Eliminar la referencia temporal negativa
            int eliminados = sql.delete(
                    tipo,
                    "id=?",
                    new String[]{String.valueOf(idLocal)}
            );

            if (eliminados != 1) {
                throw new IllegalStateException(
                        "No se pudo actualizar la referencia local"
                );
            }

            // Conservar el UUID original en el registro definitivo
            ContentValues definitivo = new ContentValues();
            definitivo.put("uuid", uuid);

            if (sql.update(
                    tipo,
                    definitivo,
                    "id=?",
                    new String[]{String.valueOf(idServidor)}
            ) != 1) {

                throw new IllegalStateException(
                        "No se pudo confirmar el ID del servidor"
                );
            }

            sql.setTransactionSuccessful();

        } finally {
            sql.endTransaction();
        }
    }




}
