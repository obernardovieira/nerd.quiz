package a21240068.isec.nerdquiz.Core;

import android.app.Application;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import a21240068.isec.nerdquiz.R;

/**
 * Created by bernardovieira on 21-11-2016.
 */

public class NerdQuizApp extends Application {

    private String username;

    @Override
    public void onCreate()
    {
        super.onCreate();
        this.username = getResources().getString(R.string.no_user_name_default);
        Log.d("onCreate", "Create nerdQuizApp class!");
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }

}