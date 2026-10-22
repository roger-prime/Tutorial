package com.example.campusreport;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.campusreport.data.CampusDb;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.SQLiteMode;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28,application=Application.class)
@SQLiteMode(SQLiteMode.Mode.NATIVE)
public class CampusDbLocalTest {
    private Context context;private CampusDb db;
    private static final String NAME="campusreport-unit.db";
    @Before public void setup() {context=RuntimeEnvironment.getApplication();context.deleteDatabase(NAME);db=new CampusDb(context,NAME);}
    @After public void cleanup() {db.close();context.deleteDatabase(NAME);}

    @Test public void savesLocalReferencesWithoutNetworkAndPreservesThemAfterDownload() throws Exception {
        long place=db.crearLugar("Sala de estudio"),category=db.crearCategoria("Equipamiento");
        assertTrue(place<0);assertTrue(category<0);
        assertThrows(IllegalArgumentException.class,()->db.crearLugar(" sala DE estudio "));
        db.crearReporte(place,category,"El proyector no funciona");
        JSONObject catalog=new JSONObject("{\"lugares\":[{\"id\":1,\"nombre\":\"Aula 1\"}],\"categorias\":[{\"id\":1,\"nombre\":\"Electricidad\"}]}");
        db.guardarCatalogo(catalog);db.guardarCatalogo(catalog);
        db.close();db=new CampusDb(context,NAME);
        assertEquals(2,db.lugares().size());assertEquals(2,db.categorias().size());
        assertEquals("Sala de estudio",db.reportes().get(0).lugar);
    }
    @Test public void anonymousReportDoesNotStoreAProvidedName() {
        long place=db.crearLugar("Laboratorio"),category=db.crearCategoria("Electricidad");
        String id=db.crearReporte(place,category,"La luz del aula no enciende",true,"Nombre que no debe guardarse");
        assertTrue(db.reporte(id).anonimo);assertNull(db.reporte(id).autor);
        try(Cursor c=db.getReadableDatabase().rawQuery("SELECT autor FROM reporte WHERE uuid=?",new String[]{id})) {
            assertTrue(c.moveToFirst());assertTrue(c.isNull(0));
        }
    }
    @Test public void namedReportValidatesAndPreservesName() {
        long place=db.crearLugar("Biblioteca"),category=db.crearCategoria("Mobiliario");
        assertThrows(IllegalArgumentException.class,()->db.crearReporte(place,category,"Hay una silla dañada",false," "));
        String id=db.crearReporte(place,category,"Hay una silla dañada",false," Ana López ");
        db.close();db=new CampusDb(context,NAME);
        assertFalse(db.reporte(id).anonimo);assertEquals("Ana López",db.reporte(id).autorVisible());
    }
    @Test public void migrationKeepsExistingReportsAndMarksThemAnonymous() {
        db.close();context.deleteDatabase(NAME);
        try(SQLiteDatabase old=context.openOrCreateDatabase(NAME,Context.MODE_PRIVATE,null)) {
            old.execSQL("CREATE TABLE lugar(id INTEGER PRIMARY KEY,nombre TEXT NOT NULL)");
            old.execSQL("CREATE TABLE categoria(id INTEGER PRIMARY KEY,nombre TEXT NOT NULL)");
            old.execSQL("CREATE TABLE reporte(uuid TEXT PRIMARY KEY,lugar_id INTEGER NOT NULL REFERENCES lugar(id),categoria_id INTEGER NOT NULL REFERENCES categoria(id),descripcion TEXT NOT NULL,fecha_creacion TEXT NOT NULL,sincronizado INTEGER NOT NULL DEFAULT 0)");
            old.execSQL("INSERT INTO lugar VALUES(1,'Aula 1')");old.execSQL("INSERT INTO categoria VALUES(1,'Electricidad')");
            old.execSQL("INSERT INTO reporte VALUES('anterior',1,1,'La lámpara no funciona','2026-10-16T12:00:00Z',0)");old.setVersion(1);
        }
        db=new CampusDb(context,NAME);
        assertEquals("La lámpara no funciona",db.reporte("anterior").descripcion);
        assertTrue(db.reporte("anterior").anonimo);assertNull(db.reporte("anterior").autor);
        assertTrue(db.crearLugar("Patio")<0);assertEquals(2,db.getReadableDatabase().getVersion());
    }
    @Test public void downloadedCatalogAndTwoPendingReportsSurviveReopening() throws Exception {
        db.guardarCatalogo(new JSONObject("{\"lugares\":[{\"id\":1,\"nombre\":\"Aula 1\"},{\"id\":2,\"nombre\":\"Biblioteca\"}],\"categorias\":[{\"id\":1,\"nombre\":\"Electricidad\"},{\"id\":2,\"nombre\":\"Mobiliario\"}]}"));
        // Desde aquí solo se usa SQLite: no existe ninguna llamada a red.
        String first=db.crearReporte(1,1,"La luz del aula no enciende",true,null);
        String second=db.crearReporte(2,2,"Una silla de la biblioteca está rota",false,"Roger Báez");
        db.close();db=new CampusDb(context,NAME);
        assertEquals(2,db.lugares().size());assertEquals(2,db.categorias().size());
        assertEquals(2,db.reportes().size());assertEquals(2,db.pendientes());
        assertFalse(db.reporte(first).sincronizado);assertFalse(db.reporte(second).sincronizado);
        assertEquals("Anónimo",db.reporte(first).autorVisible());assertEquals("Roger Báez",db.reporte(second).autorVisible());
        assertEquals("Una silla de la biblioteca está rota",db.reporte(second).descripcion);
    }
    @Test public void badDownloadRollsBackAndInvalidFormDoesNotInsert() throws Exception {
        long place=db.crearLugar("Laboratorio"),category=db.crearCategoria("Electricidad");
        assertThrows(IllegalArgumentException.class,()->db.crearReporte(place,category,"corta"));
        assertThrows(IllegalArgumentException.class,()->db.crearReporte(place,category,new String(new char[501]).replace('\0','x')));
        assertThrows(android.database.sqlite.SQLiteConstraintException.class,()->db.crearReporte(999,category,"La lámpara no funciona"));
        assertEquals(0,db.reportes().size());
        assertThrows(org.json.JSONException.class,()->db.guardarCatalogo(new JSONObject("{\"lugares\":[{\"id\":1,\"nombre\":\"Patio\"}],\"categorias\":[{\"id\":0,\"nombre\":\"Inválido\"}]}")));
        assertEquals(1,db.lugares().size());assertEquals("Laboratorio",db.lugares().get(0).nombre);
    }
}
