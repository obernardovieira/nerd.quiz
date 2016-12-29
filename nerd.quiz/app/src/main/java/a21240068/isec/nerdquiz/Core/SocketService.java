package a21240068.isec.nerdquiz.Core;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by bernardovieira on 02-12-2016.
 */

public class SocketService extends Service
{
    public static final String SERVERIP = "192.168.10.7"; //your computer IP address should be written here
    public static final int SERVERPORT = 5007;
    //PrintWriter out;
    ObjectOutputStream out;
    ObjectInputStream in;
    //
    public Socket socket;
    InetAddress serverAddr;
    private Context context;

    @Override
    public IBinder onBind(Intent intent)
    {
        //
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        //
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
        new Thread(new connectSocket()).start();
    }

    public ObjectInputStream getObjectStreamIn()
    {
        //
        return in;
    }

    public InputStream getStreamIn() throws IOException
    {
        return socket.getInputStream();
    }

    public ObjectOutputStream getObjectStreamOut()
    {
        //
        return out;
    }

    public OutputStream getStreamOut() throws IOException
    {
        return socket.getOutputStream();
    }

    public void sendMessage(final Object object)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if(out == null)
                {
                    return;
                }
                try
                {
                    out.writeObject(object);
                    out.flush();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public void IsBoundable(Context context){
        Toast.makeText(context,"I bind like butter", Toast.LENGTH_LONG).show();
    }

    public void setContext(Context context)
    {
        this.context = context;
        if(socket == null)
        {
            errorConnection();
        }
    }

    public boolean isConnected()
    {
        //
        return (socket != null);
    }

    public void errorConnection()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Probably you lost your internet connection!")
                .setTitle("Server not found")
                .setPositiveButton("Reconnect", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(final DialogInterface dialog, int which)
                    {
                        if(!isConnected())
                        {
                            new Thread(new connectSocket()).start();
                        }
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    class connectSocket implements Runnable
    {
        @Override
        public void run()
        {
            try
            {


                //here you must put your computer's IP address.
                Log.d("TCP Client", "C: Connecting");
                serverAddr = InetAddress.getByName(SERVERIP);
                Log.d("TCP Client", "C: Connecting...");
                //create a socket to make the connection with the server

                socket = new Socket(serverAddr, SERVERPORT);



                try {


                    //send the message to the server
                    //out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    Log.d("TCP Client", "Opening streams.");
                    out = new ObjectOutputStream(socket.getOutputStream());
                    in = new ObjectInputStream(socket.getInputStream());
                    //out.writeObject("Ola");

                    Log.d("TCP Client", "C: Sent.");

                    Log.d("TCP Client", "C: Done.");

                }
                catch (Exception e) {

                    Log.d("TCP", "S: Error", e);

                }
            } catch (Exception e) {


                Log.d("TCP", "C: Error", e);
                socket = null;
            }

        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            out.close();
            socket.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        socket = null;
    }


}