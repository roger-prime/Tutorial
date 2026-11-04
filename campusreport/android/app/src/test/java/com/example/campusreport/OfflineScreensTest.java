package com.example.campusreport;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.campusreport.ui.ReportsActivity;
import com.example.campusreport.ui.DetailActivity;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.SQLiteMode;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

/** Recorre las pantallas reales de listado y detalle sin una red activa. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=28,application=CampusApp.class)
@SQLiteMode(SQLiteMode.Mode.NATIVE)
public class OfflineScreensTest {
    private static TextView buscar(View root,String text) {
        if(root instanceof TextView && ((TextView)root).getText().toString().contains(text)) return (TextView)root;
        if(root instanceof ViewGroup) {
            ViewGroup group=(ViewGroup)root;
            for(int i=0;i<group.getChildCount();i++) {
                TextView result=buscar(group.getChildAt(i),text);if(result!=null) return result;
            }
        }
        return null;
    }

    @Test public void opensPersistedReportFromListAndReadsDetailWithoutNetwork() throws Exception {
        CampusApp app=(CampusApp)RuntimeEnvironment.getApplication();
        app.db.guardarCatalogo(new JSONObject("{\"lugares\":[{\"id\":1,\"nombre\":\"Biblioteca\"}],\"categorias\":[{\"id\":1,\"nombre\":\"Mobiliario\"}]}"));
        ConnectivityManager network=app.getSystemService(ConnectivityManager.class);
        shadowOf(network).setActiveNetworkInfo(null);
        assertNull(network.getActiveNetworkInfo());
        String id=app.db.crearReporte(1,1,"La silla de la biblioteca está rota");
        app.db.close();app.db.getReadableDatabase();
        var list=Robolectric.buildActivity(ReportsActivity.class).setup();
        try {
            TextView row=buscar(list.get().getWindow().getDecorView(),"Biblioteca · Mobiliario");
            assertNotNull(row);assertTrue(row.getText().toString().contains("Pendiente de sincronización"));
            assertTrue(row.performClick());
            Intent target=shadowOf(list.get()).getNextStartedActivity();
            assertEquals(DetailActivity.class.getName(),target.getComponent().getClassName());
            assertEquals(id,target.getStringExtra("uuid"));
            var detail=Robolectric.buildActivity(DetailActivity.class,target).setup();
            try {
                View root=detail.get().getWindow().getDecorView();
                assertNotNull(buscar(root,"La silla de la biblioteca está rota"));
                assertNotNull(buscar(root,"Autor: Anónimo"));
                assertNotNull(buscar(root,"Estado: Pendiente de sincronización"));
            }finally {detail.pause().stop().destroy();}
        }finally {list.pause().stop().destroy();}
    }
}
