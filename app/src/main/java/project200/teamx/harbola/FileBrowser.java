package project200.teamx.harbola;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by notselected on 10/1/16.
 */

public class FileBrowser extends Activity {

    private String root;
    private String currentPath;

    private ArrayList<String> targets;
    private ArrayList<String> paths;


    private File targetFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        root = "/";
        currentPath = root;

        targets = null;
        paths = null;

        targetFile = null;

        showDir(currentPath);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void selectDirectory(View view) {

        File f = new File(currentPath);
        targetFile = f;

        //Return target File to activity
        returnTarget();


    }


    public void setCurrentPathText(String message)
    {
        TextView fileTransferStatusText = (TextView) findViewById(R.id.current_path);
        fileTransferStatusText.setText(message);
    }


    private void showDir(String targetDirectory){

        setCurrentPathText("Current Directory: " + currentPath);

        targets = new ArrayList<String>();
        paths = new ArrayList<String>();

        File f = new File(targetDirectory);
        File[] directoryContents = f.listFiles();


        if (!targetDirectory.equals(root))

        {
            targets.add(root);
            paths.add(root);
            targets.add("../");
            paths.add(f.getParent());
        }

        for(File target: directoryContents)
        {
            paths.add(target.getPath());

            if(target.isDirectory())
            {
                targets.add(target.getName() + "/");
            }
            else
            {
                targets.add(target.getName());

            }

        }

        ListView fileBrowserListView = (ListView) findViewById(R.id.file_browser_listview);

        ArrayAdapter<String> directoryData = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, targets);
        fileBrowserListView.setAdapter(directoryData);


        fileBrowserListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View view, int pos,long id) {

                File f = new File(paths.get(pos));

                if(f.isFile())
                {
                    targetFile = f;
                    returnTarget();
                    //Return target File to activity
                }
                else
                {
                    //f must be a dir
                    if(f.canRead())
                    {
                        currentPath = paths.get(pos);
                        Toast.makeText(getApplicationContext(), currentPath, Toast.LENGTH_SHORT).show();
                        showDir(paths.get(pos));
                    }

                }


            }
            // TODO Auto-generated method stub
        });

	    /*
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle("WiFi Direct File Transfer");
		*/


    }

    public void returnTarget()
    {

        Intent returnIntent = new Intent();
        returnIntent.putExtra("file", targetFile);
        //setResult(RESULT_OK, returnIntent);
        finish();

    }



}


