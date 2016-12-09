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
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by bernardovieira on 02-12-2016.
 */

public class SocketService extends Service {
    public static final String SERVERIP = "192.168.1.4"; //your computer IP address should be written here
    public static final int SERVERPORT = 5007;
    //PrintWriter out;
    ObjectOutputStream out;
    //ObjectInputStream in;
    public Socket socket;
    InetAddress serverAddr;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        Log.d("SocketService", "I am in Ibinder onBind method");
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        Log.d("SocketService", "onUnbind");
        return true;
    }
    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
        Log.d("SocketService", "onRebind");
    }


    private final IBinder myBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public SocketService getService() {
            Log.d("SocketService", "I am in Localbinder ");
            return SocketService.this;

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SocketService", "I am in on create");
        //in = null;
        Runnable connect = new connectSocket();
        new Thread(connect).start();
    }

    public void IsBoundable(Context context){
        Toast.makeText(context,"I bind like butter", Toast.LENGTH_LONG).show();
    }

    public void sendMessage(final Object object) {
        /*if (out != null && !out.checkError()) {
            Log.d("SocketService", "in sendMessage"+message);
            out.println(message);
            out.flush();
        }*/
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(out != null)
                {

                    try {
                        out.writeObject(object);
                        out.flush();
                        Log.d("sendMessage","ENVIADO!");
                        if(object instanceof String)
                            Log.d("sendMessage", (String) object);
                    }
                    catch(StreamCorruptedException e)
                    {
                        Log.d("erro", "StreamCorruptedException");
                        e.printStackTrace();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    Log.d("sendMessage","Erro");
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        Log.d("SocketService", "I am in on start");
        //  Toast.makeText(this,"Service created ...", Toast.LENGTH_LONG).show();
        /*Runnable connect = new connectSocket();
        new Thread(connect).start();*/
        return START_STICKY;
    }


    class connectSocket implements Runnable {

        @Override
        public void run() {


            try {
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
                    //in = new ObjectInputStream(socket.getInputStream());
                    //out.writeObject("Ola");

                    Log.d("TCP Client", "C: Sent.");

                    Log.d("TCP Client", "C: Done.");


                }
                catch (Exception e) {

                    Log.d("TCP", "S: Error", e);

                }
            } catch (Exception e) {

                Log.d("TCP", "C: Error", e);

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