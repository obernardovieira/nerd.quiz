package a21240068.isec.nerdquiz.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import a21240068.isec.nerdquiz.Objects.DownloadQuestion;
import a21240068.isec.nerdquiz.Objects.GameQuestion;


public class QuestionsData {

    private NerdQuizDBHelper mDbHelper;

    public QuestionsData(Context context)
    {
        mDbHelper = new NerdQuizDBHelper(context);
    }

    public HashMap<String, String> getQuestion(int id)
    {

        HashMap<String, String> _question = new HashMap<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                NerdQuizContract.QuestionsTable.COLUMN_QUESTION_SUBJECT,
                NerdQuizContract.QuestionsTable.COLUMN_QUESTION
        };

        String selection = NerdQuizContract.QuestionsTable._ID + " = ?";
        String[] selectionArgs = { Integer.toString(id) };

        Cursor c = db.query(
                NerdQuizContract.QuestionsTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        c.moveToFirst();
        String subject = c.getString(
                c.getColumnIndexOrThrow(NerdQuizContract.QuestionsTable.COLUMN_QUESTION_SUBJECT)
        );
        String question = c.getString(
                c.getColumnIndexOrThrow(NerdQuizContract.QuestionsTable.COLUMN_QUESTION)
        );
        _question.put(subject, question);

        return _question;
    }

    public String getAnswer(int id)
    {
        String answer;
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                NerdQuizContract.QuestionsTable.COLUMN_RIGHT_ANSWER
        };

        String selection = NerdQuizContract.QuestionsTable._ID + " = ?";
        String[] selectionArgs = { Integer.toString(id) };

        Cursor c = db.query(
                NerdQuizContract.QuestionsTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        c.moveToFirst();
        answer = c.getString(
                c.getColumnIndexOrThrow(NerdQuizContract.QuestionsTable.COLUMN_RIGHT_ANSWER)
        );

        return answer;
    }

    public int countQuestions(String subject)
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                NerdQuizContract.QuestionsTable._ID
        };
        Cursor c;

        if(subject.equals("NONE"))
        {
            c = db.query(
                    NerdQuizContract.QuestionsTable.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
        else
        {
            String selection = NerdQuizContract.QuestionsTable.COLUMN_QUESTION_SUBJECT + " = ?";
            String[] selectionArgs = {subject};

            c = db.query(
                    NerdQuizContract.QuestionsTable.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
        }
        return c.getCount();
    }

    public int countQuestions()
    {
        return countQuestions("NONE");
    }

    public ArrayList<String> getAnswersBySubject(String subject)
    {
        ArrayList<String> output = new ArrayList<>();
        String answer;
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                NerdQuizContract.QuestionsTable.COLUMN_RIGHT_ANSWER
        };

        String selection = NerdQuizContract.QuestionsTable.COLUMN_QUESTION_SUBJECT + " = ?";
        String[] selectionArgs = { subject };

        Cursor c = db.query(
                NerdQuizContract.QuestionsTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        c.moveToFirst();
        answer = c.getString(
                c.getColumnIndexOrThrow(NerdQuizContract.QuestionsTable.COLUMN_RIGHT_ANSWER)
        );
        output.add(answer);

        while(c.moveToNext())
        {
            answer = c.getString(
                    c.getColumnIndexOrThrow(NerdQuizContract.QuestionsTable.COLUMN_RIGHT_ANSWER)
            );
            output.add(answer);
        }

        return output;
    }

    public ArrayList<String> getRandomAnswersBySubject(String subject, String right_answer)
    {
        ArrayList<String> r_answers = new ArrayList<>();
        ArrayList<String> all_answers;
        //
        String new_answer;
        int t_suject_questions;
        int i, rand_number;
        Random rand = new Random(System.nanoTime());
        all_answers = getAnswersBySubject(subject);
        t_suject_questions = all_answers.size() - 1;

        for(i = 0; i < 2; i++)
        {
            do
            {
                rand_number = rand.nextInt(t_suject_questions) + 1;
                new_answer = all_answers.get(rand_number);

            }while(r_answers.contains(new_answer) || right_answer.equals(new_answer));
            r_answers.add(new_answer);
        }

        return r_answers;
    }

    public ArrayList<GameQuestion> getRandomQuestions(int n_questions)
    {
        ArrayList<GameQuestion>     questions           = new ArrayList<>();
        ArrayList<Integer>          questions_list      = new ArrayList<>();
        HashMap<String, String>     t_question;
        int                         t_suject_questions  = countQuestions() - 1;
        int                         value;
        ArrayList<String>           answers;
        Random                      rand                = new Random(System.nanoTime());
        GameQuestion                question;

        for(int i = 0; i < n_questions; i ++)
        {
            answers         = new ArrayList<>();
            question        = new GameQuestion();
            do
            {
                value       = rand.nextInt(t_suject_questions) + 1;
            }while(questions_list.contains(value));

            t_question      = getQuestion(value);
            questions_list  .add(value);
            question        .setRightAnswer(getAnswer(value));

            for (HashMap.Entry<String, String> entry : t_question.entrySet())
            {
                question    .setQuestion(entry.getValue());
                answers     .addAll(getRandomAnswersBySubject(entry.getKey(), getAnswer(value)));
                Log.d("new_question", Integer.toString(i));
                Log.d("setQuestion", entry.getValue());
                Log.d("setRightAnswer", getAnswer(value));
                Log.d("answers", answers.get(0));
                Log.d("answers", answers.get(1));
            }

            answers         .add(getAnswer(value));
            Collections     .shuffle(answers, rand);
            question        .addAnswers(answers);
            questions       .add(question);
        }
        //
        return questions;
    }

    public boolean add(String subject, String question, String right_answer)
    {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NerdQuizContract.QuestionsTable.COLUMN_QUESTION_SUBJECT, subject);
        values.put(NerdQuizContract.QuestionsTable.COLUMN_QUESTION, question);
        values.put(NerdQuizContract.QuestionsTable.COLUMN_RIGHT_ANSWER, right_answer);
        long newRowId = db.insert(NerdQuizContract.QuestionsTable.TABLE_NAME, null, values);

        return (newRowId != -1);
    }

    public void updateQuestions(ArrayList<DownloadQuestion> questions, int version)
    {
        for(DownloadQuestion question : questions)
        {
            add(question.getType(), question.getQuestion(), question.getRanswer());
        }
        /*add("httpcode", "What is the HTTP retrieved code when the request was successful?", "200");
        add("httpcode", "What is the HTTP code for not found?", "404");
        add("httpcode", "What is the HTTP code for bad gateway?", "502");
        add("httpcode", "What is the HTTP retrieved code when the request is unauthorized?", "401");
        add("httpcode", "What is the HTTP code for bad request?", "400");

        add("langcreator", "Who designed C language?", "Denis Rechie");
        add("langcreator", "Who designed Ruby language?", "Yukihiro Matsumoto");
        add("langcreator", "Who designed C++ language?", "Bjarne Stroustrup");
        add("langcreator", "Who designed Java language?", "James Gosling");
        add("langcreator", "Who designed Python language?", "Guido van Rossum");*/

        Log.d("message", "Introduzido! Agora com " + countQuestions() + ". Na versao " + version);
    }
}
