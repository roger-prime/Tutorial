package com.example.campusreport.ui;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.campusreport.CampusApp;
import com.example.campusreport.R;

public abstract class BaseActivity extends Activity {
    protected LinearLayout content;
    protected CampusApp app;
    protected int dp(int value) { return Math.round(value*getResources().getDisplayMetrics().density); }
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);app=(CampusApp)getApplication();setContentView(R.layout.activity_shell);
        content=findViewById(R.id.content);
        View root=findViewById(R.id.root);
        root.setOnApplyWindowInsetsListener((view,insets)->{
            view.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets;
        });
        root.requestApplyInsets();
    }
    protected TextView text(String value,int size,int color) {
        TextView v=new TextView(this);v.setText(value);v.setTextSize(size);v.setTextColor(color);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(14);
        content.addView(v,lp);return v;
    }
    protected void title(String value,String subtitle) {
        text(value,30,Color.rgb(16,33,62)).setTypeface(null,Typeface.BOLD);
        text(subtitle,16,Color.rgb(80,99,126));
    }
    protected TextView card(String value) {
        TextView v=text(value,17,Color.rgb(16,33,62));v.setPadding(dp(20),dp(20),dp(20),dp(20));
        v.setBackgroundResource(R.drawable.card);return v;
    }
    protected Button button(String value,View.OnClickListener action) {
        Button b=new Button(this,null,0,R.style.PrimaryButton);b.setText(value);b.setOnClickListener(action);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(12);
        content.addView(b,lp);return b;
    }
    protected void open(Class<?> type) { startActivity(new Intent(this,type)); }
    protected Button button(int label,View.OnClickListener action) { return button(getString(label),action); }
    protected void backButton() { button(R.string.volver,v->finish()); }
}
