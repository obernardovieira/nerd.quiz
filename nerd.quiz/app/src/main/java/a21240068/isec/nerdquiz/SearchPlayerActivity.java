package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SearchPlayerActivity extends Activity {

    ArrayList<Profile> players_profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_player);

        players_profile = new ArrayList<>();
        getConnectedPlayers();
        updatePlayersList();
    }

    public void getConnectedPlayers()
    {
        getConnectedPlayers("a21240068.isec.nerdquiz");
    }

    public void getConnectedPlayers(String search_for_name)
    {
        if(search_for_name.equals("a21240068.isec.nerdquiz"))
        {
            //search random
            players_profile.add(new Profile("User1", "user"));
            players_profile.add(new Profile("User2", "user"));
            players_profile.add(new Profile("User3", "user"));
            players_profile.add(new Profile("User4", "user"));

        }
        else
        {
            //search by name
        }
    }

    public void updatePlayersList()
    {
        ListView lv = (ListView) findViewById(R.id.lv_players);
        lv.setAdapter(new MyAdapter());

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

    class MyAdapter extends BaseAdapter {

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

}
