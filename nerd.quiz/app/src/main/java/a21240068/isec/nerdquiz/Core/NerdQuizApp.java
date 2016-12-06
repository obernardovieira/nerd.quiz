package a21240068.isec.nerdquiz.Core;

import android.app.Application;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by bernardovieira on 21-11-2016.
 */

public class NerdQuizApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("NerdQuizApp", "Create nerdQuizApp class!");
    }

}