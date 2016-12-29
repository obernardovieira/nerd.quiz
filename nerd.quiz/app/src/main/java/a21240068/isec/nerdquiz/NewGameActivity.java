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

public class NewGameActivity extends Activity
{
    private final int INVITE_PLAYER_CODE = 0;
    private boolean mIsBound;
    private SocketService mBoundService;
    private ReceiveFromServerTask fromServerTask;
    private boolean first_attempt;
    private String reInvite;
    private Handler handler;

    Runnable fromServerRunner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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

        fromServerRunner = new Runnable()
        {
            public void run()
            {
                fromServerTask = new ReceiveFromServerTask();
                fromServerTask.execute();
            }
        };

        handler = new Handler();
    }

    public void clickSearchPlayerButton(View view)
    {
        startActivityForResult(new Intent(this, SearchPlayerActivity.class), INVITE_PLAYER_CODE);
    }

    private void invitePlayer(String username)
    {
        mBoundService.sendMessage(Command.INVITE + " " + username);

        TextView message = (TextView)findViewById(R.id.tv_message);
        message.setText("Waiting opponet's answer ...");

        Toast.makeText(NewGameActivity.this, username +
                " invited!", Toast.LENGTH_LONG).show();
    }

    private void processInvitationAnswer(String answer)
    {
        if(answer == null)
        {
            return;
        }

        String [] params = answer.split(" ");
        if(answer.startsWith(Command.ACCEPT_INV))
        {
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
                invitePlayer(data.getStringExtra("name"));
            }
        }
    }

    private class ReceiveFromServerTask extends AsyncTask<Void, Void, String>
    {
        protected String doInBackground(Void... params)
        {
            String response = null;
            try
            {
                while(!isCancelled())
                {
                    if(mBoundService.socket.getInputStream().available() > 4)
                    {
                        ObjectInputStream in = mBoundService.getObjectStreamIn();
                        response = (String)in.readObject();
                    }
                }
            }
            catch (IOException | ClassNotFoundException ignored)
            {
                response = null;
            }
            return response;
        }

        protected void onPostExecute(String result)
        {
            //
            processInvitationAnswer(result);
        }

        protected void onCancelled()
        {
            //
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

    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mBoundService = ((SocketService.LocalBinder)service).getService();
            if(mBoundService.isConnected())
            {
                handler.post(fromServerRunner);
                if (reInvite != null)
                {
                    invitePlayer(reInvite);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mBoundService = null;
        }

    };

    private void doBindService()
    {
        bindService(new Intent(NewGameActivity.this, SocketService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService()
    {
        if (mIsBound)
        {
            unbindService(mConnection);
            mIsBound = false;
            if(fromServerTask != null)
            {
                fromServerTask.cancel(true);
            }
        }
    }
}
