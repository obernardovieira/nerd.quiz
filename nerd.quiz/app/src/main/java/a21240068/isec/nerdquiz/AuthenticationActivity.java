package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import a21240068.isec.nerdquiz.Core.Command;
import a21240068.isec.nerdquiz.Core.Response;
import a21240068.isec.nerdquiz.Core.SocketService;

public class AuthenticationActivity extends Activity {

    private boolean mIsBound;
    private SocketService mBoundService;

    String profile_pic_file_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
    }

    public void clickLoginButton(View view)
    {
        TextView tv_username = (TextView) findViewById(R.id.et_username);
        TextView tv_password = (TextView) findViewById(R.id.et_password);

        String username = tv_username.getText().toString();
        String password = tv_password.getText().toString();

        //

        mBoundService.sendMessage(Command.LOGIN + " " + username + " " + password);

        new ReceiveFromServerTask().execute();
    }

    public void clickNotRegisteredText(View view)
    {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);

        finish();
    }

    private class ReceiveFromServerTask extends AsyncTask<Void, Void, String>
    {
        protected String doInBackground(Void... params)
        {
            Integer response = Response.ERROR;

            try
            {
                while(!isCancelled())
                {
                    if(mBoundService.socket.getInputStream().available() > 4)
                    {
                        Object obj;
                        ObjectInputStream in;
                        in = mBoundService.getObjectStreamIn();
                        obj = in.readObject();

                        if(obj instanceof String)
                            Log.d("sdfghj", (String)obj);
                        else if(obj instanceof Integer)
                            response = (Integer)obj;
                        //in.close();
                        break;
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
            return response.toString();
        }

        protected void onPostExecute(String result) {
            Integer response = Integer.parseInt(result);
            Log.d("onPostExecute",result);
            if(response == Response.OK)
            {
                TextView tv_username = (TextView) findViewById(R.id.et_username);
                final String username = tv_username.getText().toString();

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AuthenticationActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.user_name), username);
                editor.apply();

                Toast.makeText(AuthenticationActivity.this, "You are logged now!", Toast.LENGTH_LONG).show();

                //receber nome da foto de perfil
                //verificar se existe
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ObjectInputStream in;
                        in = mBoundService.getObjectStreamIn();

                        try {
                            profile_pic_file_name = (String)in.readObject();

                            File profile_pic = new File(getApplicationContext().getFilesDir(), profile_pic_file_name);
                            if(profile_pic.exists())
                            {
                                Intent intent = new Intent(AuthenticationActivity.this, DashboardActivity.class);
                                startActivity(intent);

                                finish();
                            }
                            else
                            {
                                mBoundService.sendMessage(Command.PROFILE_PIC_DOWN + " " + profile_pic_file_name);
                                new ReceiveProfilePhotoServerTask().execute();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
            else if(response == Response.ERROR)
            {
                Toast.makeText(AuthenticationActivity.this, "An error occurred while login!", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            Log.i("AsyncTask", "Cancelled.");
        }
    }

    private class ReceiveProfilePhotoServerTask extends AsyncTask<Void, Void, String>
    {
        protected String doInBackground(Void... params)
        {
            String response = "ERROR";

            try
            {
                while(!isCancelled())
                {
                    if(mBoundService.socket.getInputStream().available() > 4)
                    {
                        ObjectInputStream oiStream;
                        oiStream = mBoundService.getObjectStreamIn();


                        Log.d("ppp", "abc");
                        Integer size = (Integer)oiStream.readObject();
                        Integer received = 0;
                        BufferedInputStream in = new BufferedInputStream(mBoundService.getStreamIn());

                        OutputStream out = new FileOutputStream(
                                new File(getApplicationContext().getFilesDir(), profile_pic_file_name));
                        Log.d("receiving file", "abc");

                        byte[] buf = new byte[8192];
                        int len = 0;
                        while ((len = in.read(buf)) != -1) {
                            out.write(buf, 0, len);
                            if(received + len == size)
                                break;
                            else
                                received += len;
                        }

                        Log.d("received", "abc");
                        out.close();

                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AuthenticationActivity.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(getString(R.string.profile_pic), profile_pic_file_name);
                        editor.apply();

                        response = "OK";
                        //in.close();
                        break;
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
            return response;
        }

        protected void onPostExecute(String result) {
            Log.d("onPostExecute",result);

            if(result.equals("OK"))
            {
                Intent intent = new Intent(AuthenticationActivity.this, DashboardActivity.class);
                startActivity(intent);

                finish();
            }
            else
            {
                //error downloading photo
            }
        }

        @Override
        protected void onCancelled() {
            Log.i("AsyncTask", "Cancelled.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


        doBindService();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(mBoundService == null)
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                while(mBoundService.socket == null)
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        doUnbindService();
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        //EDITED PART
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            mBoundService = ((SocketService.LocalBinder)service).getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            mBoundService = null;
        }

    };


    private void doBindService() {
        bindService(new Intent(AuthenticationActivity.this, SocketService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        if(mBoundService!=null){
            mBoundService.IsBoundable(this);
        }
        Log.d("SocketService", "doBindService");
    }


    private void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
        Log.d("SocketService", "doUnbindService");
    }

}
