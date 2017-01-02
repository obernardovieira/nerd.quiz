package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import a21240068.isec.nerdquiz.Core.NerdQuizApp;
import a21240068.isec.nerdquiz.Core.Response;
import a21240068.isec.nerdquiz.Core.SocketService;

public class RegisterActivity extends Activity
{
    private final int TAKE_NEW_PHOTO = 0;
    private boolean mIsBound;
    private SocketService mBoundService;
    private Uri selectedImageUri;
    private ReceiveFromServerTask task;
    NerdQuizApp nerdQuizApp;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nerdQuizApp = (NerdQuizApp)getApplication();
        selectedImageUri = null;
    }

    public void changeProfilePhoto(View view)
    {
        startActivityForResult(new Intent(this, TakePhotoActivity.class), TAKE_NEW_PHOTO);
    }

    public void clickRegisterButton(View view)
    {

        if(!mBoundService.isConnected())
        {
            mBoundService.errorConnection();
            return;
        }

        EditText et_username = (EditText)findViewById(R.id.et_username);
        EditText et_password = (EditText)findViewById(R.id.et_password);

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
        else if (selectedImageUri == null)
        {
            Toast.makeText(this, "Needs image!", Toast.LENGTH_LONG).show();
            return;
        }
        registerOnServer(et_username.getText().toString(), et_password.getText().toString());
    }

    public void registerOnServer(final String username, final String password)
    {
        mBoundService.sendMessage(getResources().getString(R.string.command_register) +
                " " + username + " " + password);
        task = new ReceiveFromServerTask();
        task.execute();
    }

    public void clickRegisteredText(View view)
    {
        Intent intent = new Intent(this, AuthenticationActivity.class);
        startActivity(intent);

        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case TAKE_NEW_PHOTO:
                if (resultCode == Activity.RESULT_OK)
                {
                    selectedImageUri = Uri.fromFile(new File(getApplicationContext().getFilesDir(),
                            getResources().getString(R.string.default_profile_pic_name)));
                    ((ImageView) findViewById(R.id.iv_profile_pic)).setImageURI(selectedImageUri);
                }
                break;
        }
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

    private class ReceiveFromServerTask extends AsyncTask<Void, Void, String>
    {
        protected String doInBackground(Void... params)
        {
            String response = getResources().getString(R.string.response_error);
            try
            {
                while(!isCancelled())
                {
                    if(mBoundService.socket.getInputStream().available() < 4)
                    {
                        continue;
                    }
                    ObjectInputStream ins = mBoundService.getObjectStreamIn();
                    Integer tq = (Integer)ins.readObject();
                    if(tq.equals(Response.REGISTERED))
                    {
                        response = getResources().getString(R.string.response_registered);
                        break;
                    }
                    else if(tq.equals(Response.ERROR))
                    {
                        break;
                    }
                    ObjectOutputStream out = mBoundService.getObjectStreamOut();
                    if(out == null)
                    {
                        break;
                    }
                    try
                    {
                        InputStream in = new FileInputStream(
                                new File(getApplicationContext().getFilesDir(),
                                        getResources().getString(R.string.default_profile_pic_name)));
                        out.writeObject(in.available());
                        out.flush();
                        OutputStream outs = mBoundService.getStreamOut();

                        byte[] buf = new byte[getResources().getInteger(R.integer.bytes_on_photo)];
                        int len = 0;
                        while ((len = in.read(buf)) != -1)
                        {
                            outs.write(buf, 0, len);
                            outs.flush();
                        }

                        in.close();

                        String file_name = (String)ins.readObject();
                        File own_file = new File(getApplicationContext().getFilesDir(),
                                getResources().getString(R.string.default_profile_pic_name));
                        own_file.renameTo(new File(getApplicationContext().getFilesDir(), file_name));

                        SharedPreferences preferences = PreferenceManager.
                                getDefaultSharedPreferences(RegisterActivity.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(getString(R.string.profile_pic), file_name);
                        editor.apply();

                        response = getResources().getString(R.string.response_ok);
                        break;
                    }
                    catch(IOException | ClassNotFoundException ignored) { }
                    break;
                }
            }
            catch (IOException | ClassNotFoundException ignored) { }
            return response;
        }

        protected void onPostExecute(String result)
        {
            if(result.equals(getResources().getString(R.string.response_ok)))
            {
                Intent intent = new Intent(RegisterActivity.this, AuthenticationActivity.class);
                startActivity(intent);

                finish();
            }
            else if(result.equals(getResources().getString(R.string.response_registered)))
            {
                Toast.makeText(RegisterActivity.this, "Already registered!",
                        Toast.LENGTH_LONG).show();
            }
            else
            {
                mBoundService.errorConnection();
            }
        }

        public void onCancelled()
        { }

    }

    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mBoundService = ((SocketService.LocalBinder)service).getService();
            mBoundService.setContext(RegisterActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mBoundService = null;
        }

    };

    private void doBindService()
    {
        bindService(new Intent(RegisterActivity.this, SocketService.class),
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
