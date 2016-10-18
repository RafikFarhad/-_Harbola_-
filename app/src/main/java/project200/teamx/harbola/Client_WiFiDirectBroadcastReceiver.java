package project200.teamx.harbola;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;


/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */


public class Client_WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private Create_page mActivity;
    WifiP2pManager.PeerListListener myPeerListListener;


    public Client_WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, Create_page activity) {
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

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
//            if (mManager != null) {
//                mManager.requestPeers(mChannel, myPeerListListener);
//            }
            //This broadcast is sent when status of in range peers changes. Attempt to get current list of peers.

//            mManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
//
//                public void onPeersAvailable(WifiP2pDeviceList peers) {
//                    mActivity.displayPeers(peers);
//                }
//            });

            //update UI with list of peers
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            NetworkInfo networkState = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            WifiP2pInfo wifiInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

            if(networkState.isConnected())
            {
                //set client state so that all needed fields to make a transfer are ready
                System.out.println("Connection Status: Connected");
                //activity.setTransferStatus(true);
                mActivity.setNetworkToReadyState(true, wifiInfo, device);
                mActivity.setClientStatus("Connection Status: Connected");
            }
            else
            {
                //set variables to disable file transfer and reset client back to original state
                System.out.println("Connection Status: Disonnected");

//                mActivity.setTransferStatus(false);
                //mActivity.setClientStatus("Connection Status: Disconnected");
//                mManager.cancelConnect(mChannel, null);

            }
            //activity.setClientStatus(networkState.isConnected());

// Respond to new connection or disconnections
            // Respond to new connection or disconnections
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            // Respond to this device's wifi state changing
        }


    }

}
