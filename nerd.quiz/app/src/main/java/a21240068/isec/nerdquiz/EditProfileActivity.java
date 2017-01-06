package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import a21240068.isec.nerdquiz.Core.Response;
import a21240068.isec.nerdquiz.Core.SocketService;

public class EditProfileActivity extends Activity
{
    private final int TAKE_NEW_PHOTO = 0;
    private Uri selectedImageUri;

    private boolean mIsBound;
    private SocketService mBoundService;
    private ReceiveFromServerTask task;

    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String file_profile_pic = preferences.getString(getString(R.string.profile_pic), "");
        String defaultValue = getResources().getString(R.string.no_user_name_default);
        username = preferences.getString(getString(R.string.user_name), defaultValue);

        ImageView iv_profile_pic = (ImageView) findViewById(R.id.iv_profile_pic);
        File imgFile = new File(getApplicationContext().getFilesDir(), file_profile_pic);

        if(imgFile.exists())
        {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            iv_profile_pic.setImageBitmap(myBitmap);
        }
        selectedImageUri = null;
    }

    public void changeProfilePhoto(View view)
    {
        startActivityForResult(new Intent(this, TakePhotoActivity.class), TAKE_NEW_PHOTO);
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

    public void updateData(View view)
    {
        boolean run_task = false;
        TextView tv_pass = (TextView) findViewById(R.id.et_password);
        TextView tv_rpass = (TextView) findViewById(R.id.et_repeat_password);
        if(selectedImageUri != null)
        {
            run_task = true;
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        ObjectOutputStream os = mBoundService.getObjectStreamOut();
                        InputStream in = new FileInputStream(
                                new File(getApplicationContext().getFilesDir(),
                                        getResources().getString(R.string.default_profile_pic_name)));
                        os.writeObject(getResources().getString(R.string.command_profilepup) +
                                " " + username);
                        os.writeObject(in.available());
                        os.flush();
                        OutputStream outs = mBoundService.getStreamOut();

                        byte[] buf = new byte[getResources().getInteger(R.integer.bytes_on_photo)];
                        int len = 0;
                        while ((len = in.read(buf)) != -1)
                        {
                            outs.write(buf, 0, len);
                            outs.flush();
                        }
                        in.close();
                    }
                    catch (IOException ignored) { }
                }
            }).start();
        }
        else if(tv_pass.getText().length() > 0)
        {
            String pass = tv_pass.getText().toString();
            String rpass = tv_rpass.getText().toString();

            if(pass.equals(rpass))
            {
                mBoundService.sendMessage(getResources().getString(R.string.command_uppass) + " " +
                    username + " " + pass);
                run_task = true;
            }
            else
            {
                Toast.makeText(EditProfileActivity.this, getString(R.string.passwords_are_different),
                        Toast.LENGTH_LONG).show();
            }
        }
        if(run_task)
        {
            task = new ReceiveFromServerTask();
            task.execute();
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
                    String tq = (String)ins.readObject();
                    if(tq.startsWith(getResources().getString(R.string.command_uppass)))
                    {
                        String [] pms = tq.split(" ");
                        Integer resp = Integer.parseInt(pms[1]);
                        if(resp.equals(Response.OK))
                        {
                            response = getResources().getString(R.string.response_ok);
                        }
                    }
                    else if(tq.startsWith(getResources().getString(R.string.command_profilepup)))
                    {
                        String [] pms = tq.split(" ");
                        Integer resp = Integer.parseInt(pms[1]);
                        if(resp.equals(Response.OK))
                        {
                            response = getResources().getString(R.string.response_ok);

                            File own_file = new File(getApplicationContext().getFilesDir(),
                                    getResources().getString(R.string.default_profile_pic_name));
                            own_file.renameTo(new File(getApplicationContext().getFilesDir(), pms[2]));

                            SharedPreferences preferences = PreferenceManager.
                                    getDefaultSharedPreferences(EditProfileActivity.this);

                            File old_file = new File(preferences.getString(getString(R.string.profile_pic), ""));
                            if(old_file.exists())
                            {
                                old_file.delete();
                            }

                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(getString(R.string.profile_pic), pms[2]);
                            editor.apply();
                            editor.commit();
                        }
                    }
                    else continue;
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
                Toast.makeText(EditProfileActivity.this, getString(R.string.updated), Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(EditProfileActivity.this, getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
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
            mBoundService.setContext(EditProfileActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mBoundService = null;
        }

    };

    private void doBindService()
    {
        bindService(new Intent(EditProfileActivity.this, SocketService.class),
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
