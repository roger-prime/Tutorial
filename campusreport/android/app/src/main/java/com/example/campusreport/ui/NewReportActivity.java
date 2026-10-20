package com.example.campusreport.ui;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.campusreport.R;
import com.example.campusreport.data.NamedItem;
import java.util.List;

public class NewReportActivity extends BaseActivity {
    private Spinner lugar,categoria;private EditText description,autor;private CheckBox anonimo;
    private TextView autorLabel;
    private List<NamedItem> lugares,categorias;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);title(getString(R.string.nuevo_reporte),"Contanos qué pasó.");
        lugares=app.db.lugares();categorias=app.db.categorias();
        if(lugares.isEmpty()||categorias.isEmpty()) {
            card("Agregá al menos un lugar y una categoría para crear tu reporte.");
            button(R.string.lugares,v->{open(PlacesActivity.class);finish();});
            button(R.string.categorias,v->{open(CategoriesActivity.class);finish();});backButton();return;
        }
        text("Lugar",16,0xFF10213E);lugar=spinner(lugares);lugar.setContentDescription("Lugar");
        text("Categoría",16,0xFF10213E);categoria=spinner(categorias);categoria.setContentDescription("Categoría");
        text("Descripción",16,0xFF10213E);
        description=field("Ejemplo: la luz junto a la puerta no enciende.",500);
        description.setMinLines(3);description.setGravity(android.view.Gravity.TOP);
        description.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        description.setContentDescription("Descripción");
        text("Entre 10 y 500 caracteres.",13,0xFF50637E);
        anonimo=new CheckBox(this);anonimo.setText(R.string.reporte_anonimo);anonimo.setTextSize(16);anonimo.setChecked(true);
        content.addView(anonimo,new LinearLayout.LayoutParams(-1,-2));
        autorLabel=text(getString(R.string.tu_nombre),16,0xFF10213E);autor=field("Escribí tu nombre",80);
        autor.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PERSON_NAME|InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        autor.setSingleLine(true);autor.setContentDescription(getString(R.string.tu_nombre));
        if(state!=null) {
            description.setText(state.getString("description",""));
            select(lugar,lugares,state.getLong("place",Long.MAX_VALUE));select(categoria,categorias,state.getLong("category",Long.MAX_VALUE));
            anonimo.setChecked(state.getBoolean("anonimo",true));
            if(!anonimo.isChecked()) autor.setText(state.getString("autor",""));
        }
        visibility();anonimo.setOnCheckedChangeListener((view,checked)->{if(checked) autor.setText("");visibility();});
        button(R.string.guardar,v->{
            String desc=description.getText().toString().trim(),name=autor.getText().toString().trim();
            if(desc.length()<10||desc.length()>500) {description.setError("Escribí entre 10 y 500 caracteres");return;}
            if(!anonimo.isChecked()&&(name.length()<2||name.length()>80)) {autor.setError("Escribí tu nombre entre 2 y 80 caracteres");return;}
            try {
                String uuid=app.db.crearReporte(((NamedItem)lugar.getSelectedItem()).id,
                    ((NamedItem)categoria.getSelectedItem()).id,desc,anonimo.isChecked(),name);
                Log.i("CampusReport","Guardar: reporte creado, UUID="+uuid);
                Toast.makeText(this,"Reporte guardado",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(this,DetailActivity.class);intent.putExtra("uuid",uuid);startActivity(intent);finish();
            } catch(Exception e) {Log.e("CampusReport","No se pudo guardar",e);Toast.makeText(this,"No se pudo guardar. Intentá de nuevo.",Toast.LENGTH_LONG).show();}
        });backButton();
    }
    private EditText field(String hint,int max) {
        EditText edit=new EditText(this);edit.setHint(hint);edit.setTextSize(16);
        edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(max)});
        content.addView(edit,new LinearLayout.LayoutParams(-1,-2));return edit;
    }
    private void visibility() {int vis=anonimo.isChecked()?View.GONE:View.VISIBLE;autor.setVisibility(vis);autorLabel.setVisibility(vis);}
    private Spinner spinner(List<NamedItem> items) {
        Spinner s=new Spinner(this);ArrayAdapter<NamedItem> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);s.setAdapter(adapter);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(52));lp.bottomMargin=dp(18);content.addView(s,lp);return s;
    }
    private void select(Spinner spinner,List<NamedItem> items,long id) {for(int i=0;i<items.size();i++) if(items.get(i).id==id) spinner.setSelection(i);}
    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if(description!=null) {
            state.putString("description",description.getText().toString());
            state.putLong("place",((NamedItem)lugar.getSelectedItem()).id);state.putLong("category",((NamedItem)categoria.getSelectedItem()).id);
            state.putBoolean("anonimo",anonimo.isChecked());
            if(!anonimo.isChecked()) state.putString("autor",autor.getText().toString());
        }
    }
}
