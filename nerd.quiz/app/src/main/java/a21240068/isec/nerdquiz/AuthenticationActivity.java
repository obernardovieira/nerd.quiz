package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import a21240068.isec.nerdquiz.Core.NerdQuizApp;
import a21240068.isec.nerdquiz.Core.Response;
import a21240068.isec.nerdquiz.Core.SocketService;

public class AuthenticationActivity extends Activity {

    private boolean mIsBound;
    private SocketService mBoundService;
    private ReceiveFromServerTask task;

    Handler handler;
    String profile_pic_file_name;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        handler = new Handler();
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
            Toast.makeText(this, getString(R.string.needs_username), Toast.LENGTH_LONG).show();
            return;
        }
        else if(et_password.length() == 0)
        {
            Toast.makeText(this, getString(R.string.needs_password), Toast.LENGTH_LONG).show();
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
            String response = "";
            Object object;
            try
            {
                while(!isCancelled())
                {
                    if(mBoundService.socket.getInputStream().available() < 4)
                    {
                        continue;
                    }
                    ObjectInputStream in = mBoundService.getObjectStreamIn();
                    object = in.readObject();
                    if(object instanceof String)
                    {
                        response = (String)object;
                        if(response.startsWith(getResources().getString(R.string.command_profilepdown)))
                        {
                            response = downloadProfilePicResult();
                        }
                    }
                    break;
                }
            }
            catch (IOException | ClassNotFoundException ignored) { }
            return response;
        }

        protected void onPostExecute(String result)
        {
            String [] pms = result.split(" ");
            if(pms[0].equals(getResources().getString(R.string.command_login)))
            {
                loginResult(pms[1]);
            }
            else if(pms[0].equals(getResources().getString(R.string.command_profilepdown)))
            {
                if(pms[1].equals(getResources().getString(R.string.response_ok)))
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
        }

        @Override
        protected void onCancelled() { }
    }

    private void loginResult(String result)
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
            Toast.makeText(AuthenticationActivity.this, getString(R.string.logged_in), Toast.LENGTH_LONG).show();

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
                            mBoundService.sendMessage(
                                    getResources().getString(R.string.command_profilepdown) +
                                            " " + profile_pic_file_name);
                            handler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    task = new ReceiveFromServerTask();
                                    task.execute();
                                }
                            });
                        }

                    }
                    catch (IOException | ClassNotFoundException e)
                    {
                        mBoundService.errorConnection();
                    }
                }
            }).start();

        }
        else if(response.equals(Response.NOT_REGISTERED))
        {
            Toast.makeText(AuthenticationActivity.this, getString(R.string.not_registered_adv),
                    Toast.LENGTH_LONG).show();
        }
        else if(response.equals(Response.ERROR))
        {
            Toast.makeText(AuthenticationActivity.this, getString(R.string.error_when_login),
                    Toast.LENGTH_LONG).show();
        }
    }

    public String downloadProfilePicResult()
            throws IOException, ClassNotFoundException
    {
        String response;
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

        response = getResources().getString(R.string.command_profilepdown) + " " +
            getResources().getString(R.string.response_ok);
        return response;
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
        }
    }
}
