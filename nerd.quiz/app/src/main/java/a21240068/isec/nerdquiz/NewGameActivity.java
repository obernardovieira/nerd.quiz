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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import a21240068.isec.nerdquiz.Core.SocketService;
import a21240068.isec.nerdquiz.Database.ProfilesData;

public class NewGameActivity extends Activity
{
    private final int INVITE_PLAYER_CODE = 0;
    private boolean mIsBound;
    private SocketService mBoundService;
    private ReceiveFromServerTask fromServerTask;
    private ReceivePhotoFromServerTask photo_task;
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
        mBoundService.sendMessage(getResources().getString(R.string.command_invite) + " " + username);

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
        if(answer.startsWith(getResources().getString(R.string.command_accept)))
        {
            Toast.makeText(NewGameActivity.this, params[1] +
                    " accepted your invitation!", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("playerToPlay", params[1]);
            intent.putExtra("isInvited", false);
            startActivity(intent);

            finish();
        }
        else if(answer.startsWith(getResources().getString(R.string.command_reject)))
        {
            Toast.makeText(NewGameActivity.this, params[1] +
                    " rejected your invitation!", Toast.LENGTH_LONG).show();
        }
        else if(answer.startsWith(getResources().getString(R.string.command_beinvited)))
        {
            final String inveted_by = params[1];
            AlertDialog.Builder builder = new AlertDialog.Builder(NewGameActivity.this);
            builder.setMessage("Do you want to play with " + params[1] + " ?")
                    .setTitle("Invited");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    ProfilesData pdata = new ProfilesData(NewGameActivity.this);
                    if(!pdata.search(inveted_by))
                    {
                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                photo_task = new ReceivePhotoFromServerTask();
                                photo_task.execute(inveted_by);
                            }
                        });
                    }
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    mBoundService.sendMessage(getResources().
                            getString(R.string.command_reject) + " " + inveted_by);
                    handler.post(fromServerRunner);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
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

    private class ReceivePhotoFromServerTask extends AsyncTask<String, Void, String>
    {
        protected String doInBackground(String... params)
        {
            String response = "";
            try
            {
                while(!isCancelled())
                {
                    if(mBoundService.socket.getInputStream().available() < 4)
                    {
                        continue;
                    }
                    try
                    {
                        String profile_pic = "";
                        ObjectInputStream ins = mBoundService.getObjectStreamIn();
                        mBoundService.sendMessage(getResources().
                                getString(R.string.command_getppic) + " " + params[0]);

                        profile_pic = (String)ins.readObject();
                        mBoundService.sendMessage(getResources().
                                getString(R.string.command_profilepdown) + " " + profile_pic);

                        Integer size = (Integer) ins.readObject();
                        Integer received = 0;
                        BufferedInputStream in = new BufferedInputStream(mBoundService.getStreamIn());

                        OutputStream out = new FileOutputStream(
                                new File(getApplicationContext().getFilesDir(), profile_pic));

                        byte[] buf = new byte[getResources().getInteger(R.integer.bytes_on_photo)];
                        int len = 0;
                        while ((len = in.read(buf)) != -1)
                        {
                            out.write(buf, 0, len);
                            if (received + len == size)
                                break;
                            else
                                received += len;
                        }
                        out.close();

                        response = params[0];

                    }
                    catch (IOException | ClassNotFoundException e)
                    {
                        response = getResources().getString(R.string.response_error);
                    }
                    break;
                }
            }
            catch (IOException ignored)
            {
                response = getResources().getString(R.string.response_error);
            }
            return response;
        }

        protected void onPostExecute(String result)
        {
            if(result.equals(getResources().getString(R.string.response_error)))
            {
                Toast.makeText(NewGameActivity.this,
                        "An error occurred!", Toast.LENGTH_LONG).show();
            }
            else
            {
                mBoundService.sendMessage(getResources().
                        getString(R.string.command_accept) + " " + result);

                Intent intent = new Intent(NewGameActivity.this, GameActivity.class);
                intent.putExtra("playerToPlay", result);
                intent.putExtra("isInvited", true);
                startActivity(intent);
            }
        }

        public void onCancelled()
        { }
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
            mBoundService.setContext(NewGameActivity.this);
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
            if(photo_task != null)
            {
                photo_task.cancel(true);
            }
        }
    }
}
