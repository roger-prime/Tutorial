package com.example.campusreport;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import android.content.Context;
import com.example.campusreport.data.CampusDb;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
@RunWith(AndroidJUnit4.class)
public class CampusDbTest {
    private Context context;private CampusDb db;
    private JSONObject catalog() throws Exception {
        return new JSONObject("{\"lugares\":[{\"id\":1,\"nombre\":\"Aula 1\"}],\"categorias\":[{\"id\":1,\"nombre\":\"Electricidad\"}]}");
    }
    @Before public void setup() { context=InstrumentationRegistry.getInstrumentation().getTargetContext();context.deleteDatabase("campusreport-test.db");db=new CampusDb(context,"campusreport-test.db"); }
    @After public void cleanup() { db.close();context.deleteDatabase("campusreport-test.db"); }
    @Test public void persistsOfflineAndDoesNotDuplicateCatalog() throws Exception {
        db.guardarCatalogo(catalog());String id=db.crearReporte(1,1,"La luz del aula no funciona");
        db.guardarCatalogo(catalog());db.close();db=new CampusDb(context,"campusreport-test.db");
        assertEquals(1,db.lugares().size());assertEquals(1,db.reportes().size());assertEquals(1,db.pendientes());
        assertEquals("La luz del aula no funciona",db.reporte(id).descripcion);
        assertFalse(db.reporte(id).sincronizado);
    }
    @Test public void invalidCatalogRollsBackBothTables() throws Exception {
        db.guardarCatalogo(catalog());
        try {
            db.guardarCatalogo(new JSONObject("{\"lugares\":[{\"id\":2,\"nombre\":\"Patio\"}],\"categorias\":[{\"id\":0,\"nombre\":\"Inválida\"}]}"));
            fail("Debe rechazar el catálogo");
        } catch(org.json.JSONException expected) { }
        assertEquals(1,db.lugares().size());
    }
    @Test public void rejectsShortDescription() throws Exception {
        db.guardarCatalogo(catalog());
        try { db.crearReporte(1,1,"corta");fail("Debe validar"); } catch(IllegalArgumentException expected) { }
        assertEquals(0,db.reportes().size());
    }
}
