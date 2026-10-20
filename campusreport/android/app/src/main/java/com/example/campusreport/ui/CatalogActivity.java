package com.example.campusreport.ui;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.campusreport.R;
import com.example.campusreport.data.NamedItem;
import java.util.List;

/** Lugares y Categorías comparten la misma presentación y la acción Guardar. */
public abstract class CatalogActivity extends BaseActivity {
    protected abstract boolean esLugar();
    private EditText nombre;private LinearLayout lista;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        title(getString(esLugar()?R.string.lugares:R.string.categorias),
            esLugar()?"Agregá los lugares de tu facultad.":"Agregá los tipos de problemas que querés reportar.");
        TextView label=text("Nombre",16,0xFF10213E);
        nombre=new EditText(this);nombre.setId(android.view.View.generateViewId());label.setLabelFor(nombre.getId());
        nombre.setHint(esLugar()?"Ejemplo: Sala de estudio":"Ejemplo: Equipamiento");
        nombre.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        nombre.setSingleLine(true);nombre.setFilters(new InputFilter[]{new InputFilter.LengthFilter(80)});
        content.addView(nombre,new LinearLayout.LayoutParams(-1,-2));
        if(state!=null) nombre.setText(state.getString("nombre",""));
        button(R.string.guardar,v->{
            try {
                if(esLugar()) app.db.crearLugar(nombre.getText().toString());
                else app.db.crearCategoria(nombre.getText().toString());
                nombre.setText("");nombre.setError(null);refresh();
                String message=esLugar()?"Lugar guardado":"Categoría guardada";
                Log.i("CampusReport",message);Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
            } catch(IllegalArgumentException e) { nombre.setError(e.getMessage()); }
              catch(Exception e) { Log.e("CampusReport","No se pudo guardar",e);Toast.makeText(this,"No se pudo guardar. Intentá de nuevo.",Toast.LENGTH_LONG).show(); }
        });
        lista=new LinearLayout(this);lista.setOrientation(LinearLayout.VERTICAL);
        content.addView(lista,new LinearLayout.LayoutParams(-1,-2));refresh();backButton();
    }
    private void refresh() {
        lista.removeAllViews();List<NamedItem> items=esLugar()?app.db.lugares():app.db.categorias();
        if(items.isEmpty()) row(esLugar()?"Todavía no hay lugares. Agregá el primero.":"Todavía no hay categorías. Agregá la primera.");
        for(NamedItem item:items) row(item.nombre);
    }
    private void row(String value) {
        TextView tv=new TextView(this);tv.setText(value);tv.setTextSize(17);tv.setTextColor(0xFF10213E);
        tv.setPadding(dp(20),dp(20),dp(20),dp(20));tv.setBackgroundResource(R.drawable.card);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(12);lista.addView(tv,lp);
    }
    @Override protected void onSaveInstanceState(Bundle state) {super.onSaveInstanceState(state);state.putString("nombre",nombre.getText().toString());}
}
