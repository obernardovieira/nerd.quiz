package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import a21240068.isec.nerdquiz.Core.NerdQuizApp;
import a21240068.isec.nerdquiz.Core.SocketService;
import a21240068.isec.nerdquiz.Database.ProfilesData;
import a21240068.isec.nerdquiz.Objects.DownloadQuestion;
import a21240068.isec.nerdquiz.Objects.Game;
import a21240068.isec.nerdquiz.Database.GamesData;
import a21240068.isec.nerdquiz.Database.QuestionsData;

public class DashboardActivity extends Activity
{
    private ArrayList<Game> games;
    private boolean mIsBound;
    private SocketService mBoundService;
    private NerdQuizApp application;
    private Handler handler;
    private ReceiveFromServerTask task;
    private ReceivePhotoFromServerTask photo_task;
    private boolean update_db_task;
    private MyGamesHistoryAdapter adapter;
    private boolean logged;
    private boolean show_updt_notif = false;

    Runnable fromServerRunner;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        games = new ArrayList<>();
        adapter = new MyGamesHistoryAdapter();
        ListView lv = (ListView) findViewById(R.id.lv_history);
        lv.setAdapter(adapter);
        application = (NerdQuizApp)getApplication();

        fromServerRunner = new Runnable()
        {
            public void run()
            {
                task = new ReceiveFromServerTask ();
                task.execute();
            }
        };

