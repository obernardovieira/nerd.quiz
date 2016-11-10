package a21240068.isec.nerdquiz;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by bernardovieira on 06-11-2016.
 */

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
                NerdQuizContract.QuestionsTable.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
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

        Log.d("getAnswer", "Answer number " + id);

        String[] projection = {
                NerdQuizContract.QuestionsTable.COLUMN_RIGHT_ANSWER
        };

        String selection = NerdQuizContract.QuestionsTable._ID + " = ?";
        String[] selectionArgs = { Integer.toString(id) };

        Cursor c = db.query(
                NerdQuizContract.QuestionsTable.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        c.moveToFirst();
        answer = c.getString(
                c.getColumnIndexOrThrow(NerdQuizContract.QuestionsTable.COLUMN_RIGHT_ANSWER)
        );

        return answer;
    }

    public int countQuestions()
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                NerdQuizContract.QuestionsTable._ID,
        };

        Cursor c = db.query(
                NerdQuizContract.QuestionsTable.TABLE_NAME,                     // The table to query
                projection,                               // The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );
        return c.getCount();
    }

    public ArrayList<String> getRandomAnswersBySubject(int id_right_answer, String subject)
    {
        ArrayList<String> r_answers = new ArrayList<>();
        //
        int t_suject_questions = countQuestions() - 1;
        Random rand = new Random(System.nanoTime());

        r_answers.add(getAnswer(rand.nextInt(t_suject_questions) + 1));
        r_answers.add(getAnswer(rand.nextInt(t_suject_questions) + 1));
        //
        return r_answers;
    }

    public ArrayList<GameQuestion> getRandomQuestions(int n_questions)
    {
        ArrayList<GameQuestion>     questions           = new ArrayList<>();
        HashMap<String, String>     t_question;
        int                         t_suject_questions  = countQuestions() - 1;
        int                         value;
        ArrayList<String>           answers             = new ArrayList<>();
        Random                      rand                = new Random(System.nanoTime());
        GameQuestion                question;

        for(int i = 0; i < n_questions; i ++)
        {
            question        = new GameQuestion();
            value           = rand.nextInt(t_suject_questions) + 1;
            t_question      = getQuestion(value);

            question        .setRightAnswer(getAnswer(value));

            for (HashMap.Entry<String, String> entry : t_question.entrySet())
            {
                question    .setQuestion(entry.getValue());
                answers     .addAll(getRandomAnswersBySubject(value, entry.getKey()));
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

    public void updateQuestions()
    {
        add("httpcode", "What is the HTTP retrieved code when the request was successful?", "200");
        add("httpcode", "What is the HTTP code for not found?", "404");
        add("httpcode", "What is the HTTP code for bad gateway?", "502");
        add("httpcode", "What is the HTTP retrieved code when the request is unauthorized?", "401");
        add("httpcode", "What is the HTTP code for bad request?", "400");
        Log.d("message", "Introduzido! Agora com " + countQuestions());
    }
}
