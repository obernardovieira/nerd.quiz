package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
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
import java.util.Iterator;
import java.util.List;

import a21240068.isec.nerdquiz.Core.Command;
import a21240068.isec.nerdquiz.Core.NerdQuizApp;
import a21240068.isec.nerdquiz.Core.SocketService;
import a21240068.isec.nerdquiz.Database.ProfilesData;
import a21240068.isec.nerdquiz.Objects.Profile;

public class SearchPlayerActivity extends Activity {

    private MyAdapter adapter;
    private ArrayList<Profile> players_profile;
    private Handler mainHandler;
    private boolean mIsBound;
    private SocketService mBoundService;
    private Runnable fromServerRunner;
    private ReceiveFromServerTask fromServerTask;
    private ReceivePhotoFromServerTask task_photo;
    private EditText et_search;

    ProfilesData pdata;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_player);

        mainHandler = new Handler();
        players_profile = new ArrayList<>();

        fromServerRunner = new Runnable()
        {
            public void run()
            {
                fromServerTask = new ReceiveFromServerTask();
                fromServerTask.execute();
            }
        };

        pdata = new ProfilesData(SearchPlayerActivity.this);

        et_search = (EditText)findViewById(R.id.et_player_name);
        et_search.addTextChangedListener(searchPlayer);
    }

    TextWatcher searchPlayer = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            mBoundService.sendMessage(getResources().getString(R.string.command_search) + " " + s);
        }

        @Override
        public void afterTextChanged(Editable s)
        { }
    };

    public void updatePlayersList()
    {
        ListView lv = (ListView) findViewById(R.id.lv_players);
        adapter = new MyAdapter();
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                Intent output = new Intent();
                output.putExtra("name", players_profile.get(i).getName());
                setResult(RESULT_OK, output);
                finish();
            }
        });

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void addPlayerToView(String username, String profile_pic)
    {
        if(pdata.search(username))
        {
            players_profile.add(new Profile(username, pdata.getProfilePic(username)));
            adapter.notifyDataSetChanged();
        }

        File p_pic = new File(getApplicationContext().getFilesDir(), profile_pic);
        if(!p_pic.exists())
        {
            task_photo = new ReceivePhotoFromServerTask();
            task_photo.execute(username, profile_pic);
        }
    }

    public void removePlayerFromView(String name)
    {
        Iterator it = players_profile.iterator();
        Profile p;
        while(it.hasNext())
        {
            p = (Profile)it.next();
            if(p.getName().equals(name))
            {
                players_profile.remove(p);
                break;
            }
        }
    }

    class MyAdapter extends BaseAdapter implements ListAdapter
    {
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
        public View getView(int i, View view, ViewGroup viewGroup)
        {
            View layout;
            String username;
            String profile_pic;

            layout = getLayoutInflater().inflate(R.layout.player_profile_for_lv,null);
            username = players_profile.get(i).getName();
            profile_pic = players_profile.get(i).getProfilePicture();

            ((TextView)layout.findViewById(R.id.tv_username)).setText(username);
            File imgFile = new File(getApplicationContext().getFilesDir(), profile_pic);

            if(imgFile.exists())
            {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ((ImageView)layout.findViewById(R.id.iv_profile_pic)).setImageBitmap(myBitmap);
            }

            return layout;
        }
    }

    private class ReceivePhotoFromServerTask extends AsyncTask<String, Void, String>
    {
        protected String doInBackground(String... params)
        {
            String response = "";
            try
            {
                mBoundService.sendMessage(Command.PROFILE_PIC_DOWN +
                        " " + params[1]);

                while (!isCancelled()) {

                    if (mBoundService.socket.getInputStream().available() > 4) {

                        ObjectInputStream ins = mBoundService.getObjectStreamIn();
                        Integer size = (Integer)ins.readObject();
                        Integer received = 0;
                        BufferedInputStream in = new BufferedInputStream(
                                mBoundService.getStreamIn());

                        OutputStream out = new FileOutputStream(
                                new File(getApplicationContext().getFilesDir(), params[1]));

                        byte[] buf = new byte[8192];
                        int len = 0;
                        while ((len = in.read(buf)) != -1)
                        {
                            out.write(buf, 0, len);
                            if(received + len == size)
                                break;
                            else
                                received += len;
                        }
                        out.close();
                        //
                        response = params[0] + " " + params[1];
                        break;
                    }
                }
            }
            catch (IOException | ClassNotFoundException e)
            {
                response = "";
            }
            return response;
        }

        protected void onPostExecute(String result)
        {
            if(result.equals(""))
            {
                return;
            }

            String [] params = result.split(" ");

            if(!pdata.search(params[0]))
            {
                pdata.add(params[0], params[1]);
                players_profile.add(new Profile(params[0], params[1]));
                adapter.notifyDataSetChanged();
            }
            else if(!pdata.getProfilePic(params[0]).equals(params[1]))
            {
                if(pdata.updateProfilePic(params[0], params[1]))
                {
                    players_profile.add(new Profile(params[0], params[1]));
                    adapter.notifyDataSetChanged();
                }
            }
        }

        public void onCancelled()
        { }
    }

    private class ReceiveFromServerTask extends AsyncTask<Void, Void, String>
    {
        List<Profile> profiles = new ArrayList<>();
        protected String doInBackground(Void... params)
        {
            String response = "";
            Object obj;
            Profile profile;
            try
            {
                while (!isCancelled())
                {
                    if (mBoundService.socket.getInputStream().available() > 4)
                    {
                        ObjectInputStream ins = mBoundService.getObjectStreamIn();
                        response = (String) ins.readObject();
                        if(response.equals(Command.SEARCH))
                        {
                            obj = ins.readObject();
                            while(obj instanceof Profile)
                            {
                                profile = (Profile)obj;
                                profiles.add(profile);
                                obj = ins.readObject();
                            }
                            response = getResources().getString(R.string.type_new_search);
                        }
                        break;
                    }
                }
            }
            catch (IOException | ClassNotFoundException ignored)
            {
                response = "";
            }
            return response;
        }

        protected void onPostExecute(String result)
        {
            String [] params = result.split(" ");
            if(result.startsWith(Command.JOINED))
            {
                if(params[1].contains(et_search.getText().toString()))
                {
                    Toast.makeText(SearchPlayerActivity.this, params[1] +
                            " joined the game!", Toast.LENGTH_LONG).show();
                    addPlayerToView(params[1], params[2]);
                }
            }
            else if(result.startsWith(Command.LEAVED))
            {
                if(params[1].contains(et_search.getText().toString()))
                {
                    Toast.makeText(SearchPlayerActivity.this, params[1] +
                            " leaved the game!", Toast.LENGTH_LONG).show();
                    removePlayerFromView(params[1]);
                }
            }
            else if(result.equals(getResources().getString(R.string.type_new_search)))
            {
                for(Profile p : profiles)
                {
                    addPlayerToView(p.getName(), p.getProfilePicture());
                }
            }
            mainHandler.post(fromServerRunner);
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
            if(mBoundService.isConnected())
            {
                mainHandler.post(fromServerRunner);
                mBoundService.sendMessage(getResources().getString(R.string.command_search));
                updatePlayersList();
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
        bindService(new Intent(SearchPlayerActivity.this, SocketService.class),
                mConnection, Context.BIND_AUTO_CREATE);
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
            if(task_photo != null)
            {
                task_photo.cancel(true);
            }
        }
    }
}
