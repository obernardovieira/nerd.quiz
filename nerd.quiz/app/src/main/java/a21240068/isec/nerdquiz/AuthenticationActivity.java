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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import a21240068.isec.nerdquiz.Core.Command;
import a21240068.isec.nerdquiz.Core.Response;
import a21240068.isec.nerdquiz.Core.SocketService;

public class AuthenticationActivity extends Activity {

    private boolean mIsBound;
    private SocketService mBoundService;

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
    }

    public void clickNotRegisteredText(View view)
    {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);

        finish();
    }

    private class ReceiveFromServerTask extends AsyncTask<Void, Void, String>
    {
        private boolean cancelledFlag;
        public ReceiveFromServerTask()
        {
            cancelledFlag = false;
        }

        protected String doInBackground(Void... params)
        {
            Integer response = Response.ERROR;

            try
            {
                while(cancelledFlag == false)
                {
                    if(mBoundService.socket.getInputStream().available() > 4)
                    {
                        ObjectInputStream in;
                        in = new ObjectInputStream(mBoundService.socket.getInputStream());
                        response = (Integer)in.readObject();
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
                String username = tv_username.getText().toString();

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AuthenticationActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.user_name), username);
                editor.apply();

                Toast.makeText(AuthenticationActivity.this, "You are logged now!", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(AuthenticationActivity.this, DashboardActivity.class);
                startActivity(intent);

                cancel(true);
                finish();
            }
            else if(response == Response.ERROR)
            {
                Toast.makeText(AuthenticationActivity.this, "An error occurred while login!", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            cancelledFlag = true;
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
