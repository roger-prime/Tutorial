package com.example.campusreport.ui;
import android.content.Intent;
import android.os.Bundle;
import com.example.campusreport.R;
import com.example.campusreport.data.Report;
public class ReportsActivity extends BaseActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);title(getString(R.string.reportes),"Consultá tus reportes guardados.");
        button(R.string.nuevo_reporte,v->{open(NewReportActivity.class);finish();});
        if(app.db.reportes().isEmpty()) card("Todavía no hay reportes. Creá el primero.");
        for(Report r:app.db.reportes()) {
            button(r.lugar+" · "+r.categoria+"\n"+r.autorVisible()+"\n"+getString(r.sincronizado?R.string.sincronizado:R.string.pendiente_sincronizacion),v->{
                Intent intent=new Intent(this,DetailActivity.class);intent.putExtra("uuid",r.uuid);startActivity(intent);
            });
        }backButton();
    }
}
