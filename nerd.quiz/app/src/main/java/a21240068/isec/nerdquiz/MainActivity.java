package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import java.net.Socket;

public class MainActivity extends Activity {

    NerdQuizApp nerdQuizApp;
    Handler mainHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nerdQuizApp = (NerdQuizApp)getApplication();
        mainHandler = new Handler();

        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.d("ConnectToRemoteServer", "Connecting to the server 192.168.1.7");
                    nerdQuizApp.socketToServer = new Socket("192.168.1.7", 5007);
                    Log.d("ConnectToRemoteServer", "Successfully connected!");
                }
                catch (Exception e)
                {
                    nerdQuizApp.socketToServer = null;
                }
                if (nerdQuizApp.socketToServer == null)
                {
                    mainHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Log.d("ConnectToRemoteServer","An error occurred!");
                            finish();
                        }
                    });
                    return;
                }
            }
        });
        t.start();
        try
        {
            t.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultValue = getResources().getString(R.string.no_user_name_default);
        String username = preferences.getString(getString(R.string.user_name), defaultValue);

        if(!username.equals(defaultValue))
        {
            Intent intent = new Intent(this, AuthenticationActivity.class);
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
        }

        finish();
    }
}
