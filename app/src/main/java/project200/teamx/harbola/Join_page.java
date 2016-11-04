package project200.teamx.harbola;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class Join_page extends AppCompatActivity {


    public final int fileRequestID = 55;
    public final int port = 8080;
    ClientRxThread clientRxThread;
    private String path;
    private File downloadTarget;
    private Intent serverServiceIntent;
    private boolean serverThreadActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_page);

        if(isNetworkAvailable()){
            Toast.makeText(getApplicationContext(), "ACTIVE", Toast.LENGTH_SHORT).show();

        }
        else
            Toast.makeText(getApplicationContext(), "PASSIVE" , Toast.LENGTH_SHORT).show();

        /// IP VALIDATOR
        EditText ipEditText = (EditText) findViewById(R.id.editText);
        final Pattern PARTIAl_IP_ADDRESS =
                Pattern.compile("^((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])\\.){0,3}"+
                        "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])){0,1}$");

        ipEditText.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after) {}

            private String mPreviousText = "";
            @Override
            public void afterTextChanged(Editable s) {
                if(PARTIAl_IP_ADDRESS.matcher(s).matches()) {
                    mPreviousText = s.toString();
                } else {
                    s.replace(0, s.length(), mPreviousText);
                }
            }
        });
        ////

        final Button refresh_button = (Button) findViewById(R.id.refresh);
        refresh_button.performClick();
        final EditText edittext = (EditText) findViewById(R.id.editText);

        edittext.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    edittext.setInputType(InputType.TYPE_NULL);
                    refresh_button.performClick();
                    return true;
                }
                return false;
            }
        });

    }
    public void save_it(View a) throws IOException{

        File src = new File(Environment.getExternalStorageDirectory(), "/test.png");
        File dst = new File(Environment.getExternalStoragePublicDirectory("/") + "/Harbola");
        dst.mkdirs();
        System.out.println("HEREEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEe " + dst.getPath().toString());
        if(!dst.exists()){
            dst.createNewFile();
        }
        dst = new File(dst.toString() + "/Harbola_File_" + System.currentTimeMillis() + ".png");

        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();

    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class ClientRxThread extends Thread {
        String dstAddress;
        int dstPort;

        ClientRxThread(String address, int port) {
            dstAddress = address;
            dstPort = port;
        }

        @Override
        public void run() {
            Socket socket = null;
            System.out.println("RUNNNNNNNED");

            try {
                System.out.println("INSIDE TRY + " + dstAddress + " " + dstPort);
                //InetSocketAddress socketAddress = new InetSocketAddress(dstAddress, dstPort);

                socket = new Socket(dstAddress, dstPort);
                //socket.connect(socketAddress);
                System.out.println("AFTER SOCKET");

                final File file = new File(Environment.getExternalStorageDirectory(), "/test.png");
                //Toast.makeText(getApplicationContext(), "SERVER CREATED", Toast.LENGTH_SHORT).show();

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                byte[] bytes;
                FileOutputStream fos = null;
                try {
                    bytes = (byte[])ois.readObject();
                    fos = new FileOutputStream(file);
                    fos.write(bytes);
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    if(fos!=null){
                        fos.close();
                    }

                }

                socket.close();

                Join_page.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //Toast.makeText(Join_page.this, "Finished ", Toast.LENGTH_LONG).show();
                        String path_for_shocase = file.getPath();
                        ImageView SC = (ImageView) findViewById(R.id.ShowCase);
                        Bitmap myBitmap = BitmapFactory.decodeFile(path_for_shocase);
                        if(myBitmap!=null) SC.setImageBitmap(myBitmap);
                        else SC.setImageResource(R.drawable.ic_launcher);
                    }});

            } catch (IOException e) {

                e.printStackTrace();

                final String eMsg = "Something wrong: " + e.getMessage();
                System.out.println("EMSG: " + eMsg);
                Join_page.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(Join_page.this,  eMsg,  Toast.LENGTH_LONG).show();
                    }});

            } finally {
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    public void Refresh_Clicked(View view){

        //System.out.println("CLiCkED");
        path = Environment.getExternalStorageDirectory() + "/";
        downloadTarget = new File(path + "test.png");
        EditText ipEditText = (EditText) findViewById(R.id.editText);
        String IP = ipEditText.getText().toString();
        System.out.println("-----IP: " + IP + " " + Patterns.IP_ADDRESS.matcher(IP).matches());
        if(!Patterns.IP_ADDRESS.matcher(IP).matches()){
            Toast.makeText(Join_page.this,  "Please Enter an IP Address",  Toast.LENGTH_LONG).show();
        }
        else {
            ipEditText.setEnabled(false);
            clientRxThread = new ClientRxThread(IP, 8080);
            clientRxThread.start();
        }
    }
    public void setServerWifiStatus(String message)
    {
//        server_wifi_status_text.setText(message);
    }

    public void setServerStatus(String message)
    {

        //server_status_text.setText(message);
    }

    public void setServerFileTransferStatus(String message)
    {
//        TextView server_status_text = (TextView) findViewById(R.id.server_file_transfer_status);
//        server_status_text.setText(message);
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        setServerFileTransferStatus("CHECK");
//        signalActivity("Ready to receive Image from Pair");
        System.out.println("On resume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("On Pause");
        //stopServer(null);
        //unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("On Destroy");

        //stopServer(null);

        //stopService(serverServiceIntent);

        //Unregister broadcast receiver
//        try {
//            unregisterReceiver(mReceiver);
//        } catch (IllegalArgumentException e) {
//            // This will happen if the server was never running and the stop
//            // button was pressed.
//            // Do nothing in this case.
//        }
    }
}
