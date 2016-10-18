package project200.teamx.harbola;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class Join_page extends AppCompatActivity {


    public final int fileRequestID = 55;
    public final int port = 7950;

    WifiP2pManager.PeerListListener myPeerListListener;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    WifiP2pDevice targetDevice;
    //    private WifiP2pManager wifiManager;
//    private WifiP2pManager.Channel wifichannel;
//    private BroadcastReceiver wifiServerReceiver;
//    private IntentFilter wifiServerReceiverIntentFilter;
    private String path;
    private File downloadTarget;
    private Intent serverServiceIntent;
    private boolean serverThreadActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_page);


        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new Admin_WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        registerReceiver(mReceiver, mIntentFilter);
        //searchForPeers();
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Discovery Process Started", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(getApplicationContext(), "Discovery Process Starting Failed", Toast.LENGTH_LONG).show();
            }
        });
        Toast.makeText(getApplicationContext(), "Searching for !_Harbola_! group", Toast.LENGTH_SHORT).show();
        TextView serverServiceStatus = (TextView) findViewById(R.id.server_status_text);
        serverServiceStatus.setText("server_stopped");

        path = "/";
        downloadTarget = new File(path);

        serverServiceIntent = null;
        serverThreadActive = false;

        setServerFileTransferStatus("No File being transfered");

        //registerReceiver(wifiServerReceiver, wifiServerReceiverIntentFilter);
//        startServer();
        //displayPeers();
    }

    public void startServer() {

        //If server is already listening on port or transfering data, do not attempt to start server service
        if(!serverThreadActive)
        {
            Toast.makeText(getApplicationContext(), "SERVER CREATED with port: " + port, Toast.LENGTH_SHORT).show();
            System.out.println("SERVER CREATED");
            //Create new thread, open socket, wait for connection, and transfer file
            serverServiceIntent = new Intent(this, ServerService.class);
            serverServiceIntent.putExtra("saveLocation", downloadTarget);
            serverServiceIntent.putExtra("port", new Integer(port));
            serverServiceIntent.putExtra("serverResult", new ResultReceiver(null) {
                @Override
                protected void onReceiveResult(int resultCode, final Bundle resultData) {
                    if(resultCode == port )
                    {
                        System.out.println("--------------- result: " );
                        if (resultData == null) {
                            //Server service has shut down. Download may or may not have completed properly.
                            serverThreadActive = false;
                            final TextView server_status_text = (TextView) findViewById(R.id.server_status_text);
                            server_status_text.post(new Runnable() {
                                public void run() {
                                    server_status_text.setText("server_stopped");
                                }
                            });
                        }
                        else
                        {
                            final TextView server_file_status_text = (TextView) findViewById(R.id.server_file_transfer_status);
                            server_file_status_text.post(new Runnable() {
                                public void run() {
                                    System.out.println("--------------- result: " + resultData.getString("path_for_showcase"));
                                    String msg = (String)resultData.get("message");
                                    if(msg!=null) server_file_status_text.setText(msg);
                                    String path_for_shocase = (String) resultData.get("path_for_showcase");
                                    ImageView SC = (ImageView) findViewById(R.id.ShowCase);
                                    Bitmap myBitmap = BitmapFactory.decodeFile(path_for_shocase);
                                    if(myBitmap!=null) SC.setImageBitmap(myBitmap);
                                    else SC.setImageResource(R.drawable.ic_launcher);
                                }
                            });
                        }
                    }

                }
            });

            serverThreadActive = true;
            /// here starts ServerService.java
            startService(serverServiceIntent);
            System.out.println("startService(serverServiceIntent); + " + serverServiceIntent.getExtras().get("port"));

            //Set status to running
            TextView serverServiceStatus = (TextView) findViewById(R.id.server_status_text);
            serverServiceStatus.setText("server_running");

        }
        else
        {
            //Set status to already running
            TextView serverServiceStatus = (TextView) findViewById(R.id.server_status_text);
            serverServiceStatus.setText("The server is already running");

        }
    }

    public void stopServer(View view) {


        //stop download thread
        if(serverServiceIntent != null)
        {
            stopService(serverServiceIntent);

        }

    }


