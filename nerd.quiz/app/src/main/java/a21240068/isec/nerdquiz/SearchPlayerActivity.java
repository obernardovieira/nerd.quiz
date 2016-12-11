package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import a21240068.isec.nerdquiz.Core.Command;
import a21240068.isec.nerdquiz.Core.NerdQuizApp;
import a21240068.isec.nerdquiz.Core.SocketService;
import a21240068.isec.nerdquiz.Objects.Profile;

public class SearchPlayerActivity extends Activity {

    private MyAdapter adapter;
    ArrayList<Profile> players_profile;
    Handler mainHandler;
    private boolean mIsBound;
    private SocketService mBoundService;

    private Runnable fromServerRunner;

    ObjectInputStream in;
    private ReceiveFromServerTask fromServerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_player);

        mainHandler = new Handler();
        players_profile = new ArrayList<>();
        getConnectedPlayers();
        updatePlayersList();

        fromServerRunner = new Runnable(){
            public void run() {
                fromServerTask = new ReceiveFromServerTask();
                fromServerTask.execute();
            }
        };
    }

    public void getConnectedPlayers()
    {
        getConnectedPlayers("a21240068.isec.nerdquiz");
    }

    public void getConnectedPlayers(final String search_for_name)
    {
        if(search_for_name.equals("a21240068.isec.nerdquiz"))
        {
            /*players_profile.add(new Profile("User1", "user"));
            players_profile.add(new Profile("User2", "user"));
            players_profile.add(new Profile("User3", "user"));
            players_profile.add(new Profile("User4", "user"));*/
        }
        else
        {
            //search by name
        }
    }


    private String saveToInternalStorage(Bitmap bitmapImage)
    {
        ContextWrapper  cw          = new ContextWrapper(getApplicationContext());
        File            directory   = cw.getDir("directoryName", Context.MODE_PRIVATE);
        String          fileName    = Long.toString(System.currentTimeMillis()) + ".jpg";
        File            myPath      = new File(directory, fileName);

        try
        {
            FileOutputStream fos;
            fos = new FileOutputStream(myPath);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }

    public void updatePlayersList()
    {
        ListView lv = (ListView) findViewById(R.id.lv_players);
        adapter = new MyAdapter();
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                //add player to game
                Intent output = new Intent();
                output.putExtra("name", players_profile.get(i).getName());
                setResult(RESULT_OK, output);
                finish();
            }
        });

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void addPlayerToView(String name)
    {
        //
        players_profile.add(new Profile(name, ""));
        adapter.notifyDataSetChanged();
    }

    public void removePlayerFromView(String name)
    {
        //
    }

    class MyAdapter extends BaseAdapter implements ListAdapter {

        @Override
        public int getCount() {
            return players_profile.size();
        }

        @Override
        public Object getItem(int i) {
            return players_profile.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View layout;
            String username;
            //Drawable profile_pic;

            layout = getLayoutInflater().inflate(R.layout.player_profile_for_lv,null);
            username = (String) players_profile.get(i).getName();
            //profile_pic = (Drawable) players_profile.get(i).getProfilePicture();

            ((TextView)layout.findViewById(R.id.tv_username)).setText(username);
            ((ImageView)layout.findViewById(R.id.iv_profile_pic)).setImageResource(R.drawable.user);

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

            try {
                while (cancelledFlag == false) {
                    //Log.d("ReceiveFromServerTask", String.valueOf(mBoundService.socket.getInputStream().available()));
                    if (mBoundService.socket.getInputStream().available() > 4) {
                        //
                        response = (String) in.readObject();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Log.d("ReceiveFromServerTask","b");
            return response;
        }

        protected void onPostExecute(String result) {
            Log.d("onPostExecute",result);

            String [] params = result.split(" ");
            if(result.startsWith(Command.JOINED))
            {
                //
                Toast.makeText(SearchPlayerActivity.this, params[1] + " joined the game!", Toast.LENGTH_LONG).show();
                addPlayerToView(params[1]);
            }
            else if(result.startsWith(Command.LEAVED))
            {
                //
                Toast.makeText(SearchPlayerActivity.this, params[1] + " leaved the game!", Toast.LENGTH_LONG).show();
                removePlayerFromView(params[1]);
            }
            mainHandler.post(fromServerRunner);
        }

        public void fuckingStop()
        {
            cancelledFlag = true;
            Log.d("fuckingStop", "Cancelled.");
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

                /*try {
                    in = new ObjectInputStream(mBoundService.socket.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                in = mBoundService.getStreamIn();
                mainHandler.post(fromServerRunner);
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        doUnbindService();

        fromServerTask.cancel(true);
        fromServerTask.fuckingStop();
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
        bindService(new Intent(SearchPlayerActivity.this, SocketService.class), mConnection, Context.BIND_AUTO_CREATE);
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
