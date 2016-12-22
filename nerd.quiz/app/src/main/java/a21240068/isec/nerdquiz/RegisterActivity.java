package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import a21240068.isec.nerdquiz.Core.Command;
import a21240068.isec.nerdquiz.Core.NerdQuizApp;
import a21240068.isec.nerdquiz.Core.Response;
import a21240068.isec.nerdquiz.Core.SocketService;

public class RegisterActivity extends Activity {

    private final int TAKE_NEW_PHOTO = 0;

    private Uri selectedImageUri;
    NerdQuizApp nerdQuizApp;

    private boolean mIsBound;
    private SocketService mBoundService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        Log.d("fick", "me");

        mBoundService.sendMessage(Command.REGISTER + " " + username + " " + password);

        new ReceiveFromServerTask().execute();
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
                    selectedImageUri = Uri.fromFile(new File(getApplicationContext().getFilesDir(), "picture.jpg"));
                    ((ImageView) findViewById(R.id.iv_profile_pic)).setImageURI(selectedImageUri);
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


        doBindService();
    }

    @Override
    protected void onPause() {
        super.onPause();

        doUnbindService();
    }

    private class ReceiveFromServerTask extends AsyncTask<Void, Void, String>
    {
        protected String doInBackground(Void... params)
        {
            String response = "";
            Log.d("doInBackground(DBA)", "started");
            try
            {
                while(!isCancelled())
                {
                    //Log.d("ReceiveFromServerTask", String.valueOf(mBoundService.socket.getInputStream().available()));
                    if(mBoundService.socket.getInputStream().available() > 4)
                    {
                        ObjectInputStream ins = mBoundService.getObjectStreamIn();
                        //in = new ObjectInputStream(mBoundService.socket.getInputStream());

                        Integer tq = (Integer)ins.readObject();

                        if(tq.equals(Response.OK))
                        {
                            ObjectOutputStream out = mBoundService.getObjectStreamOut();
                            if(out != null)
                            {

                                try {

                                    /*Log.d("uploadPhoto","uploading");
                                    out.writeObject(Command.PROFILE_PIC_UP);
                                    out.flush();*/
                                    Log.d("uploadPhoto","uploading");

                                    InputStream in = new FileInputStream(
                                            new File(getApplicationContext().getFilesDir(), "picture.jpg"));
                                    out.writeObject(in.available());
                                    out.flush();
                                    OutputStream outs = mBoundService.getStreamOut();

                                    byte[] buf = new byte[8192];
                                    int len = 0;
                                    while ((len = in.read(buf)) != -1) {
                                        outs.write(buf, 0, len);
                                        outs.flush();
                                    }

                                    in.close();
                                    //out.close();

                                    String file_name = (String)ins.readObject();
                                    File own_file = new File(getApplicationContext().getFilesDir(), "picture.jpg");
                                    own_file.renameTo(new File(getApplicationContext().getFilesDir(), file_name));

                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(RegisterActivity.this);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString(getString(R.string.profile_pic), file_name);
                                    editor.apply();

                                    Log.d("uploadPhoto","uploaded");
                                    response = "OK";
                                    break;
                                }
                                catch(IOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            else
                            {
                                Log.d("sendMessage","Erro");
                            }


                        }
                        else
                        {
                            throw new IOException("Response.ERROR");
                        }


                        //in.close();
                        break;
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Log.d("ReceiveFromServerTask","b");
            return response;
        }

        protected void onPostExecute(String result)
        {
            Log.d("onPostExecute(DBA)",result);

            if(result.equals("OK"))
            {
                Intent intent = new Intent(RegisterActivity.this, AuthenticationActivity.class);
                startActivity(intent);

                finish();
            }
            else
            {
                //error uploading photo
            }
        }

        public void onCancelled()
        {
            Log.d("fuckingStop(DBA)", "Cancelled.");
        }

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
        bindService(new Intent(RegisterActivity.this, SocketService.class), mConnection, Context.BIND_AUTO_CREATE);
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