        logged = false;
        update_db_task = false;
        handler = new Handler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.dashboard_options,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.id_profile)
        {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
            return true;
        }
        else if(item.getItemId() == R.id.id_upload_questions)
        {
            show_updt_notif = true;
            update_questions_database();
        }
        return super.onOptionsItemSelected(item);
    }

    public void update_questions_database()
    {
        SharedPreferences preferences = PreferenceManager.
                getDefaultSharedPreferences(this);
        int defaultValue = 0;
        int atualDBVersion = preferences.
                getInt(getString(R.string.version_dbquestions), defaultValue);

        update_db_task = true;
        mBoundService.sendMessage(getResources().getString(R.string.command_updatedb) +
                " " + atualDBVersion);
    }

    public void startNewGame(View view)
    {
        Intent intent = new Intent(this, NewGameActivity.class);
        startActivity(intent);
    }

    class MyGamesHistoryAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return games.size();
        }

        @Override
        public Object getItem(int i) {
            return games.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            View layout = getLayoutInflater().inflate(R.layout.layout_item_dashboard,null);

            String opponent_name = games.get(i).getOpponentName();
            String opponent_points = String.valueOf(games.get(i).getOpponentPoints());
            String player_points = String.valueOf(games.get(i).getPlayerPoints());
            String date = games.get(i).getDate();

            ((TextView)layout.findViewById(R.id.tv_opponent_name)).setText(opponent_name);
            ((TextView)layout.findViewById(R.id.tv_opponent_points)).setText(opponent_points);
            ((TextView)layout.findViewById(R.id.tv_player_points)).setText(player_points);
            ((TextView)layout.findViewById(R.id.tv_date)).setText(date);

            ProfilesData pdata = new ProfilesData(DashboardActivity.this);
            File imgFile = new File(getApplicationContext().getFilesDir(),
                    pdata.getProfilePic(games.get(i).getOpponentName()));

            if(imgFile.exists())
            {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ((ImageView)layout.findViewById(R.id.iv_profile_pic)).setImageBitmap(myBitmap);
            }
            return layout;
        }
    }

    private class ReceiveFromServerTask extends AsyncTask<Void, Void, String>
    {
        protected String doInBackground(Void... params)
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
                    ObjectInputStream in = mBoundService.getObjectStreamIn();
                    if(update_db_task)
                    {
                        QuestionsData qdata = new QuestionsData(DashboardActivity.this);
                        ArrayList<DownloadQuestion> questions = new ArrayList<>();
                        Integer version;

                        Object object = in.readObject();

                        if(object instanceof String)
                            Log.d("------------",(String)object);

                        Integer tq = (Integer)object;
                        Integer z = 0;

                        Log.d("tq", String.valueOf(tq));

                        while(z++ < tq)
                        {
                            questions.add((DownloadQuestion) in.readObject());
                        }
                        version = (Integer)in.readObject();
                        if(!questions.isEmpty())
                        {
                            qdata.updateQuestions(questions, version);

                            SharedPreferences preferences = PreferenceManager.
                                    getDefaultSharedPreferences(DashboardActivity.this);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt(getString(R.string.version_dbquestions), version);
                            editor.apply();
                        }
                        response = getResources().getString(R.string.response_ok);
                    }
                    else
                    {
                        response = (String)in.readObject();
                    }
                    break;
                }
            }
            catch (IOException | ClassNotFoundException ignored)
            {
                response = getResources().getString(R.string.response_error);
            }
            return response;
        }

        protected void onPostExecute(String result)
        {
            if(update_db_task)
            {
                if(show_updt_notif)
                {
                    if(result.equals(getResources().getString(R.string.response_ok)))
                        Toast.makeText(DashboardActivity.this,
                                "Questions database updated!", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(DashboardActivity.this,
                                "An error occurred!", Toast.LENGTH_LONG).show();

                    show_updt_notif = false;
                }
                update_db_task = false;
                handler.post(fromServerRunner);
            }
            else
            {
                if(result.equals(getResources().getString(R.string.response_error)))
                {
                    Toast.makeText(DashboardActivity.this,
                            "An error occurred!", Toast.LENGTH_LONG).show();
                    return;
                }

                final String [] params = result.split(" ");
                if(params[0].equals(getResources().getString(R.string.command_beinvited)))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
                    builder.setMessage("Do you want to play with " + params[1] + " ?")
                            .setTitle("Invited");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            ProfilesData pdata = new ProfilesData(DashboardActivity.this);
                            if(!pdata.search(params[1]))
                            {
                                handler.post(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        photo_task = new ReceivePhotoFromServerTask();
                                        photo_task.execute(params[1]);
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
                                    getString(R.string.command_reject) + " " + params[1]);
                            handler.post(fromServerRunner);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else
                {
                    handler.post(fromServerRunner);
                }
            }
        }

        public void onCancelled()
        { }

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
                Toast.makeText(DashboardActivity.this,
                        "An error occurred!", Toast.LENGTH_LONG).show();
            }
            else
            {
                mBoundService.sendMessage(getResources().
                        getString(R.string.command_accept) + " " + result);

                Intent intent = new Intent(DashboardActivity.this, GameActivity.class);
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
        //
        GamesData gdata = new GamesData(this);
        games = gdata.listAll();
        adapter.notifyDataSetChanged();
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
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBoundService = ((SocketService.LocalBinder) service).getService();
            mBoundService.setContext(DashboardActivity.this);
            Log.d("logged", String.valueOf(logged));
            if (!logged)
            {
                Log.d("logged2", String.valueOf(logged));
                SharedPreferences preferences = PreferenceManager.
                            getDefaultSharedPreferences(DashboardActivity.this);
                String defaultValue = getResources().getString(R.string.no_user_name_default);
                String username = preferences.getString(getString(R.string.user_name), defaultValue);
                //
                Log.d("application.getUsername", application.getUsername());
                if (application.getUsername().equals(defaultValue))
                {
                    mBoundService.sendMessage(getResources().getString(R.string.command_autologin) +
                            " " + username);
                    application.setUsername(username);

                    //schedule aqui
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            update_questions_database();
                        }
                    }).start();

                }
                logged = true;
            }
            //
            if(mBoundService.isConnected())
            {
                handler.post(fromServerRunner);
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
        bindService(new Intent(DashboardActivity.this, SocketService.class),
                mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService()
    {
        if (mIsBound)
        {
            unbindService(mConnection);
            mIsBound = false;
            if(task != null)
            {
                task.cancel(true);
            }
            if(photo_task != null)
            {
                photo_task.cancel(true);
            }
        }
    }
}
