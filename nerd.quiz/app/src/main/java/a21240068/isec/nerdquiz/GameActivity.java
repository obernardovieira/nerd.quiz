package a21240068.isec.nerdquiz;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import a21240068.isec.nerdquiz.Core.Command;
import a21240068.isec.nerdquiz.Core.SocketService;
import a21240068.isec.nerdquiz.Objects.DownloadQuestion;
import a21240068.isec.nerdquiz.Objects.GameQuestion;
import a21240068.isec.nerdquiz.Database.QuestionsData;

public class GameActivity extends Activity {

    private int                     time;
    private int                     in_question;
    private int                     total_questions_per_round;
    private ArrayList<GameQuestion> questions;
    private int                     answered_right;

    private ProgressBar             pb_questions_left;
    private TextView                tv_time;
    private TextView                tv_question;
    private Button                  bt_answer_one;
    private Button                  bt_answer_two;
    private Button                  bt_answer_three;

    private String                  opponent_name;

    private Handler                 handler;
    private Runnable                myRunner;

    Socket                          player_socket;
    ObjectOutputStream              oostream;
    ObjectInputStream               oistream;

    private boolean mIsBound;
    private SocketService mBoundService;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        QuestionsData qdata         = new QuestionsData(this);
        total_questions_per_round   = 4;
        questions                   = qdata.getRandomQuestions(total_questions_per_round);
        in_question                 = 0;
        handler                     = new Handler();
        answered_right              = 0;

        pb_questions_left   = (ProgressBar)   findViewById(R.id.pb_questions_left);
        tv_time             = (TextView)      findViewById(R.id.tv_time);
        tv_question         = (TextView)      findViewById(R.id.tv_question);
        bt_answer_one       = (Button)        findViewById(R.id.bt_answer_one);
        bt_answer_two       = (Button)        findViewById(R.id.bt_answer_two);
        bt_answer_three     = (Button)        findViewById(R.id.bt_answer_three);

        pb_questions_left   .setMax(total_questions_per_round);
        //showNewQuestion();

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            if(extras == null)
            {
                opponent_name = null;
            }
            else
            {
                opponent_name = extras.getString("playerToPlay");
            }
        }
        else
        {
            opponent_name = (String) savedInstanceState.getSerializable("playerToPlay");
        }

        myRunner = new Runnable(){
            public void run() {
                new ReceiveFromPlayerTask().execute();
            }
        };
    }

    private void startCountdown()
    {
        Thread t = new Thread()
        {
            private int order = in_question;
            public void run()
            {
                while (order == in_question)
                {
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable()
                    {
                        public void run()
                        {
                            tv_time.setText(Integer.toString(time));
                            time--;
                            if(time < 0)
                            {
                                if(++in_question < total_questions_per_round)
                                    showNewQuestion();
                                else
                                    finishQuiz();

                            }
                        }
                    });
                }
            }
        };
        t.start();
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
        startCountdown();
    }

    public void finishQuiz()
    {
        Intent intent = new Intent(this, FinishGameActivity.class);
        //passar dados
        intent.putExtra("t_questions", total_questions_per_round);
        intent.putExtra("ans_right", answered_right);
        startActivity(intent);

        finish();
    }

    public void clickAnswerButton(View view)
    {
        Button btn = (Button) view.findViewById(view.getId());
        if(btn.getText().equals(questions.get(in_question).getRightAnswer()))
            answered_right ++;

        if(++in_question < total_questions_per_round)
            showNewQuestion();
        else
            finishQuiz();
    }

    private class ReceiveFromPlayerTask extends AsyncTask<Void, Void, String>
    {
        private boolean cancelledFlag;
        public ReceiveFromPlayerTask()
        {
            cancelledFlag = false;
        }

        protected String doInBackground(Void... params)
        {
            String response = "";
            Log.d("reveice", "doInBackground");
            try {
                while (cancelledFlag == false) {
                    //Log.d("ReceiveFromServerTask", String.valueOf(mBoundService.socket.getInputStream().available()));
                    if (player_socket.getInputStream().available() > 4) {
                        response = (String) oistream.readObject();
                        Log.d("reveice", response);
                        break;
                    }

                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("ReceiveFromServerTask","b");

            return response;
        }

        protected void onPostExecute(String result) {
            Log.d("onPostExecute",result);
            if(result.equals(Command.NEXT_QUEST))
            {
                showNewQuestion();
                Log.d("reveice", "playerer");
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

                try
                {


                    ServerSocket game_socket = new ServerSocket(5009);
                    game_socket.setSoTimeout(5000);

                    WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
                    String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

                    mBoundService.sendMessage(Command.ACCEPT_INV + " " + opponent_name + " "
                        + ip + " " + 5009);

                    player_socket = game_socket.accept();

                    oostream = new ObjectOutputStream(player_socket.getOutputStream());
                    oistream = new ObjectInputStream(player_socket.getInputStream());

                    handler.post(myRunner);

                    oostream.writeObject(questions.size());
                    for(GameQuestion q : questions)
                        oostream.writeObject(q);

                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        bindService(new Intent(GameActivity.this, SocketService.class), mConnection, Context.BIND_AUTO_CREATE);
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
