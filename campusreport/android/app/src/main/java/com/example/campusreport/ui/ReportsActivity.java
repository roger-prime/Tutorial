package com.example.campusreport.ui;
import android.content.Intent;
import android.os.Bundle;
import com.example.campusreport.data.Report;
public class ReportsActivity extends BaseActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);title("Mis reportes","Registros guardados en este dispositivo.");
        if(app.db.reportes().isEmpty()) card("Todavía no hay reportes. Creá el primero desde Inicio.");
        for(Report r:app.db.reportes()) {
            button(r.lugar+" · "+r.categoria+"\n"+(r.sincronizado?"Sincronizado":"Pendiente de envío"),v->{
                Intent intent=new Intent(this,DetailActivity.class);intent.putExtra("uuid",r.uuid);startActivity(intent);
            });
        }
        backButton();
    }
}
