package com.example.campusreport.ui;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.campusreport.data.NamedItem;
public class PlacesActivity extends BaseActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);title("Lugares disponibles","Esta lista se consulta desde SQLite, también sin conexión.");
        if(app.db.lugares().isEmpty()) card("Todavía no hay lugares. Volvé a Inicio y descargá el catálogo.");
        for(NamedItem item:app.db.lugares()) card(item.nombre+"\nCódigo "+item.id);
        text("Categorías",21,0xFF10213E);
        for(NamedItem item:app.db.categorias()) card(item.nombre);
        backButton();
    }
}
