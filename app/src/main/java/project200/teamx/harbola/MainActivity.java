package project200.teamx.harbola;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

}
