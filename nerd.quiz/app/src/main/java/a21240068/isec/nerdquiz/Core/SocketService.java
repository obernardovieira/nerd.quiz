package a21240068.isec.nerdquiz.Core;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
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
    public static final String SERVERIP = "192.168.1.7"; //your computer IP address should be written here
    public static final int SERVERPORT = 5007;
    //PrintWriter out;
    ObjectOutputStream out;
    ObjectInputStream in;
    //
    public Socket socket;
    InetAddress serverAddr;
    private Context context;
    private Handler handler;

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
        Log.d("sendit","wadesfrgdthfy");
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if(out == null)
                {
                    errorConnection();
                    return;
                }
                try
                {
                    Log.d("sendit","wadesfrgdthfy");
                    out.writeObject(object);
                    out.flush();
                }
                catch(IOException e)
                {
                    errorConnection();
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

    public void setContext(Context context)
    {
        this.context = context;
        this.handler = new Handler(context.getMainLooper());
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
        handler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Probably you lost your internet connection!")
                        .setTitle("Server not found")
                        .setPositiveButton("Reconnect", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(final DialogInterface dialog, int which)
                            {
                                if(!isConnected())
                                {
                                    Toast.makeText(context, "Reconnecting ...", Toast.LENGTH_LONG).show();
                                    new Thread(new connectSocket()).start();
                                    new Thread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            try
                                            {
                                                Thread.sleep(2000);
                                            }
                                            catch (InterruptedException ignored) { }
                                            if(!isConnected())
                                            {
                                                handler.post(new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        errorConnection();
                                                    }
                                                });
                                            }
                                        }
                                    }).start();
                                }
                                dialog.dismiss();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    class connectSocket implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                serverAddr = InetAddress.getByName(SERVERIP);
                socket = new Socket(serverAddr, SERVERPORT);
                try
                {
                    out = new ObjectOutputStream(socket.getOutputStream());
                    in = new ObjectInputStream(socket.getInputStream());
                }
                catch (Exception ignored)
                {
                    socket = null;
                }
            }
            catch (Exception e)
            {
                socket = null;
            }
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        try
        {
            out.close();
            socket.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        socket = null;
    }
}