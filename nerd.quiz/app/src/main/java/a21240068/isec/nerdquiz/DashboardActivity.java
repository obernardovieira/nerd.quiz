package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class DashboardActivity extends Activity {

    private ArrayList<Game> games;
    Handler mainHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        GamesData gdata = new GamesData(this);
        games = gdata.listAll();

        ListView lv = (ListView) findViewById(R.id.lv_history);
        lv.setAdapter(new MyGamesHistoryAdapter());

        mainHandler = new Handler();
    }

    @Override
    public void onPause() {
        super.onPause();

        //
    }

    @Override
    public void onResume() {
        super.onResume();

        //
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
}
