package a21240068.isec.nerdquiz;

import android.app.Application;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by bernardovieira on 21-11-2016.
 */

public class Connection extends Application {

    public String serverIP;
    public Integer serverPort;
    public Socket socketToServer;
    public Socket socketForClientServer;
    public Socket socketForClientClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("onCreate", "Create connection class!");
    }

    public void connectToServer()
    {
        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.d("connectToServer", "Connecting to the server  " + serverIP);
                    socketToServer = new Socket(serverIP, serverPort);
                }
                catch (Exception e)
                {
                    socketToServer = null;
                    Log.d("connectToServer", "An error occurred!");
                }
                listenToServer.start();
            }
        });
        t.start();
    }

    public void disconnectFromServer()
    {
        try
        {
            listenToServer.interrupt();
            if (socketToServer != null)
            {
                socketToServer.close();
            }
        }
        catch (Exception e)
        {
        }
        socketToServer = null;
    }

    public void sendMessageToServer(String message)
    {
        try
        {
            OutputStream oStream = socketToServer.getOutputStream();
            ObjectOutputStream ooStream = new ObjectOutputStream(oStream);

            ooStream.writeObject(message);
            Log.d("sendMessageToServer", "Message sent '" + message+ "'");
        }
        catch (IOException e)
        {
            Log.d("sendMessageToServer", "Error sending message!");
            e.printStackTrace();
        }
    }

    Thread listenToServer = new Thread(new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                OutputStream oStream = socketToServer.getOutputStream();
                InputStream iStream = socketToServer.getInputStream();
                ObjectOutputStream ooStream = new ObjectOutputStream(oStream);
                ObjectInputStream oiStream = new ObjectInputStream(iStream);

                while (!Thread.currentThread().isInterrupted())
                {
                    String read = (String) oiStream.readObject();
                    Log.d("RPS", "Received: " + read);
                }
            }
            catch (Exception e)
            {
            }
        }
    });
}