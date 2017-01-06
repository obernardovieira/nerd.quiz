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
import android.widget.ImageView;
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
    private String reInvite;
    private Handler handler;

    String invited_by;
    String profilepic_invited_by;
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
        message.setText(getString(R.string.wait_other_answer));

        ((ImageView)findViewById(R.id.img_invited)).setImageResource(R.drawable.invited);

        Toast.makeText(NewGameActivity.this, username +
                getString(R.string._invited), Toast.LENGTH_LONG).show();
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

    private String receiveProfilePicResult(ObjectInputStream ins)
            throws IOException, ClassNotFoundException
    {
        String response;
        Integer size = (Integer) ins.readObject();
        Integer received = 0;
        BufferedInputStream in = new BufferedInputStream(mBoundService.getStreamIn());

        OutputStream out = new FileOutputStream(
                new File(getApplicationContext().getFilesDir(), profilepic_invited_by));

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

        response = getResources().getString(R.string.command_profilepdown) + " " + invited_by;
        return response;
    }

    private class ReceiveFromServerTask extends AsyncTask<Void, Void, String>
    {
        protected String doInBackground(Void... params)
        {
            String response = null;
            Object object;
            try
            {
                while(!isCancelled())
                {
                    if(mBoundService.socket.getInputStream().available() > 4)
                    {
                        ObjectInputStream in = mBoundService.getObjectStreamIn();
                        object = in.readObject();
                        if(object instanceof String)
                        {
                            response = (String)object;
                            if(response.startsWith(getResources().getString(R.string.command_getppic)))
                            {
                                String [] pms = response.split(" ");
                                profilepic_invited_by = pms[1];
                                mBoundService.sendMessage(getResources().
                                        getString(R.string.command_profilepdown) + " " + params[1]);
                            }
                            else if(response.equals(getResources().getString(R.string.command_profilepdown)))
                            {
                                response = receiveProfilePicResult(in);
                            }
                        }
                        break;
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
            String [] params = result.split(" ");
            if(result.startsWith(getResources().getString(R.string.command_accept)))
            {
                Toast.makeText(NewGameActivity.this, params[1] +
                        getString(R.string._accepted_invitation), Toast.LENGTH_LONG).show();

                Intent intent = new Intent(NewGameActivity.this, GameActivity.class);
                intent.putExtra("playerToPlay", params[1]);
                intent.putExtra("isInvited", false);
                startActivity(intent);

                finish();
            }
            else if(result.startsWith(getResources().getString(R.string.command_reject)))
            {
                Toast.makeText(NewGameActivity.this, params[1] +
                        getString(R.string._rejected_invitation), Toast.LENGTH_LONG).show();
                handler.post(fromServerRunner);
            }
            else if(result.startsWith(getResources().getString(R.string.command_beinvited)))
            {
                if(invited_by.length() > 0)
                {
                    return;
                }
                invited_by = params[1];
                AlertDialog.Builder builder = new AlertDialog.Builder(NewGameActivity.this);
                builder.setMessage(getString(R.string.invitation_request) + params[1] + " ?")
                        .setTitle(R.string.invited);

                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        ProfilesData pdata = new ProfilesData(NewGameActivity.this);
                        if(!pdata.search(invited_by))
                        {
                            mBoundService.sendMessage(getResources().
                                    getString(R.string.command_getppic) + " " + invited_by);
                        }
                        else
                        {
                            mBoundService.sendMessage(getResources().
                                    getString(R.string.command_accept) + " " + invited_by);

                            Intent intent = new Intent(NewGameActivity.this, GameActivity.class);
                            intent.putExtra("playerToPlay", invited_by);
                            intent.putExtra("isInvited", true);
                            startActivity(intent);
                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        mBoundService.sendMessage(getResources().
                                getString(R.string.command_reject) + " " + invited_by);
                        handler.post(fromServerRunner);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else if(result.startsWith(getResources().getString(R.string.command_profilepdown)))
            {
                String [] pms = result.split(" ");
                if(pms.length > 1)
                {
                    mBoundService.sendMessage(getResources().
                            getString(R.string.command_accept) + " " + result);

                    Intent intent = new Intent(NewGameActivity.this, GameActivity.class);
                    intent.putExtra("playerToPlay", invited_by);
                    intent.putExtra("isInvited", true);
                    startActivity(intent);
                }
                else
                {
                    mBoundService.errorConnection();
                }
            }
            else
            {
                handler.post(fromServerRunner);
            }
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
        invited_by = "";
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
        }
    }
}
