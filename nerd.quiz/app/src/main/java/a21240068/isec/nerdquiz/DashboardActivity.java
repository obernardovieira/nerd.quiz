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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import a21240068.isec.nerdquiz.Core.Command;
import a21240068.isec.nerdquiz.Core.NerdQuizApp;
import a21240068.isec.nerdquiz.Core.SocketService;
import a21240068.isec.nerdquiz.Database.ProfilesData;
import a21240068.isec.nerdquiz.Objects.DownloadQuestion;
import a21240068.isec.nerdquiz.Objects.Game;
import a21240068.isec.nerdquiz.Database.GamesData;
import a21240068.isec.nerdquiz.Database.QuestionsData;
import a21240068.isec.nerdquiz.Objects.Profile;

public class DashboardActivity extends Activity {

    private ArrayList<Game> games;
    private boolean mIsBound;
    private SocketService mBoundService;
    private NerdQuizApp application;

    private Handler handler;
    Runnable fromServerRunner;
    private ReceiveFromServerTask fromServerTask;
    private boolean update_db_task;
    private boolean first_attempt;

    private MyGamesHistoryAdapter adapter;

    ObjectInputStream in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        games = new ArrayList<>();

        adapter = new MyGamesHistoryAdapter();
        ListView lv = (ListView) findViewById(R.id.lv_history);
        lv.setAdapter(adapter);
        application = (NerdQuizApp)getApplication();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this);
        String defaultValue = getResources().getString(R.string.no_user_name_default);
        String username = preferences.getString(getString(R.string.user_name), defaultValue);
        application.setUsername(username);

        fromServerRunner = new Runnable(){
            public void run() {
                fromServerTask = new ReceiveFromServerTask ();
                fromServerTask.execute();
            }
        };

        update_db_task = false;

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

                mBoundService.sendMessage(Command.AUTO_LOGIN + " " + application.getUsername());
                /*try {
                    in = new ObjectInputStream(mBoundService.socket.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                in = mBoundService.getObjectStreamIn();
                handler.post(fromServerRunner);
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_options,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.id_profile) {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
            return true;
        }
        else if(item.getItemId() == R.id.id_upload_questions)
        {
            //QuestionsData qdata = new QuestionsData(this);
            //qdata.updateQuestions();

            //mensagem ao servidor a pedir perguntas
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            int defaultValue = 0;
            int atualDBVersion = preferences.getInt(getString(R.string.version_dbquestions), defaultValue);

            update_db_task = true;
            mBoundService.sendMessage(Command.UPDATE_DB + " " + atualDBVersion);


        }
        return super.onOptionsItemSelected(item);
    }

    public void startNewGame(View view)
    {
        Intent intent = new Intent(this, NewGameActivity.class);
        startActivity(intent);
    }

    class MyGamesHistoryAdapter extends BaseAdapter {

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
        public View getView(int i, View view, ViewGroup viewGroup) {
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
            Log.d("interface",games.get(i).getOpponentName());
            File imgFile = new File(getApplicationContext().getFilesDir(),
                    pdata.getProfilePic(games.get(i).getOpponentName()));

            if(imgFile.exists())
            {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ((ImageView)layout.findViewById(R.id.iv_profile_pic)).setImageBitmap(myBitmap);
            }

            //((ImageView)layout.findViewById(R.id.iv_profile_pic)).setImageResource(android.R.drawable.btn_star_big_on);

            return layout;
        }
    }


    private class ReceiveFromServerTask extends AsyncTask<Void, Void, String>
    {
        protected String doInBackground(Void... params)
        {
            String response = "";
            Log.d("doInBackground(DBA)", "started");
            try
            {
                while(!isCancelled())
                {
                    //Log.d("ReceiveFromServerTask", String.valueOf(mBoundService.socket.getInputStream().available()));
                    if(mBoundService.socket.getInputStream().available() > 4)
                    {
                        //ObjectInputStream in;
                        //in = new ObjectInputStream(mBoundService.socket.getInputStream());
                        if(update_db_task)
                        {
                            //
                            Log.d("update","c");
                            //ObjectInputStream in;
                            //in = new ObjectInputStream(mBoundService.socket.getInputStream());
                            QuestionsData qdata = new QuestionsData(DashboardActivity.this);
                            Log.d("update","c");

                            ArrayList<DownloadQuestion> questions = new ArrayList<>();
                            Log.d("update","c");
                            Integer version;
                            Log.d("update","c");

                            Integer tq = (Integer)in.readObject();
                            Integer z = 0;

                            while(z++ < tq)
                            {
                                questions.add((DownloadQuestion) in.readObject());
                            }

                            //questions = (ArrayList<DownloadQuestion>)in.readObject();
                            Log.d("update","c");
                            version = (Integer)in.readObject();//in.readObject();
                            Log.d("update","d");
                            if(!questions.isEmpty())
                            {
                                qdata.updateQuestions(questions, version);

                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt(getString(R.string.version_dbquestions), version);
                                editor.apply();
                            }
                            else
                            {
                                Log.d("UpdateDBTask","no questions to update!");
                            }
                            Log.d("update","e");
                        }
                        else
                        {
                            //
                            Log.d("update","z");
                            response = (String)in.readObject();
                        }
                        //in.close();
                        break;
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                Log.d("noclass","b");
                e.printStackTrace();
            }
            Log.d("ReceiveFromServerTask","b");
            return response;
        }

        protected void onPostExecute(String result) {
            Log.d("onPostExecute(DBA)",result);

            if(update_db_task == true)
            {
                //
                Toast.makeText(DashboardActivity.this, "Questions database updated!", Toast.LENGTH_LONG).show();
                update_db_task = false;
                handler.post(fromServerRunner);
            }
            else
            {
                //
                //String contem "beinvited nomejogador"
                final String [] params = result.split(" ");
                if(params[0].equals(Command.INVITED))
                {
                /*Toast.makeText(DashboardActivity.this,
                        "You have been invited by " + params[1], Toast.LENGTH_LONG).show();*/

                    // 1. Instantiate an AlertDialog.Builder with its constructor
                    AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);

                    // 2. Chain together various setter methods to set the dialog characteristics
                    builder.setMessage("Do you want to play with " + params[1] + " ?")
                            .setTitle("Invited");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button

                            final ProfilesData pdata = new ProfilesData(DashboardActivity.this);
                            if(!pdata.search(params[1]))
                            {

                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {

                                        try {
                                            String profile_pic = "";

                                            mBoundService.sendMessage("getppic" +
                                                    " " + params[1]);

                                            profile_pic = (String)in.readObject();

                                            mBoundService.sendMessage(Command.PROFILE_PIC_DOWN +
                                                    " " + profile_pic);

                                            Integer size = (Integer) in.readObject();
                                            Integer received = 0;
                                            BufferedInputStream in = new BufferedInputStream(mBoundService.getStreamIn());

                                            OutputStream out = new FileOutputStream(
                                                    new File(getApplicationContext().getFilesDir(), profile_pic));
                                            Log.d("receiving file", "abc");

                                            byte[] buf = new byte[8192];
                                            int len = 0;
                                            while ((len = in.read(buf)) != -1) {
                                                out.write(buf, 0, len);
                                                if (received + len == size)
                                                    break;
                                                else
                                                    received += len;
                                            }

                                            Log.d("received", "abc");
                                            out.close();

                                            if(pdata.add(params[1], profile_pic))
                                            {
                                                Log.d("posttt","adicionado");
                                            }
                                            else
                                            {
                                                Log.d("posttt","errrrror");
                                            }

                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {

                                                    mBoundService.sendMessage(Command.ACCEPT_INV + " " + params[1]);

                                                    Intent intent = new Intent(DashboardActivity.this, GameActivity.class);
                                                    intent.putExtra("playerToPlay", params[1]);
                                                    intent.putExtra("isInvited", true);
                                                    startActivity(intent);
                                                }
                                            });

                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();




                            }

                        /*
                        ServerSocket game_socket = new ServerSocket(5009);

                        mBoundService.sendMessage(Command.ACCEPT_INV + " " + params[1]);
                        mBoundService.sendMessage(game_socket.getInetAddress());
                        mBoundService.sendMessage(5009);
                        */

                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            mBoundService.sendMessage(Command.REJECT_INV + " " + params[1]);
                            handler.post(fromServerRunner);
                        }
                    });
                    // Set other dialog properties


                    // 3. Get the AlertDialog from create()
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else
                {
                    handler.post(fromServerRunner);
                    Log.d("adsfgthj", "por aqui");
                }
            }
        }

        public void onCancelled()
        {
            Log.d("fuckingStop(DBA)", "Cancelled.");
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

        GamesData gdata = new GamesData(this);
        games = gdata.listAll();

        adapter.notifyDataSetChanged();
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
        bindService(new Intent(DashboardActivity.this, SocketService.class), mConnection, Context.BIND_AUTO_CREATE);
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
