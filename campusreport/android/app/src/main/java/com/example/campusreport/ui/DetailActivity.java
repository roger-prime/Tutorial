package com.example.campusreport.ui;
import android.os.Bundle;
import com.example.campusreport.data.Report;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
public class DetailActivity extends BaseActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);title("Detalle del reporte","Tu registro queda disponible sin conexión.");
        String uuid=getIntent().getStringExtra("uuid");Report report=uuid==null?null:app.db.reporte(uuid);
        if(report==null) card("No se encontró este reporte.");
        else {
            card(report.lugar+"\n"+report.categoria);
            card(report.descripcion);
            card(report.sincronizado?"Sincronizado":"Pendiente de envío");
            String fecha=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault()).format(Instant.parse(report.fecha));
            text("Creado: "+fecha,14,0xFF50637E);
            text("Identificador único\n"+report.uuid,12,0xFF50637E);
        }
        backButton();
    }
}
