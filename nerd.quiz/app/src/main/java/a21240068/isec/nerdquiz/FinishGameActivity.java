package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import a21240068.isec.nerdquiz.Database.GamesData;

public class FinishGameActivity extends Activity {

    private String opponent_name;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_game);

        int t_questions = 0;
        int ans_right = 0;
        int points = 0;
        int other_points = 0;
        opponent_name = "";
        boolean isOk = true;
        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if(extras == null)
            {
                isOk = false;
            }
            else
            {
                t_questions = extras.getInt("t_questions");
                ans_right = extras.getInt("ans_right");
                points = extras.getInt("p_points");
                other_points = extras.getInt("o_points");
                opponent_name = extras.getString("op_name");
                //
                saveGame(opponent_name, other_points, points);
            }
        }
        else
        {
            Toast.makeText(FinishGameActivity.this, getString(R.string.error_occurred),
                    Toast.LENGTH_LONG).show();
        }

        if(isOk)
        {
            TextView tv_score = (TextView) findViewById(R.id.tv_score);
            TextView tv_winner = (TextView) findViewById(R.id.tv_winner);

            tv_score.setText(ans_right + "/" + t_questions);
            if(other_points > points)
            {
                tv_winner.setText(getString(R.string.lose));
                ((ImageView)findViewById(R.id.img_real)).setImageResource(R.drawable.lose);
            }
            else
            {
                tv_winner.setText(getString(R.string.won));
                ((ImageView)findViewById(R.id.img_real)).setImageResource(R.drawable.win);
            }
        }
        else
        {
            Toast.makeText(this, getString(R.string.error_occurred), Toast.LENGTH_LONG).show();
        }
    }

    public void clickButtonPlayAgain(View view)
    {
        Intent intent = new Intent(this, NewGameActivity.class);
        intent.putExtra("reInvite", opponent_name);
        startActivity(intent);

        finish();
    }

    public void clickButtonGoBack(View view)
    {
        //
        finish();
    }

    private boolean saveGame(String opponent_name, int opponent_points, int player_points)
    {
        GamesData gdata = new GamesData(this);
        long id = gdata.add(opponent_name, opponent_points, player_points);
        return (id != -1);
    }
}
