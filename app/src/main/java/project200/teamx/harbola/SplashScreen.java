package project200.teamx.harbola;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Locale;

public class SplashScreen extends AppCompatActivity {
     // time to display the splash screen in ms



    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
    }

    public void create_request(View view){

        Intent intent = new Intent(this, Create_page.class);
        startActivity(intent);

    }

    public void join_request(View view){

        Intent intent = new Intent(this, Join_page.class);
        startActivity(intent);

    }
    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu, menu);
        System.out.println("Reached");
        menu.add(0, 1, 0, "English");
        menu.add(0, 2, 0, "Bangla");
        menu.add(0, 3, 0, "Exit");
        return true;
    }
    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
            {
                setLocale("en");
            }
            return true;
            case 2:
            {
                setLocale("bn");
            }
            return true;
            case 3:
            {
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
                super.onDestroy();
            }
            return true;
        }
        return false;
    }
    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        this.recreate();
    }
}