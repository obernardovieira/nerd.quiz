package a21240068.isec.nerdquiz;

/**
 * Created by bernardovieira on 06-01-2017.
 */

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FragmentRightFinishGame extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_left_finish_game, container, false);
    }

    public void updateText(int ans_right, int t_questions, int points, int other_points)
    {
        TextView tv_score = (TextView) getView().findViewById(R.id.tv_score);
        TextView tv_winner = (TextView) getView().findViewById(R.id.tv_winner);

        tv_score.setText(ans_right + "/" + t_questions);
        if(other_points > points)
        {
            tv_winner.setText(getString(R.string.lose));
            ((ImageView)getView().findViewById(R.id.img_real)).setImageResource(R.drawable.lose);
        }
        else
        {
            tv_winner.setText(getString(R.string.won));
            ((ImageView)getView().findViewById(R.id.img_real)).setImageResource(R.drawable.win);
        }
    }
}
