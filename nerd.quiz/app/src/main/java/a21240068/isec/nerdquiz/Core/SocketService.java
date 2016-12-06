package a21240068.isec.nerdquiz.Core;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by bernardovieira on 02-12-2016.
 */

public class SocketService extends Service
{
    ObjectOutputStream out;
    ObjectInputStream in;
    Socket socket;
    private NerdQuizApp application;

    @Override
    public IBinder onBind(Intent intent)
    {
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        return true;
    }
    @Override
    public void onRebind(Intent intent)
    {
        //
    }


    private final IBinder myBinder = new LocalBinder();

    public class LocalBinder extends Binder
    {
        public SocketService getService()
        {
            return SocketService.this;

        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        application = (NerdQuizApp)getApplication();

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    String serverIP = "192.168.1.4";
                    Integer serverPort = 5007;
                    InetAddress serverAddr;

                    try
                    {
                        Log.d("NerdQuizApp", "Connecting ...");
                        serverAddr = InetAddress.getByName(serverIP);
                        socket = new Socket(serverAddr, serverPort);
                        Log.d("NerdQuizApp", "Connected");
                    }
                    catch (Exception e)
                    {
                        socket = null;
                        Log.d("NerdQuizApp", "C: Error", e);
                    }

                    Log.d("SocketService","Opening Streams.");
                    in = new ObjectInputStream(socket.getInputStream());
                    out = new ObjectOutputStream(socket.getOutputStream());
                    Log.d("SocketService","Streams opened.");
                }
                catch (IOException e)
                {
                    Log.d("SocketService","Error when open streams.");
                }
            }
        }).start();
        Log.d("SocketService","Streams.");
    }

    public void sendCommandToServer(final String command)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    out.writeObject(command);
                    out.flush();
                }
                catch (IOException e)
                {
                    Log.d("sendCommandToServer","Error writing to server.");
                }
            }
        }).start();
    }

    public Object waitMessageFromServer() throws IOException, ClassNotFoundException {
        return in.readObject();
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }


}