package project200.teamx.harbola;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Parcel;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Create_page extends AppCompatActivity {

    public final int fileRequestID = 55;
    public final int port = 7950;

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    ///
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_page);

        /// Intent is created
        System.out.println("On Create");


        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new Admin_WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        registerReceiver(mReceiver, mIntentFilter);


        Toast.makeText(getApplicationContext(), "Broadcast", Toast.LENGTH_SHORT).show();

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

        /*mManager.createGroup(mChannel, new WifiP2pManager.ActionListener()  {
            public void onSuccess() {

                Toast.makeText(getApplicationContext(), "Group Created Successful", Toast.LENGTH_SHORT).show();
                System.out.println("Group Created Successful");
                //setServerFileTransferStatus("WiFi Group creation successful");
                //Group creation successful
            }
            public void onFailure(int reason) {

                Toast.makeText(getApplicationContext(), "Group Created Failed " + String.valueOf(reason), Toast.LENGTH_SHORT).show();
                System.out.println("Group Created Failed " + reason);
                //setServerFileTransferStatus("WiFi Group creation failed");
                //Group creation failed
            }
        });*/


        //// FOR VIEW CUSTOM GALLERY

        //get the large image view
        picView = (ImageView) findViewById(R.id.picture);

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
        picGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //handle clicks
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //set the larger image view to display the chosen bitmap calling method of adapter class
                //picView.setImageBitmap(BitmapFactory.decodeFile(new File(listOfAllImages.get(position)).getAbsolutePath()));
                System.out.println("********** = " + listOfAllImages.get(position));
                Picasso.with(getBaseContext()).load(new File(listOfAllImages.get(position))).into(picView);
                Toast.makeText(getApplicationContext(), "Send This Image Now", Toast.LENGTH_SHORT).show();
            }
        });


        int total = listOfAllImages.size();
        //total = total > 100 ? 100 : total;                                                 /// MAJOR PROBLEM


        try {
            picGallery.setAdapter(imgAdapt);
        } catch (OutOfMemoryError e) {
            Toast.makeText(getApplicationContext(), "OUT OF MEMORY!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error in reading external storage!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

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
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            listOfAllImages.add(absolutePathOfImage);
            //System.out.println(absolutePathOfImage);
        }
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
                imageView.setImageBitmap(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(new File(listOfAllImages.get(position)).getPath()), 100, 100));
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
    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

}
