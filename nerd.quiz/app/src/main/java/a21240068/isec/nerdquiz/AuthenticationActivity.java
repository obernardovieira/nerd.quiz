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
import a21240068.isec.nerdquiz.Core.NerdQuizApp;
import a21240068.isec.nerdquiz.Core.Response;
import a21240068.isec.nerdquiz.Core.SocketService;

public class AuthenticationActivity extends Activity {

    private boolean mIsBound;
    private SocketService mBoundService;
    private ReceiveFromServerTask task;
    private ReceiveProfilePhotoServerTask photo_task;

    String profile_pic_file_name;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
    }

    public void clickLoginButton(View view)
    {
        if(!mBoundService.isConnected())
        {
            mBoundService.errorConnection();
            return;
        }

        TextView et_username = (TextView) findViewById(R.id.et_username);
        TextView et_password = (TextView) findViewById(R.id.et_password);

        if(et_username.length() == 0)
        {
            Toast.makeText(this, "Needs username!", Toast.LENGTH_LONG).show();
            return;
        }
        else if(et_password.length() == 0)
        {
            Toast.makeText(this, "Needs password!", Toast.LENGTH_LONG).show();
            return;
        }

        String username = et_username.getText().toString();
        String password = et_password.getText().toString();

        mBoundService.sendMessage(getResources().getString(R.string.command_login) +
                " " + username + " " + password);

        task = new ReceiveFromServerTask();
        task.execute();
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
                    if(mBoundService.socket.getInputStream().available() < 4)
                    {
                        continue;
                    }
                    ObjectInputStream in = mBoundService.getObjectStreamIn();
                    response = (Integer)in.readObject();
                    break;
                }
            }
            catch (IOException | ClassNotFoundException ignored) { }
            return response.toString();
        }

        protected void onPostExecute(String result)
        {
            Integer response = Integer.parseInt(result);
            if(response.equals(Response.OK))
            {
                TextView tv_username = (TextView) findViewById(R.id.et_username);
                final String username = tv_username.getText().toString();

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AuthenticationActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.user_name), username);
                editor.apply();

                NerdQuizApp application = (NerdQuizApp)getApplication();
                application.setUsername(username);
                //
                Toast.makeText(AuthenticationActivity.this, "You are logged now!", Toast.LENGTH_LONG).show();

                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ObjectInputStream in;
                        in = mBoundService.getObjectStreamIn();

                        try
                        {
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
                                photo_task = new ReceiveProfilePhotoServerTask();
                                photo_task.execute();
                            }

                        }
                        catch (IOException | ClassNotFoundException e)
                        {
                            mBoundService.errorConnection();
                        }
                    }
                }).start();

            }
            else if(response.equals(Response.ERROR))
            {
                Toast.makeText(AuthenticationActivity.this, "An error occurred while login!", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() { }
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
                    if(mBoundService.socket.getInputStream().available() < 4)
                    {
                        continue;
                    }
                    ObjectInputStream oiStream = mBoundService.getObjectStreamIn();
                    Integer size = (Integer)oiStream.readObject();
                    Integer received = 0;
                    BufferedInputStream in = new BufferedInputStream(mBoundService.getStreamIn());

                    OutputStream out = new FileOutputStream(
                            new File(getApplicationContext().getFilesDir(), profile_pic_file_name));

                    byte[] buf = new byte[getResources().getInteger(R.integer.bytes_on_photo)];
                    int len = 0;
                    while ((len = in.read(buf)) != -1)
                    {
                        out.write(buf, 0, len);
                        if(received + len == size)
                            break;
                        else
                            received += len;
                    }
                    out.close();

                    SharedPreferences preferences = PreferenceManager.
                            getDefaultSharedPreferences(AuthenticationActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(getString(R.string.profile_pic), profile_pic_file_name);
                    editor.apply();

                    response = "OK";
                    break;
                }
            }
            catch (IOException | ClassNotFoundException ignored) { }
            return response;
        }

        protected void onPostExecute(String result)
        {
            if(result.equals("OK"))
            {
                Intent intent = new Intent(AuthenticationActivity.this, DashboardActivity.class);
                startActivity(intent);

                finish();
            }
            else
            {
                mBoundService.errorConnection();
            }
        }

        @Override
        protected void onCancelled()
        { }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        doBindService();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        doUnbindService();
    }

    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mBoundService = ((SocketService.LocalBinder)service).getService();
            mBoundService.setContext(AuthenticationActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mBoundService = null;
        }
    };

    private void doBindService()
    {
        bindService(new Intent(AuthenticationActivity.this, SocketService.class),
                mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService()
    {
        if (mIsBound)
        {
            unbindService(mConnection);
            mIsBound = false;
            if(task != null)
            {
                task.cancel(true);
            }
            if(photo_task != null)
            {
                photo_task.cancel(true);
            }
        }
    }
}
