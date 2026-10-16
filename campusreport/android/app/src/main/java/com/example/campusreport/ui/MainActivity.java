package com.example.campusreport.ui;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BaseActivity {
    private TextView stats,status;private Button download;
    private final Runnable refresh=this::refresh;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        title("Un campus mejor,\nun reporte a la vez.","Registrá un problema, incluso sin conexión.");
        stats=card("");
        button("Nuevo reporte",v->open(NewReportActivity.class));
        button("Mis reportes",v->open(ReportsActivity.class));
        button("Lugares disponibles",v->open(PlacesActivity.class));
        download=button("Descargar lugares y categorías",v->{
            Toast.makeText(this,"Iniciando descarga del servidor",Toast.LENGTH_SHORT).show();app.descargar();
        });
        status=text("",14,0xFF50637E);
        text("Semana 1 · Los reportes se guardan en este dispositivo. El envío al servidor se incorpora en la semana 3.",13,0xFF50637E);
    }
    @Override protected void onStart(){super.onStart();app.observar(refresh);}
    @Override protected void onStop(){app.dejarDeObservar(refresh);super.onStop();}
    private void refresh() {
        stats.setText(app.db.pendientes()+" reportes pendientes de envío\n"+app.db.lugares().size()+" lugares guardados");
        status.setText(app.resultado());download.setEnabled(!app.descargando());
        download.setText(app.descargando()?"Descargando…":"Descargar lugares y categorías");
    }
}
