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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;

public class NewGameActivity extends Activity {

    private final int INVITE_PLAYER_CODE = 0;

    NerdQuizApp nerdQuizApp;
    Handler mainHandler;
    //
    OutputStream oStream;
    InputStream iStream;

    ObjectOutputStream ooStream;
    ObjectInputStream oiStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        mainHandler = new Handler();
        nerdQuizApp = (NerdQuizApp)getApplication();
    }

    public void clickSearchPlayerButton(View view)
    {
        startActivityForResult(new Intent(this, SearchPlayerActivity.class), INVITE_PLAYER_CODE);
    }

    public void clickPlayGameButton(View view)
    {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);

        finish();
    }

    private void invitePlayer(String username)
    {
        //
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == INVITE_PLAYER_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                invitePlayer(data.getStringExtra("name"));
            }
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        //
        communicationThread.interrupt();
        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                //
                try
                {
                    oStream.close();
                    iStream.close();
                    ooStream.close();
                    oiStream.close();
                    mainHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Log.d("onPause","Streams closed!");
                        }
                    });
                }
                catch (IOException e)
                {
                    mainHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Log.d("onPause","Error closing streams!");
                        }
                    });
                }
            }
        });
        t.start();
    };

    @Override
    protected void onResume()
    {
        super.onResume();
        //
        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                //
                try
                {
                    oStream = nerdQuizApp.socketToServer.getOutputStream();
                    iStream = nerdQuizApp.socketToServer.getInputStream();
                    ooStream = new ObjectOutputStream(oStream);
                    oiStream = new ObjectInputStream(iStream);
                    mainHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Log.d("onResume", "Streams opened!");
                        }
                    });
                    communicationThread.start();
                }
                catch (IOException e)
                {
                    mainHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Log.d("onResume", "Error opening streams!");
                        }
                    });
                }
            }
        });
        t.start();
    };

    Thread communicationThread = new Thread(new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
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
            }
            catch (Exception e)
            {
                mainHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Log.d("communicationThread","An error occurred!");
                        finish();
                    }
                });
            }
        }
    });
}
