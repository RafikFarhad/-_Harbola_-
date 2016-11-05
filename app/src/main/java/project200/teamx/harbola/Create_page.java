package project200.teamx.harbola;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;


public class Create_page extends AppCompatActivity {

    private static int Load_Until = 40;
    public final int port = 8080;

    private boolean connectedAndReadyToSendFile;

    private File fileToSend;
    private boolean transferActive;
    boolean issent;

    ServerSocket serverSocket;

    //variable for selection intent
    private final int PICKER = 1;
    //variable to store the currently selected image
    private int currentPic = 0;
    //gallery object
    private Gallery picGallery;
    //image view for larger display
    private ImageView picView;
    //adapter for gallery view
    private PicAdapter imgAdapt;
    ArrayList<String> listOfAllImages;

    ServerSocketThread serverSocketThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_page);
        /// Intent is created
        System.out.println("On Create");
        System.out.println("IP: " + getIpAddress());
        TextView fileTransferStatusText = (TextView) findViewById(R.id.file_transfer_status);
        fileTransferStatusText.setText(getIpAddress() + " " + port);

        connectedAndReadyToSendFile = false;
        fileToSend = null;
        transferActive = false;
        issent = false;

        fileToSend = new File("/test.png");
        serverSocketThread = new ServerSocketThread();
        serverSocketThread.start();
        setClientFileTransferStatus("Client is currently idle");

        Toast.makeText(getApplicationContext(), "Broadcast", Toast.LENGTH_SHORT).show();

        picView = (ImageView) findViewById(R.id.picture);
        picView.setImageResource(R.drawable.logo_extended);

        //get the gallery view
        picGallery = (Gallery) findViewById(R.id.gallery);
        //create a new adapter
        imgAdapt = new PicAdapter(this);

        //set the gallery adapter
        picGallery.setAdapter(imgAdapt);

        //Toast.makeText(getApplicationContext(), "DEFAULT;", Toast.LENGTH_SHORT).show();
        currentPic = 1;

        listOfAllImages = getAllShownImagesPath(this);
        System.out.println("SIZE:   " + listOfAllImages.size());
        int total = listOfAllImages.size();
        total = total > Load_Until ? Load_Until : total;               /// MAJOR PROBLEM
        try {
            picGallery.setAdapter(imgAdapt);
        } catch (OutOfMemoryError e) {
            Toast.makeText(getApplicationContext(), "OUT OF MEMORY!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error in reading external storage!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        picGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //handle clicks
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //set the larger image view to display the chosen bitmap calling method of adapter class
                //picView.setImageBitmap(BitmapFactory.decodeFile(new File(listOfAllImages.get(position)).getAbsolutePath()));
                System.out.println("********** = " + listOfAllImages.get(position));
                Picasso.with(getBaseContext()).load(new File(listOfAllImages.get(position))).into(picView);
                Toast.makeText(getApplicationContext(), "Sending This Image", Toast.LENGTH_SHORT).show();
                fileToSend = new File(listOfAllImages.get(position));
                serverSocketThread.interrupt();
                serverSocketThread = new ServerSocketThread();
                serverSocketThread.start();
                // sendFile();
                System.out.println("File Is sending......... " + fileToSend.getName());
            }
        });

    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        String aa = getResources().getString(R.string.address);
                        ip += aa + ": "   + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }

    public class ServerSocketThread extends Thread {

        @Override
        public void run() {
            Socket socket = null;

            try {
                serverSocket = new ServerSocket(port);
                serverSocket.setReuseAddress(true);
                //serverSocket.bind(new InetSocketAddress(port));
                Create_page.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("I'm waiting here: "+ serverSocket.getLocalPort());
                    }});

                while (true) {
                    socket = serverSocket.accept();
                    FileTxThread fileTxThread = new FileTxThread(socket);
                    fileTxThread.start();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
//                Toast.makeText(getApplicationContext(), "Broadcasting stopped by user", Toast.LENGTH_SHORT).show();
//                Create_page.this.runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        Toast.makeText(Create_page.this,  "Broadcasting stopped by user",  Toast.LENGTH_LONG).show();
//                    }});

            } finally {
                if (socket != null) {
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

    public class FileTxThread extends Thread {
        Socket socket;

        FileTxThread(Socket socket){
            this.socket= socket;
        }

        @Override
        public void run() {
//            File file = new File(Environment.getExternalStorageDirectory(), "android-er_sketch_1000.png");
            File file;
            file = fileToSend;

            byte[] bytes = new byte[(int) file.length()];
            BufferedInputStream bis;
            try {
                bis = new BufferedInputStream(new FileInputStream(file));
                bis.read(bytes, 0, bytes.length);

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(bytes);
                oos.flush();

                socket.close();

                final String sentMsg = "File sent to: " + socket.getInetAddress();

//                Create_page.this.runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        Toast.makeText(Create_page.this, sentMsg, Toast.LENGTH_LONG).show();
//                    }});

            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    }

    ///////////////////////////////////////////Gallery Area

    public static ArrayList<String> getAllShownImagesPath(Activity activity) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = activity.getContentResolver().query(uri, projection, null, null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        int i = 0;
        while (cursor.moveToNext() && i<Load_Until) {
            i++;
            absolutePathOfImage = cursor.getString(column_index_data);
            listOfAllImages.add(absolutePathOfImage);
            //System.out.println(absolutePathOfImage);
        }
        cursor.close();
        return listOfAllImages;
    }

    public class PicAdapter extends BaseAdapter {

        //use the default gallery background image
        int defaultItemBackground;

        //gallery context
        private Context galleryContext;

        //array to store bitmaps to display
        private Bitmap[] imageBitmaps;

        //placeholder bitmap for empty spaces in gallery
        Bitmap placeholder;


        public Context context;
        private LayoutInflater inflater;

        private String[] imageUrls;

        public PicAdapter(Context c) {

            //instantiate context
            galleryContext = c;

            //create bitmap array
            imageBitmaps = new Bitmap[200];

            //decode the placeholder image
            placeholder = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

            //more processing
        }

        //return number of data items i.e. bitmap images
        public int getCount() {
            return imageBitmaps.length;
        }

        //return item at specified position
        public Object getItem(int position) {
            return position;
        }

        //return item ID at specified position
        public long getItemId(int position) {
            return position;
        }
        //return bitmap at specified position for larger display
        public Bitmap getPic(int posn) {
            //return bitmap at posn index
            return imageBitmaps[posn];
        }
        //get view specifies layout and display options for each thumbnail in the gallery
        public View getView(int position, View convertView, ViewGroup parent) {

            //create the view
            ImageView imageView = new ImageView(galleryContext);
            //specify the bitmap at this position in the array
            try {
                if(position<Load_Until) imageView.setImageBitmap(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(new File(listOfAllImages.get(position)).getPath()), 100, 100));
            }
            catch (OutOfMemoryError e) {
                Toast.makeText(getApplicationContext(), "OUT OF MEMORY!", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error in readin external storage!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
//            //imageView.setImageBitmap(imageBitmaps[position]);
//            //Picasso.with(context).load(listOfAllImages.get(position)).into(imageView);
//            //set layout options
//            imageView.setLayoutParams(new Gallery.LayoutParams(300, 200));
//            //scale type within view area
//            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//            //set default gallery item background
//            imageView.setBackgroundResource(defaultItemBackground);
//            //return the view
            return imageView;
        }

        //helper method to add a bitmap to the gallery when the user chooses one
        public void addPic(Bitmap newPic)
        {
            //set at currently selected index
            imageBitmaps[currentPic] = newPic;
        }
    }
    public void setClientWifiStatus(String message)
    {
//        TextView connectionStatusText = (TextView) findViewById(R.id.client_wifi_status_text);
//        connectionStatusText.setText(message);
    }

    public void setClientStatus(String message)
    {
//        TextView clientStatusText = (TextView) findViewById(R.id.client_status_text);
//        clientStatusText.setText(message);
    }

    public void setClientFileTransferStatus(String message)
    {
//        TextView fileTransferStatusText = (TextView) findViewById(R.id.file_transfer_status);
//        fileTransferStatusText.setText(message);
    }

    public void setTargetFileStatus(String message)
    {
//        TextView targetFileStatus = (TextView) findViewById(R.id.selected_filename);
//        targetFileStatus.setText(message);
    }
    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        try {
            //registerReceiver(mReceiver, mIntentFilter);
        }
        catch (Exception e) {
            // This will happen if the server was never running and the stop
            // button was pressed.
            // Do nothing in this case.
        }
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}


//    public void connectToPeer_Create_page(final WifiP2pDevice wifiPeer)
//    {
//        this.targetDevice = wifiPeer;
//
//        WifiP2pConfig config = new WifiP2pConfig();
//        config.deviceAddress = wifiPeer.deviceAddress;
//        System.out.println("Reached CRETAE PAGE CONNECT TO PEER " + config.toString());
//        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener()  {
//            public void onSuccess() {
//                Toast.makeText(getApplicationContext(), "Connection to " + targetDevice.deviceName + " sucessful", Toast.LENGTH_LONG).show();
//
//                //startServer();
//                //setClientStatus("Connection to " + targetDevice.deviceName + " sucessful");
//            }
//
//            public void onFailure(int reason) {
//                //setClientStatus("Connection to " + targetDevice.deviceName + " failed");
//
//            }
//        });
//
//    }
//
//    public void setNetworkToReadyState(boolean status, WifiP2pInfo info, WifiP2pDevice device)
//    {
//        wifiInfo = info;
//        targetDevice = device;
//        connectedAndReadyToSendFile = status;
//    }

//    private void stopClientReceiver()
//    {
//        try
//        {
//            unregisterReceiver(mReceiver);
//        }
//        catch(IllegalArgumentException e)
//        {
//            //This will happen if the server was never running and the stop button was pressed.
//            //Do nothing in this case.
//        }
//    }
//
//    public void searchForPeers(View view) {
//
//        //Discover peers, no call back method given
//        mManager.discoverPeers(mChannel, null);
//
//    }

//    public void sendFile() {
//
//        //Only try to send file if there isn't already a transfer active
//        System.out.println("Send File : " + fileToSend.toString());
//        System.out.println("Send File  status: " + filePathProvided + " " + connectedAndReadyToSendFile);
//
//        if(!transferActive)
//        {
//            if(!filePathProvided)
//            {
//                setClientFileTransferStatus("Select a file to send before pressing send");
//            }
//            else if(!connectedAndReadyToSendFile)
//            {
//                setClientFileTransferStatus("You must be connected to a server before attempting to send a file");
//            }
//
//	        else if(targetDevice == null)
//	        {
//	        	setClientFileTransferStatus("Target Device network information unknown");
//	        }
//
//            else if(wifiInfo == null)
//            {
//                setClientFileTransferStatus("Missing Wifi P2P information");
//            }
//            else
//            {
//                //Launch client service
//                clientServiceIntent = new Intent(this, ClientService.class);
//                clientServiceIntent.putExtra("fileToSend", fileToSend);
//                clientServiceIntent.putExtra("port", new Integer(port));
//                clientServiceIntent.putExtra("targetDevice", targetDevice);
//                clientServiceIntent.putExtra("wifiInfo", wifiInfo);
//                clientServiceIntent.putExtra("clientResult", new ResultReceiver(null) {
//                    @Override
//                    protected void onReceiveResult(int resultCode, final Bundle resultData) {
//
//                        if(resultCode == port )
//                        {
//                            if (resultData == null) {
//                                //Client service has shut down, the transfer may or may not have been successful. Refer to message
//                                transferActive = false;
//                            }
//                            else
//                            {
//                                final TextView client_status_text = (TextView) findViewById(R.id.file_transfer_status);
//
//                                client_status_text.post(new Runnable() {
//                                    public void run() {
//                                        client_status_text.setText((String)resultData.get("message"));
//                                    }
//                                });
//                            }
//                        }
//
//                    }
//                });
//
//                transferActive = true;
//                startService(clientServiceIntent);
//                //end
//            }
//        }
//    }


