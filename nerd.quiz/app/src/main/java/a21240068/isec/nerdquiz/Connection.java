package a21240068.isec.nerdquiz;

import android.app.Application;

import java.net.Socket;

/**
 * Created by bernardovieira on 21-11-2016.
 */

public class Connection extends Application {

    public String serverIP;
    public Integer serverPort;
    public Socket socketToServer;
    public Socket socketForClientServer;
    public Socket socketForClientClient;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}