package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;

import a21240068.isec.nerdquiz.Core.Command;
import a21240068.isec.nerdquiz.Core.NerdQuizApp;
import a21240068.isec.nerdquiz.Core.Response;
import a21240068.isec.nerdquiz.Core.SocketService;

public class RegisterActivity extends Activity {

    private static final int PICK_IMAGE = 1;
    private Uri selectedImageUri;
    NerdQuizApp nerdQuizApp;

    private boolean mIsBound;
    private SocketService mBoundService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        selectedImageUri = null;
        nerdQuizApp = (NerdQuizApp)getApplication();


        mBoundService = null;
    }

    public void changeProfilePhoto(View view)
    {
        try
        {
            Intent gintent = new Intent();
            gintent.setType("image/*");
            gintent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(gintent, "Select Picture"), PICK_IMAGE);
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(e.getClass().getName(), e.getMessage(), e);
        }
    }

    public void clickRegisterButton(View view)
    {
        EditText et_username = (EditText)findViewById(R.id.et_username);
        EditText et_password = (EditText)findViewById(R.id.et_password);

        registerOnServer(et_username.getText().toString(), et_password.getText().toString());
    }

    public void registerOnServer(String username, String password)
    {
        mBoundService.sendMessage(Command.REGISTER + " " + username + " " + password);
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
            case PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK)
                {
                    ImageView iv_profile_pic = (ImageView) findViewById(R.id.iv_profile_pic);
                    selectedImageUri = data.getData();
                    iv_profile_pic.setImageURI(selectedImageUri);
                    Log.d("Uri", data.getData().toString());
                }
                break;
        }
    }

    private class ReceiveFromServerTask extends AsyncTask<Void, Void, String>
    {
        protected String doInBackground(Void... params)
        {
            Integer response = Response.ERROR;
            try
            {
                ObjectInputStream in;
                in = new ObjectInputStream(mBoundService.socket.getInputStream());
                while(!isCancelled())
                {
                    if(in.available() > 0)
                    {
                        response = (Integer)in.readObject();
                        break;
                    }
                }
                in.close();
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
                Toast.makeText(RegisterActivity.this, "You are registered now!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(RegisterActivity.this, AuthenticationActivity.class);
                startActivity(intent);
                finish();
            }
            else if(response == Response.ERROR)
            {
                Toast.makeText(RegisterActivity.this, "An error occurred while registering!", Toast.LENGTH_LONG).show();
            }
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
                new ReceiveFromServerTask().execute();
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
