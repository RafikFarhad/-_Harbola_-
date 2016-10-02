package project200.teamx.harbola;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.TextView;

/**
 * Created by notselected on 10/1/16.
 */

public class Admin_WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private Create_page mActivity;
    WifiP2pManager.PeerListListener myPeerListListener;


    public Admin_WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, Create_page activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // Check to see if Wi-Fi is enabled and notify appropriate activity
            System.out.println("Wi-Fi is enabled");
        }

        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            // Respond to new connection or disconnections

            NetworkInfo networkState = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(networkState.isConnected())
            {
                mActivity.refresh("Connection Status: Connected " + " to " + networkState.toString());
                //activity.setServerStatus("Connection Status: Connected");
            }
            else
            {
                //activity.setServerStatus("Connection Status: Disconnected");
                mManager.cancelConnect(mChannel, null);

            }
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            // Respond to this device's wifi state changing
        }


    }

}

