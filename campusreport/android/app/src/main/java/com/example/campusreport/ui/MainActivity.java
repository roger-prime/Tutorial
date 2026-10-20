package com.example.campusreport.ui;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.campusreport.R;
public class MainActivity extends BaseActivity {
    private TextView stats,status;private Button update;
    private final Runnable refresh=this::refresh;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        title(getString(R.string.inicio),"Registrá un problema de tu facultad.");
        stats=card("");
        button(R.string.nuevo_reporte,v->open(NewReportActivity.class));
        button(R.string.reportes,v->open(ReportsActivity.class));
        button(R.string.lugares,v->open(PlacesActivity.class));
        button(R.string.categorias,v->open(CategoriesActivity.class));
        update=button(R.string.actualizar,v->{
            Toast.makeText(this,"Actualizando lugares y categorías",Toast.LENGTH_SHORT).show();app.descargar();
        });
        status=text("",14,0xFF50637E);
    }
    @Override protected void onStart(){super.onStart();app.observar(refresh);}
    @Override protected void onStop(){app.dejarDeObservar(refresh);super.onStop();}
    private void refresh() {
        stats.setText(app.db.reportes().size()+" reportes guardados\n"+app.db.lugares().size()+" lugares · "+app.db.categorias().size()+" categorías");
        status.setText(app.resultado());update.setEnabled(!app.descargando());
        update.setText(R.string.actualizar);
    }
}