//    public void startClientActivity(View view) {
//
//        stopServer(null);
//        Intent clientStartIntent = new Intent(this, ClientActivity.class);
//        startActivity(clientStartIntent);
//    }

    public void searchForPeers() {

        //Discover peers, no call back method given
        mManager.discoverPeers(mChannel, null);

    }

    public void displayPeers(final WifiP2pDeviceList peers)
    {
        //Dialog to show errors/status
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("WiFi Direct File Transfer");

        //Get list view
        ListView peerView = (ListView) findViewById(R.id.peers_listview);

        //Make array list
        ArrayList<String> peersStringArrayList = new ArrayList<String>();

        //Fill array list with strings of peer names
        int t = 1;
        for(WifiP2pDevice wd : peers.getDeviceList())
        {
            peersStringArrayList.add( t + " . -" + wd.deviceName);
        }

        //Set list view as clickable
        peerView.setClickable(true);

        //Make adapter to connect peer data to list view
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, peersStringArrayList.toArray());

        //Show peer data in listview
        peerView.setAdapter(arrayAdapter);


        peerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {

                //Get string from textview
                TextView tv = (TextView) view;

                WifiP2pDevice device = null;

                //Search all known peers for matching name
                for(WifiP2pDevice wd : peers.getDeviceList()) {
                    String test = tv.getText().toString();
                    test = test.substring(test.indexOf("-")+1);
//                    dialog.setMessage(test);
//                    dialog.show();
                    if(wd.deviceName.equals(test))
                        device = wd;
                }

                if(device != null){
                    //Connect to selected peer
                    connectToPeer(device);
                }
                else {
                    dialog.setMessage("Failed");
                    dialog.show();

                }
            }
            // TODO Auto-generated method stub
        });

    }
    public void connectToPeer(final WifiP2pDevice wifiPeer)
    {
        this.targetDevice = wifiPeer;

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = wifiPeer.deviceAddress;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener()  {
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Connection to " + targetDevice.deviceName + " sucessful", Toast.LENGTH_LONG).show();
                startServer();
                //setClientStatus("Connection to " + targetDevice.deviceName + " sucessful");
            }

            public void onFailure(int reason) {
                //setClientStatus("Connection to " + targetDevice.deviceName + " failed");

            }
        });

    }
    public void startFileBrowseActivity(View view) {

        Intent clientStartIntent = new Intent(this, FileBrowser.class);
        startActivityForResult(clientStartIntent, fileRequestID);
        //Path returned to onActivityResult

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK && requestCode == fileRequestID) {
            //Fetch result
            File targetDir = (File) data.getExtras().get("file");

            /*if(targetDir.isDirectory())
            {
                if(targetDir.canWrite())
                {
                    downloadTarget = targetDir;
                    TextView filePath = (TextView) findViewById(R.id.server_file_path);
                    filePath.setText(targetDir.getPath());
                    setServerFileTransferStatus("Download directory set to " + targetDir.getName());

                }
                else
                {
                    setServerFileTransferStatus("You do not have permission to write to " + targetDir.getName());
                }

            }
            else
            {
                setServerFileTransferStatus("The selected file is not a directory. Please select a valid download directory.");
            }*/

        }
    }

    public void setServerWifiStatus(String message)
    {
//        server_wifi_status_text.setText(message);
    }

    public void setServerStatus(String message)
    {
        TextView server_status_text = (TextView) findViewById(R.id.server_status_text);
        server_status_text.setText(message);
    }


    public void setServerFileTransferStatus(String message)
    {
        TextView server_status_text = (TextView) findViewById(R.id.server_file_transfer_status);
        server_status_text.setText(message);
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

        stopServer(null);

        //stopService(serverServiceIntent);

        //Unregister broadcast receiver
        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            // This will happen if the server was never running and the stop
            // button was pressed.
            // Do nothing in this case.
        }
    }
}
