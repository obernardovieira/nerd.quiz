package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class NewGameActivity extends Activity {

    private final int INVITE_PLAYER_CODE = 0;

    NerdQuizApp nerdQuizApp;
    //Socket socketGame = null;
    BufferedReader input;
    PrintWriter output;
    Handler procMsg = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        procMsg = new Handler();
        nerdQuizApp = (NerdQuizApp)getApplication();

        //clientDlg();
        client("192.168.1.10", 5007);
    }

    public void clickSearchPlayerButton(View view)
    {
        //startActivityForResult(new Intent(this, SearchPlayerActivity.class), INVITE_PLAYER_CODE);
    }

    public void clickPlayGameButton(View view)
    {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);

        finish();
    }

    private void invitePlayer(String username)
    {
        // send request for game
        // wait for answer

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == INVITE_PLAYER_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                // A contact was picked.  Here we will just display it
                // to the user.
                invitePlayer(data.getStringExtra("name"));
            }
        }
    }

    public void clientDlg() {

        final EditText edtIP = new EditText(this);
        edtIP.setText("192.168.1.10");
        AlertDialog ad = new AlertDialog.Builder(this).setTitle("RPS Client")
                .setMessage("Server IP").setView(edtIP)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        client(edtIP.getText().toString(), 5007); // to test with emulators: PORTaux);
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                }).create();
        ad.show();
    }

    public void client(final String strIP, final int Port) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("RPS", "Connecting to the server  " + strIP);
                    nerdQuizApp.socketToServer = new Socket(strIP, Port);
                } catch (Exception e) {
                    nerdQuizApp.socketToServer = null;
                }
                if (nerdQuizApp.socketToServer == null) {
                    procMsg.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("procMsg","no run");
                            //finish();
                        }
                    });
                    return;
                }
                commThread.start();
            }
        });
        t.start();
    }

    Thread commThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {

                OutputStream oStream = nerdQuizApp.socketToServer.getOutputStream();
                InputStream iStream = nerdQuizApp.socketToServer.getInputStream();
                ObjectOutputStream ooStream = new ObjectOutputStream(oStream);
                ObjectInputStream oiStream = new ObjectInputStream(iStream);

                Log.d("commThread" , "b1");
                ooStream.writeObject("Hello!");
                ooStream.flush();
                Log.d("commThread" , "abc");
                String read = (String) oiStream.readObject();
                Log.d("commThread" , "xyz");

                Log.d("RPS", "Received: " + read);

                /*input = new BufferedReader(new InputStreamReader(socketGame.getInputStream()));
                output = new PrintWriter(socketGame.getOutputStream());
                while (!Thread.currentThread().isInterrupted()) {
                    String read = input.readLine();
                    Log.d("RPS", "Received: " + read);
                }*/
            } catch (Exception e) {
                procMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        //finish();
                    }
                });
            }
        }
    });

    protected void onPause() {
        super.onPause();
        try {
            commThread.interrupt();
            if (nerdQuizApp.socketToServer != null)
                nerdQuizApp.socketToServer.close();
            if (output != null)
                output.close();
            if (input != null)
                input.close();
        } catch (Exception e) {
        }
        input = null;
        output = null;
        nerdQuizApp.socketToServer = null;
    };
}
