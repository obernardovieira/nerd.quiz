package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

import a21240068.isec.nerdquiz.Core.NerdQuizApp;
import a21240068.isec.nerdquiz.Core.Response;
import a21240068.isec.nerdquiz.Core.SocketService;

public class RegisterActivity extends Activity {

    private static final int PICK_IMAGE = 1;
    private Uri selectedImageUri;
    NerdQuizApp nerdQuizApp;
    private boolean mIsBound;
    private SocketService mBoundService;
    private Handler handler;

    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            // TODO Auto-generated method stub
            Log.d("RegisterActivity", "bonServiceConnected");
            mBoundService = ((SocketService.LocalBinder)service).getService();
            Log.d("RegisterActivity", "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            // TODO Auto-generated method stub
            mBoundService = null;
            Log.d("RegisterActivity", "onServiceDisconnected");
        }

    };


    private void doBindService()
    {
        bindService(new Intent(RegisterActivity.this, SocketService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        Log.d("RegisterActivity", "doBindService " + String.valueOf((mBoundService == null)));
        if(mBoundService != null)
        {
            Log.d("RegisterActivity", "--------boundable-----------");
        }
        Log.d("RegisterActivity", "doBindService");
    }


    private void doUnbindService()
    {
        if (mIsBound)
        {
            unbindService(mConnection);
            mIsBound = false;
        }
        Log.d("RegisterActivity", "doUnbindService");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        selectedImageUri = null;
        nerdQuizApp = (NerdQuizApp)getApplication();
        handler = new Handler();

        startService(new Intent(RegisterActivity.this,SocketService.class));
    }

    @Override
    public void onResume() {
        super.onResume();


        doBindService();
        //answer_from_server.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        doUnbindService();
        //answer_from_server.interrupt();
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
        mBoundService.sendCommandToServer("ola-ola");
    }

    public void registerOnServer(String username, String password, String profile_pic)
    {
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

    private Thread answer_from_server = new Thread(new Runnable() {
        @Override
        public void run()
        {
            try
            {
                Integer response;
                while(true)
                {
                    response = (Integer)mBoundService.waitMessageFromServer();
                    if(response == Response.OK)
                    {
                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Toast.makeText(
                                        RegisterActivity.this,
                                        "You are successfully registered! Login now!",
                                        Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(RegisterActivity.this,
                                        AuthenticationActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                    else if(response == Response.ERROR)
                    {
                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Toast.makeText(
                                        RegisterActivity.this,
                                        "An error occurred!",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                }
            }
            catch (IOException e)
            {
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(
                                RegisterActivity.this,
                                "You are offline! Check your connection.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    });



}
