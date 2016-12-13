package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
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

    private static final int PICK_IMAGE = 1;
    private Uri selectedImageUri;
    NerdQuizApp nerdQuizApp;

    private boolean mIsBound;
    private SocketService mBoundService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        selectedImageUri = Uri.fromFile(new File("/mnt/sdcard/bluetooth/IMG_20160902_224532.JPG"));
        ((ImageView) findViewById(R.id.iv_profile_pic)).setImageURI(selectedImageUri);
        nerdQuizApp = (NerdQuizApp)getApplication();
    }

    public void changeProfilePhoto(View view)
    {
        Intent gintent = new Intent();
        gintent.setType("image/*");
        gintent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(gintent, "Select Picture"), PICK_IMAGE);
    }

    public void clickRegisterButton(View view)
    {
        EditText et_username = (EditText)findViewById(R.id.et_username);
        EditText et_password = (EditText)findViewById(R.id.et_password);

        if (selectedImageUri != null)
        {
            registerOnServer(et_username.getText().toString(), et_password.getText().toString());
        }
    }

    public void registerOnServer(String username, String password)
    {
        Log.d("fick", "me");
        uploadPhoto();
    }

    public void uploadPhoto()
    {

        new Thread(new Runnable() {
            @Override
            public void run() {



                    /*int count;
                    byte[] buffer = new byte[1024];

                    OutputStream out = mBoundService.socket.getOutputStream();
                    InputStream in = new FileInputStream(new File("/mnt/sdcard/bluetooth/IMG_20160902_224532.JPG"));
                    //getContentResolver().openInputStream(selectedImageUri);
                    //BufferedInputStream in = new BufferedInputStream(new FileInputStream(myFile));

                    Log.d("uploadPhoto","uploading");

                    ObjectOutputStream oout = new ObjectOutputStream(out);
                    oout.writeObject(Command.PROFILE_PIC_UP);
                    oout.flush();

                    Log.d("uploadPhoto","uploading");
                    while ((count = in.read(buffer)) > 0) {
                        out.write(buffer, 0, count);
                        out.flush();
                    }
                    Log.d("uploadPhoto","uploaded");*/


                    mBoundService.sendFile("/mnt/sdcard/bluetooth/IMG_20160902_224532.JPG");




            }
        }).start();
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
