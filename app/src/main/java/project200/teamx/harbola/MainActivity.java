package project200.teamx.harbola;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    protected boolean _active = true;
    protected int _splashTime = 3000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Thread splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while (_active && (waited < _splashTime)) {
                        sleep(100);
                        if (_active) {
                            waited += 100;
                        }
                    }
                } catch (Exception e) {

                } finally {

                    finish();
                    MainActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            Intent intent = new Intent(MainActivity.this, After_SplashScreen.class);
                            startActivity(intent);
                        }});
                }
            };
        };
        splashTread.start();
    }
}
