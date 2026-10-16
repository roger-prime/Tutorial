package com.example.campusreport.ui;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.example.campusreport.data.NamedItem;
import java.util.List;

public class NewReportActivity extends BaseActivity {
    private Spinner lugar,categoria;private EditText description;
    private List<NamedItem> lugares,categorias;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);title("Nuevo reporte","Contanos qué pasó. Se guarda en este dispositivo.");
        lugares=app.db.lugares();categorias=app.db.categorias();
        if(lugares.isEmpty()||categorias.isEmpty()) {
            card("Primero descargá los lugares y categorías desde Inicio. Después podrás crear reportes sin internet.");
            backButton();return;
        }
        text("Lugar",16,0xFF10213E);lugar=spinner(lugares);
        text("Categoría",16,0xFF10213E);categoria=spinner(categorias);
        text("Descripción",16,0xFF10213E);
        description=new EditText(this);description.setHint("Ejemplo: la luz junto a la puerta no enciende.");
        description.setTextSize(16);description.setMinLines(4);description.setGravity(android.view.Gravity.TOP);
        description.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        description.setFilters(new InputFilter[]{new InputFilter.LengthFilter(500)});
        content.addView(description,new android.widget.LinearLayout.LayoutParams(-1,-2));
        text("Entre 10 y 500 caracteres. No incluyas datos personales.",13,0xFF50637E);
        if(state!=null) {
            description.setText(state.getString("description",""));
            select(lugar,lugares,state.getLong("place",-1));select(categoria,categorias,state.getLong("category",-1));
        }
        button("Guardar reporte",v->{
            try {
                String uuid=app.db.crearReporte(((NamedItem)lugar.getSelectedItem()).id,
                    ((NamedItem)categoria.getSelectedItem()).id,description.getText().toString());
                Log.i("CampusReport","Botón Guardar: reporte local creado, UUID="+uuid);
                Toast.makeText(this,"Reporte guardado. Pendiente de envío.",Toast.LENGTH_LONG).show();
                Intent intent=new Intent(this,DetailActivity.class);intent.putExtra("uuid",uuid);startActivity(intent);finish();
            } catch(IllegalArgumentException e) { description.setError(e.getMessage()); }
              catch(Exception e) { Log.e("CampusReport","No se pudo guardar",e);Toast.makeText(this,"No se pudo guardar el reporte",Toast.LENGTH_LONG).show(); }
        });
        backButton();
    }
    private Spinner spinner(List<NamedItem> items) {
        Spinner s=new Spinner(this);ArrayAdapter<NamedItem> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);s.setAdapter(adapter);
        android.widget.LinearLayout.LayoutParams lp=new android.widget.LinearLayout.LayoutParams(-1,dp(52));lp.bottomMargin=dp(18);
        content.addView(s,lp);return s;
    }
    private void select(Spinner spinner,List<NamedItem> items,long id) {
        for(int i=0;i<items.size();i++) if(items.get(i).id==id) spinner.setSelection(i);
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if(description!=null) {
            state.putString("description",description.getText().toString());
            state.putLong("place",((NamedItem)lugar.getSelectedItem()).id);
            state.putLong("category",((NamedItem)categoria.getSelectedItem()).id);
        }
    }
}
