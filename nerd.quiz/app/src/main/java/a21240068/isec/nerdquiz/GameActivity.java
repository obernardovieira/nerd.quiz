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
import android.widget.LinearLayout;
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
import java.net.UnknownHostException;
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
    private boolean                 isInvited;

    private Handler                 handler;
    private Runnable                myRunner;

    private int                     points;
    private Thread                  the_final_countdown;

    Socket                          player_socket;
    ObjectOutputStream              oostream;
    ObjectInputStream               oistream;

    private boolean                 theOtherAnswered;

    private boolean mIsBound;
    private SocketService mBoundService;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        total_questions_per_round   = 4;
        questions                   = new ArrayList<>();
        in_question                 = 0;
        handler                     = new Handler();
        answered_right              = 0;
        points                      = 0;

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
                isInvited = false;
            }
            else
            {
                opponent_name = extras.getString("playerToPlay");
                isInvited = extras.getBoolean("isInvited");
            }
        }
        /*else
        {
            opponent_name = (String) savedInstanceState.getSerializable("playerToPlay");
        }*/

        myRunner = new Runnable(){
            public void run() {
                new ReceiveFromPlayerTask().execute();
            }
        };
    }

    private void startCountdown()
    {
        the_final_countdown = new Thread()
        {
            public void run()
            {
                while (!isInterrupted())
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
                                if((!theOtherAnswered && isInvited) || theOtherAnswered)
                                {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                oostream.writeObject(Command.NEXT_QUEST);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).start();
                                }
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
        the_final_countdown.start();
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
        theOtherAnswered = false;
        startCountdown();
    }

    public void gameStart()
    {
        //
        /*TextView tv_wait = (TextView)findViewById(R.id.tv_wait);

        for(int i = 3; i > 0; i--)
        {
            tv_wait.setText(String.valueOf(i));
            try { Thread.sleep(1000); } catch (InterruptedException e) { }
        }

        LinearLayout layout_game = (LinearLayout)findViewById(R.id.layout_game);
        LinearLayout layout_wait = (LinearLayout)findViewById(R.id.layout_wait);

        layout_wait.setVisibility(View.GONE);
        layout_game.setVisibility(View.VISIBLE);*/

        showNewQuestion();
    }

    public void finishQuiz()
    {
        Intent intent = new Intent(this, FinishGameActivity.class);
        //passar dados
        intent.putExtra("t_questions", total_questions_per_round);
        intent.putExtra("ans_right", answered_right);
        startActivity(intent);

        try
        {
            oostream.close();
            oistream.close();
            player_socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        finish();
    }

    public void clickAnswerButton(View view)
    {
        Button btn = (Button) view.findViewById(view.getId());

        if(btn.getText().equals(questions.get(in_question).getRightAnswer()))
        {
            answered_right ++;
        }

        if(theOtherAnswered == true)
        {
            //
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        oostream.writeObject(Command.NEXT_QUEST);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            //next question
            if (++in_question < total_questions_per_round)
                showNewQuestion();
            else
                finishQuiz();
        }
        else
        {
            //
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        oostream.writeObject(Command.OTHER_ANSWERED);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            the_final_countdown.interrupt();
            //command telling that answered
        }


    }

    private class ReceiveFromPlayerTask extends AsyncTask<Void, Void, String>
    {
        protected String doInBackground(Void... pms)
        {
            String response = "";
            Log.d("reveice", "doInBackground");
            try {
                while (!isCancelled()) {
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

            //iniciou jogo
            if(result.equals(Command.RECEIVE_QUESTIONS))
            {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("Aceitou", "conectado.parte.1");

                            Integer total = (Integer) oistream.readObject();
                            for (int i = 0; i < total; i++) {
                                questions.add((GameQuestion) oistream.readObject());
                            }

                            Log.d("Aceitou", "conectado.parte.1");

                            handler.post(myRunner);

                            oostream.writeObject(Command.GAME_START);
                            oostream.flush();

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    gameStart();
                                }
                            });
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();


            }
            if(result.startsWith(Command.GAME_START))
            {
                gameStart();
            }
            else if(result.startsWith(Command.NEXT_QUEST))
            {
                if (++in_question < total_questions_per_round)
                    showNewQuestion();
                else
                    finishQuiz();
            }
            else if(result.equals(Command.OTHER_ANSWERED))
            {
                theOtherAnswered = true;
            }



            Log.d("reveice", "playerer");
        }

        @Override
        protected void onCancelled() {
            Log.i("AsyncTask", "Cancelled.");
        }
    }

    private class ReceiveFromServerTask extends AsyncTask<Void, Void, String>
    {
        protected String doInBackground(Void... pms)
        {
            String response = "";
            Log.d("reveice", "doInBackground");
            try {
                while (!isCancelled()) {
                    //Log.d("ReceiveFromServerTask", String.valueOf(mBoundService.socket.getInputStream().available()));
                    if (mBoundService.socket.getInputStream().available() > 4) {
                        response = (String) mBoundService.getObjectStreamIn().readObject();
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

        protected void onPostExecute(final String result) {
            Log.d("onPostExecute",result);

            //iniciou jogo

            if (result.startsWith(Command.NEW_GAME)) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            String[] params = result.split(" ");
                            opponent_name = params[1];
                            player_socket = new Socket(params[2], Integer.parseInt(params[3]));

                            Log.d("Aceitou", "conectado.parte.1");

                            oostream = new ObjectOutputStream(player_socket.getOutputStream());
                            oistream = new ObjectInputStream(player_socket.getInputStream());

                            handler.post(myRunner);

                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }



            Log.d("reveice", "playerer");
        }

        @Override
        protected void onCancelled() {
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

                if(isInvited)
                {
                    try {


                        ServerSocket game_socket = new ServerSocket(5009);
                        game_socket.setSoTimeout(5000);

                        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
                        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

                        mBoundService.sendMessage(Command.NEW_GAME + " " + opponent_name + " "
                                + ip + " " + 5009);

                        player_socket = game_socket.accept();

                        QuestionsData qdata = new QuestionsData(GameActivity.this);
                        questions = qdata.getRandomQuestions(total_questions_per_round);

                        oostream = new ObjectOutputStream(player_socket.getOutputStream());
                        oistream = new ObjectInputStream(player_socket.getInputStream());

                        handler.post(myRunner);

                        oostream.writeObject(Command.RECEIVE_QUESTIONS);
                        oostream.writeObject(questions.size());
                        for (GameQuestion q : questions)
                            oostream.writeObject(q);

                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    new ReceiveFromServerTask().execute();
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
