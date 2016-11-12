package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class GameActivity extends Activity {

    private int                     time;
    private int                     in_question;
    private int                     total_questions_per_round;
    private ArrayList<GameQuestion> questions;

    private ProgressBar             pb_questions_left;
    private TextView                tv_time;
    private TextView                tv_question;
    private Button                  bt_answer_one;
    private Button                  bt_answer_two;
    private Button                  bt_answer_three;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        QuestionsData qdata         = new QuestionsData(this);
        total_questions_per_round   = 4;
        questions                   = qdata.getRandomQuestions(total_questions_per_round);
        in_question                 = 0;

        pb_questions_left = (ProgressBar)   findViewById(R.id.pb_questions_left);
        tv_time =           (TextView)      findViewById(R.id.tv_time);
        tv_question =       (TextView)      findViewById(R.id.tv_question);
        bt_answer_one =     (Button)        findViewById(R.id.bt_answer_one);
        bt_answer_two =     (Button)        findViewById(R.id.bt_answer_two);
        bt_answer_three =   (Button)        findViewById(R.id.bt_answer_three);

        //load questions
        //start game
        showNewQuestion();
    }

    public void showNewQuestion()
    {
        ArrayList<String> answers   = questions.get(in_question).getAnswers();
        time                        = 30;
        pb_questions_left           .setProgress(in_question);
        tv_time                     .setText(Integer.toString(time));
        //
        tv_question     .setText(questions.get(in_question).getQuestion());
        bt_answer_one   .setText(answers.get(0));
        bt_answer_two   .setText(answers.get(1));
        bt_answer_three .setText(answers.get(2));
        //
        //
        //start a thread time
    }

    public void finishQuiz()
    {
        finish();
    }

    public void clickAnswerButton(View view)
    {
        if(++in_question < total_questions_per_round)
            showNewQuestion();
        else
            finishQuiz();
    }

    public void countTime()
    {
        //
    }

    public void nextQuestion()
    {
        in_question ++;
    }
}
