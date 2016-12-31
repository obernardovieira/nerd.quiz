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
import android.widget.Toast;

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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import a21240068.isec.nerdquiz.Core.Command;
import a21240068.isec.nerdquiz.Core.SocketService;
import a21240068.isec.nerdquiz.Objects.DownloadQuestion;
import a21240068.isec.nerdquiz.Objects.GameQuestion;
import a21240068.isec.nerdquiz.Database.QuestionsData;

import static java.util.concurrent.TimeUnit.SECONDS;

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

    private ReceiveFromServerTask   server_task;
    private ReceiveFromPlayerTask   player_task;

    private int                     points;
    private int                     other_points;
    private ScheduledExecutorService    scheduler;

    Socket                          player_socket;
    ObjectOutputStream              oostream;
    ObjectInputStream               oistream;

    private boolean                 theOtherAnswered;
    private boolean                 IAnswered;

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
        other_points                = 0;

        pb_questions_left   = (ProgressBar)   findViewById(R.id.pb_questions_left);
        tv_time             = (TextView)      findViewById(R.id.tv_time);
        tv_question         = (TextView)      findViewById(R.id.tv_question);
        bt_answer_one       = (Button)        findViewById(R.id.bt_answer_one);
        bt_answer_two       = (Button)        findViewById(R.id.bt_answer_two);
        bt_answer_three     = (Button)        findViewById(R.id.bt_answer_three);

        pb_questions_left   .setMax(total_questions_per_round);

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

        myRunner = new Runnable()
        {
            public void run()
            {
                player_task = new ReceiveFromPlayerTask();
                player_task.execute();
            }
        };
    }

    private void startCountdown()
    {
        scheduler = Executors.newScheduledThreadPool(1);

        final Runnable beeper = new Runnable()
        {
            @Override
            public void run()
            {
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        tv_time.setText(Integer.toString(time));
                        time--;
                        if(time < 0)
                        {
                            scheduler.shutdownNow();
                            if((!theOtherAnswered && isInvited) || theOtherAnswered)
                            {
                                new Thread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        try
                                        {
                                            oostream.writeObject(getResources().getString(
                                                    R.string.command_next_quest));
                                        }
                                        catch (IOException e)
                                        {
                                            mBoundService.errorConnection();
                                        }
                                    }
                                }).start();
                                if(++in_question < total_questions_per_round)
                                {
                                    handler.post(myRunner);
                                    showNewQuestion();
                                }
                                else
                                {
                                    finishQuiz();
                                }
                            }
                        }
                    }
                });
            }
        };
        scheduler.scheduleAtFixedRate(beeper, 1, 1, SECONDS);
    }

    public void showNewQuestion()
    {
        ArrayList<String> answers = questions.get(in_question).getAnswers();
        time = 30;
        pb_questions_left.setProgress(in_question);
        tv_time.setText(Integer.toString(time));
        //
        tv_question.setText(questions.get(in_question).getQuestion());
        bt_answer_one.setText(answers.get(0));
        bt_answer_two.setText(answers.get(1));
        bt_answer_three.setText(answers.get(2));
        //
        theOtherAnswered = false;
        IAnswered = false;
        startCountdown();
    }

    public void gameStart()
    {
        //
        showNewQuestion();
    }

    public void finishQuiz()
    {
        Intent intent = new Intent(this, FinishGameActivity.class);
        intent.putExtra("t_questions", total_questions_per_round);
        intent.putExtra("ans_right", answered_right);
        intent.putExtra("p_points", points);
        intent.putExtra("o_points", other_points);
        intent.putExtra("op_name", opponent_name);
        startActivity(intent);

        try
        {
            oostream.close();
            oistream.close();
            player_socket.close();
        }
        catch (IOException e)
        {
           mBoundService.errorConnection();
        }
        finish();
    }

    public void clickAnswerButton(View view)
    {
        boolean right_ans = false;
        Button btn = (Button) view.findViewById(view.getId());
        if(btn.getText().equals(questions.get(in_question).getRightAnswer()))
        {
            answered_right ++;
            right_ans = true;
        }

        scheduler.shutdownNow();
        if(theOtherAnswered)
        {
            points ++;
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        oostream.writeObject(getResources().getString(
                                R.string.command_next_quest));
                    }
                    catch (IOException e)
                    {
                        mBoundService.errorConnection();
                    }
                }
            }).start();
            if (++in_question < total_questions_per_round)
            {
                handler.post(myRunner);
                showNewQuestion();
            }
            else
            {
                finishQuiz();
            }
        }
        else
        {
            points += 2;
            final boolean tsend = right_ans;
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        oostream.writeObject(getResources().getString(R.string.command_other_ans) +
                                " " + ((tsend) ? (1) : (0)));
                    }
                    catch (IOException e)
                    {
                        mBoundService.errorConnection();
                    }
                }
            }).start();
            IAnswered = true;
        }
    }

    private class ReceiveFromPlayerTask extends AsyncTask<Void, Void, String>
    {
        protected String doInBackground(Void... pms)
        {
            String response = "";
            try
            {
                while (!isCancelled())
                {
                    if (player_socket.getInputStream().available() > 4)
                    {
                        response = (String) oistream.readObject();
                        break;
                    }
                }
            }
            catch (ClassNotFoundException | IOException ignored)
            {
                response = "";
            }
            return response;
        }

        protected void onPostExecute(String result)
        {
            if(result.equals(getResources().getString(R.string.command_receive_ques)))
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Integer total = (Integer) oistream.readObject();
                            for (int i = 0; i < total; i++)
                            {
                                questions.add((GameQuestion) oistream.readObject());
                            }
                            handler.post(myRunner);
                            oostream.writeObject(getResources().getString(R.string.command_game_start));
                            oostream.flush();
                            handler.post(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    gameStart();
                                }
                            });
                        }
                        catch (IOException | ClassNotFoundException e)
                        {
                            mBoundService.errorConnection();
                        }
                    }
                }).start();


            }
            if(result.startsWith(getResources().getString(R.string.command_game_start)))
            {
                gameStart();
                handler.post(myRunner);
            }
            else if(result.startsWith(getResources().getString(R.string.command_next_quest)))
            {
                if (++in_question < total_questions_per_round)
                {
                    handler.post(myRunner);
                    showNewQuestion();
                }
                else
                {
                    finishQuiz();
                }
            }
            else if(result.startsWith(getResources().getString(R.string.command_other_ans)))
            {
                final String [] params = result.split(" ");
                if(IAnswered)
                {
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                oostream.writeObject(getResources().getString(
                                        R.string.command_next_quest));
                            }
                            catch (IOException e)
                            {
                                mBoundService.errorConnection();
                            }
                        }
                    }).start();
                    //
                    if (++in_question < total_questions_per_round)
                    {
                        handler.post(myRunner);
                        showNewQuestion();
                    }
                    else
                    {
                        finishQuiz();
                    }
                }
                else
                {
                    theOtherAnswered = true;
                    if(params[1].equals("1"))
                    {
                        other_points += 2;
                    }
                }
            }
            else if(result.equals(""))
            {
                mBoundService.errorConnection();
            }
        }

        @Override
        protected void onCancelled()
        { }
    }

    private class ReceiveFromServerTask extends AsyncTask<Void, Void, String>
    {
        protected String doInBackground(Void... pms)
        {
            String response = "";
            try
            {
                while (!isCancelled())
                {
                    if (mBoundService.socket.getInputStream().available() < 4)
                    {
                        continue;
                    }
                    response = (String) mBoundService.getObjectStreamIn().readObject();
                    if (response.startsWith(getResources().getString(R.string.command_new_game)))
                    {
                        String[] params = response.split(" ");
                        player_socket = new Socket(params[1], Integer.parseInt(params[2]));

                        oostream = new ObjectOutputStream(player_socket.getOutputStream());
                        oistream = new ObjectInputStream(player_socket.getInputStream());

                        handler.post(myRunner);
                    }
                    break;
                }
            }
            catch (IOException | ClassNotFoundException ignored)
            {
                response = getResources().getString(R.string.response_error);
            }
            return response;
        }

        protected void onPostExecute(String result)
        {
            if(result.equals(getResources().getString(R.string.response_error)))
            {
                mBoundService.errorConnection();
            }
        }

        @Override
        protected void onCancelled()
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
                if(isInvited)
                {
                    try
                    {
                        ServerSocket game_socket = new ServerSocket(5009);
                        game_socket.setSoTimeout(5000);

                        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
                        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

                        mBoundService.sendMessage(getResources().getString(R.string.command_new_game) +
                                " " + opponent_name + " " + ip + " " + 5009);

                        player_socket = game_socket.accept();

                        QuestionsData qdata = new QuestionsData(GameActivity.this);
                        questions = qdata.getRandomQuestions(total_questions_per_round);

                        oostream = new ObjectOutputStream(player_socket.getOutputStream());
                        oistream = new ObjectInputStream(player_socket.getInputStream());

                        handler.post(myRunner);

                        oostream.writeObject(getResources().getString(R.string.command_receive_ques));
                        oostream.writeObject(questions.size());
                        for (GameQuestion q : questions)
                            oostream.writeObject(q);

                    }
                    catch (SocketException e)
                    {
                        Toast.makeText(GameActivity.this, "TimeOut!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    catch (IOException e)
                    {
                        mBoundService.errorConnection();
                    }
                }
                else
                {
                    server_task = new ReceiveFromServerTask();
                    server_task.execute();
                }

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
        bindService(new Intent(GameActivity.this, SocketService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    private void doUnbindService()
    {
        if (mIsBound)
        {
            unbindService(mConnection);
            mIsBound = false;
            if(player_task != null)
            {
                player_task.cancel(true);
            }
            if(server_task != null)
            {
                server_task.cancel(true);
            }
        }
    }

}
