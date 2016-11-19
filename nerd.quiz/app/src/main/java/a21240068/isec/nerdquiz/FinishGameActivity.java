package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class FinishGameActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_game);

        int t_questions = 0;
        int ans_right = 0;
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
            }
        }
        else
        {
            t_questions = (int) savedInstanceState.getSerializable("t_questions");
            ans_right = (int) savedInstanceState.getSerializable("ans_right");
        }

        if(isOk)
        {
            TextView tv_score = (TextView) findViewById(R.id.tv_score);
            TextView tv_winner = (TextView) findViewById(R.id.tv_winner);

            tv_score.setText(ans_right + "/" + t_questions);
            tv_winner.setText("You won!");
        }
        else
        {
            Toast.makeText(this, "An error occurred!", Toast.LENGTH_LONG).show();
        }
    }

    public void clickButtonPlayAgain(View view)
    {
        //save data
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);

        finish();
    }

    public void clickButtonGoBack(View view)
    {
        //save data

        finish();
    }
}
