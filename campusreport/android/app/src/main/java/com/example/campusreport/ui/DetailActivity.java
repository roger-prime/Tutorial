package com.example.campusreport.ui;
import android.os.Bundle;
import com.example.campusreport.R;
import com.example.campusreport.data.Report;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
public class DetailActivity extends BaseActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);title(getString(R.string.detalle_reporte),"La información de tu reporte.");
        String uuid=getIntent().getStringExtra("uuid");Report report=uuid==null?null:app.db.reporte(uuid);
        if(report==null) card("No se encontró este reporte.");
        else {
            card("Lugar: "+report.lugar+"\nCategoría: "+report.categoria);
            card(report.descripcion);card("Autor: "+report.autorVisible());
            String fecha=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault()).format(Instant.parse(report.fecha));
            text("Creado: "+fecha,14,0xFF50637E);
        }backButton();
    }
}
