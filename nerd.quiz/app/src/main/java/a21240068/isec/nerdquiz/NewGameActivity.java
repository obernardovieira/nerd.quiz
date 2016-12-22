package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import a21240068.isec.nerdquiz.Core.Command;
import a21240068.isec.nerdquiz.Core.SocketService;

public class NewGameActivity extends Activity {

    private final int INVITE_PLAYER_CODE = 0;

    private boolean mIsBound;
    private SocketService mBoundService;

    Runnable fromServerRunner;
    private ReceiveFromServerTask fromServerTask;
    private boolean first_attempt;
    private String reInvite;

    private Handler handler;
    ObjectInputStream in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if(extras == null)
            {
                reInvite = null;
            }
            else
            {
                reInvite = extras.getString("reInvite");
            }
        }

        fromServerRunner = new Runnable(){
            public void run() {
                fromServerTask = new ReceiveFromServerTask ();
                fromServerTask.execute();
            }
        };

        handler = new Handler();

        first_attempt = true;
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

                in = mBoundService.getObjectStreamIn();
                handler.post(fromServerRunner);
                if(reInvite != null)
                {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            invitePlayer(reInvite);
                        }
                    });

                }
            }
        }).start();
    }

    public void clickSearchPlayerButton(View view)
    {
        startActivityForResult(new Intent(this, SearchPlayerActivity.class), INVITE_PLAYER_CODE);
    }

    private void invitePlayer(String username)
    {
        //
        mBoundService.sendMessage(Command.INVITE + " " + username);

        TextView message = (TextView)findViewById(R.id.tv_message);
        message.setText("Waiting opponet's answer ...");

        Toast.makeText(NewGameActivity.this, username +
                " invited!", Toast.LENGTH_LONG).show();
    }

    private void processInvitationAnswer(String answer)
    {
        if(answer == null)
            return;

        String [] params = answer.split(" ");

        if(answer.startsWith(Command.ACCEPT_INV))
        {
            //

            Toast.makeText(NewGameActivity.this, params[1] +
                    " accepted your invitation!", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("playerToPlay", params[1]);
            intent.putExtra("isInvited", false);
            startActivity(intent);

            finish();
        }
        else if(answer.startsWith(Command.REJECT_INV))
        {
            //
            Toast.makeText(NewGameActivity.this, params[1] +
                    " rejected your invitation!", Toast.LENGTH_LONG).show();
        }
        else if(answer.startsWith(Command.INVITED))
        {
            //what if being invited meanwhile?
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == INVITE_PLAYER_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                Log.d("resultFromINvittion", String.valueOf(resultCode));
                invitePlayer(data.getStringExtra("name"));
            }
        }
    }

    private class ReceiveFromServerTask extends AsyncTask<Void, Void, String>
    {
        protected String doInBackground(Void... params)
        {
            String response = null;
            Log.d("doInBackground(NGA)", "started");
            try
            {
                while(!isCancelled())
                {
                    //Log.d("ReceiveFromServerTask", String.valueOf(mBoundService.socket.getInputStream().available()));
                    if(mBoundService.socket.getInputStream().available() > 4)
                    {
                        //ObjectInputStream in;
                        //in = new ObjectInputStream(mBoundService.socket.getInputStream());

                        Object obj = in.readObject();

                        if(obj instanceof String) {
                            response = (String) obj;

                            //in.close();
                            break;
                        }
                        /*else if(obj instanceof Integer)
                        {
                            Log.d("mqsqdsf",String.valueOf((Integer)obj));
                        }*/
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Log.d("ReceiveFromServerTask","b");
            return response;
        }

        protected void onPostExecute(String result) {
            Log.d("onPostExecute(NGA)",result);
            processInvitationAnswer(result);
        }

        protected void onCancelled()
        {
            Log.d("onCancelled(NGA)","fdghnafsbdnsbdg");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();


        doBindService();

        if(first_attempt == false) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mBoundService == null) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    while (mBoundService.socket == null) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    handler.post(fromServerRunner);
                }
            }).start();
        }
        else {
            first_attempt = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        doUnbindService();

        fromServerTask.cancel(true);
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
        bindService(new Intent(NewGameActivity.this, SocketService.class), mConnection, Context.BIND_AUTO_CREATE);
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
