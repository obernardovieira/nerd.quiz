package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.util.ArrayList;

import a21240068.isec.nerdquiz.Core.Command;
import a21240068.isec.nerdquiz.Core.NerdQuizApp;
import a21240068.isec.nerdquiz.Core.Response;
import a21240068.isec.nerdquiz.Core.SocketService;
import a21240068.isec.nerdquiz.Objects.Game;
import a21240068.isec.nerdquiz.Database.GamesData;
import a21240068.isec.nerdquiz.Database.QuestionsData;

public class DashboardActivity extends Activity {

    private ArrayList<Game> games;
    private boolean mIsBound;
    private SocketService mBoundService;
    private NerdQuizApp application;

    private Handler handler;
    private Runnable myRunner;

    ObjectInputStream in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        GamesData gdata = new GamesData(this);
        games = gdata.listAll();

        ListView lv = (ListView) findViewById(R.id.lv_history);
        lv.setAdapter(new MyGamesHistoryAdapter());
        application = (NerdQuizApp)getApplication();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(DashboardActivity.this);
        String defaultValue = getResources().getString(R.string.no_user_name_default);
        String username = preferences.getString(getString(R.string.user_name), defaultValue);
        application.setUsername(username);

        myRunner = new Runnable(){
            public void run() {
                new ReceiveFromServerTask ().execute();
            }
        };

        handler = new Handler();
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
            QuestionsData qdata = new QuestionsData(this);
            qdata.updateQuestions();
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

            String opponent_name = (String) games.get(i).getOpponentName();
            String opponent_points = String.valueOf((int) games.get(i).getOpponentPoints());
            String player_points = String.valueOf((int) games.get(i).getPlayerPoints());
            String date = (String) games.get(i).getDate();

            ((TextView)layout.findViewById(R.id.tv_opponent_name)).setText(opponent_name);
            ((TextView)layout.findViewById(R.id.tv_opponent_points)).setText(opponent_points);
            ((TextView)layout.findViewById(R.id.tv_player_points)).setText(player_points);
            ((TextView)layout.findViewById(R.id.tv_date)).setText(date);

            ((ImageView)layout.findViewById(R.id.iv_profile_pic)).setImageResource(android.R.drawable.btn_star_big_on);

            return layout;
        }
    }


    private class ReceiveFromServerTask extends AsyncTask<Void, Void, String>
    {
        private boolean cancelledFlag;
        public ReceiveFromServerTask()
        {
            cancelledFlag = false;
        }

        protected String doInBackground(Void... params)
        {
            String response = "";
            try
            {
                while(cancelledFlag == false)
                {
                    //Log.d("ReceiveFromServerTask", String.valueOf(mBoundService.socket.getInputStream().available()));
                    if(mBoundService.socket.getInputStream().available() > 4)
                    {
                        //ObjectInputStream in;
                        //in = new ObjectInputStream(mBoundService.socket.getInputStream());
                        response = (String)in.readObject();
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
                e.printStackTrace();
            }
            Log.d("ReceiveFromServerTask","b");
            return response;
        }

        protected void onPostExecute(String result) {
            Log.d("onPostExecute",result);

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

                        Intent intent = new Intent(DashboardActivity.this, GameActivity.class);
                        intent.putExtra("playerToPlay", params[1]);
                        startActivity(intent);

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
                        handler.post(myRunner);
                    }
                });
                // Set other dialog properties


                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }

        @Override
        protected void onCancelled() {
            cancelledFlag = true;
            Log.i("AsyncTask", "Cancelled.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


        doBindService();

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
                try {
                    in = new ObjectInputStream(mBoundService.socket.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                handler.post(myRunner);
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        doUnbindService();
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
