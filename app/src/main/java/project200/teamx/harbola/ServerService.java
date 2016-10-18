package project200.teamx.harbola;

/**
 * Created by notselected on 10/17/16.
 */

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.Bundle;
import android.app.IntentService;
import android.content.Intent;
import android.os.ResultReceiver;
import android.widget.Toast;

public class ServerService extends IntentService {

    private boolean serviceEnabled;

    private int port;
    private File saveLocation;
    private ResultReceiver serverResult;

    public ServerService() {
        super("ServerService");
        serviceEnabled = true;

//        signalActivity("/storage/sdcard0/WhatsApp/Media/WhatsApp Images/IMG-20160814-WA0001.jpg");
//        System.out.println(1 + " storage/sdcard0/WhatsApp/Media/WhatsApp Images/IMG-20160814-WA0001.jpg");

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        port = ((Integer) intent.getExtras().get("port")).intValue();
        saveLocation = (File) intent.getExtras().get("saveLocation");
        serverResult = (ResultReceiver) intent.getExtras().get("serverResult");

        //System.out.println(2 + " storage/sdcard0/WhatsApp/Media/WhatsApp Images/IMG-20160814-WA0001.jpg");
        //signalFinalActivity("/storage/sdcard0/WhatsApp/Media/WhatsApp Images/IMG-20160814-WA0001.jpg");

        signalActivity("Ready to receive Image from Pair");


        String fileName = "";

        ServerSocket welcomeSocket = null;
        Socket socket = null;



        try {
            System.out.println("++++++++++++++++ ");
            InetAddress addr = InetAddress.getByName("10.0.2.2");
            System.out.println("****************** ");
            welcomeSocket = new ServerSocket(port, 50, addr);

            System.out.println("++++++++++++++++ " + welcomeSocket.toString());
            System.out.println("++++++++++++++++ " + serviceEnabled);

            while(true && serviceEnabled)
            {

                //Listen for incoming connections on specified port
                //Block thread until someone connects
                socket = welcomeSocket.accept();

                //signalActivity("TCP Connection Established: " + socket.toString() + " Starting file transfer");

                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);

                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);
                String inputData = "";

                signalActivity("About to start handshake");
                System.out.println("About to start handshake");
                //Client-Server handshake

				/*
				String test = "Y";
				test = test + br.readLine() + test;


				signalActivity(test);
				 */

				/*
				inputData = br.readLine();

				if(!inputData.equals("wdft_client_hello"))
				{
					throw new IOException("Invalid WDFT protocol message");

				}

				pw.println("wdft_server_hello");


				inputData = br.readLine();


				if(inputData == null)
				{
					throw new IOException("File name was null");

				}


				fileName = inputData;

				pw.println("wdft_server_ready");

				*/

                //signalActivity("Handshake complete, getting file: " + fileName);

                String savedAs = "WDFL_File_" + System.currentTimeMillis();
                File file = new File(saveLocation, savedAs);

                byte[] buffer = new byte[4096];
                int bytesRead;

                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);

                while(true)
                {
                    bytesRead = is.read(buffer, 0, buffer.length);
                    if(bytesRead == -1)
                    {
                        break;
                    }
                    bos.write(buffer, 0, bytesRead);
                    bos.flush();

                }


			    /*
			    fos.close();
			    bos.close();

			    br.close();
			    isr.close();
			    is.close();

			    pw.close();
			    os.close();

			    socket.close();
			    */

                bos.close();
                socket.close();


                signalActivity("File Transfer Complete, saved as: " + savedAs);
                signalFinalActivity(savedAs);

                //Start writing to file

            }


        } catch (IOException e) {
            signalActivity("IOEXCEPTION");
        }
        catch(Exception e)
        {
            signalActivity("EXCEPTION");
        }

        //Signal that operation is complete
        serverResult.send(port, null);
    }


    public void signalActivity(String message)
    {
        Bundle b = new Bundle();
        b.putString("message", message);
        serverResult.send(port, b);
    }
    public void signalFinalActivity(String message)
    {
        Bundle b = new Bundle();
        b.putString("path_for_showcase", message);
        serverResult.send(port, b);
    }


    public void onDestroy()
    {
        serviceEnabled = false;

        //Signal that the service was stopped
        //serverResult.send(port, new Bundle());

        stopSelf();
    }

}