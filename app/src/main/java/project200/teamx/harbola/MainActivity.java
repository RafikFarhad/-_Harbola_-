package project200.teamx.harbola;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        String languageToLoad  = "bn"; // your language
//        Locale locale = new Locale(languageToLoad);
//        Locale.setDefault(locale);
//        Configuration config = new Configuration();
//        config.locale = locale;
//        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        setContentView(R.layout.activity_main);

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
        menu.add(0, 1, 0, "English");
        menu.add(0, 2, 0, "Bangla");
        menu.add(0, 3, 0, "Exit");
        return true;
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


}
