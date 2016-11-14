package a21240068.isec.nerdquiz;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class GamesData
{

    private NerdQuizDBHelper mDbHelper;

    public GamesData(Context context)
    {
        mDbHelper = new NerdQuizDBHelper(context);
    }

    public long add(String opponent_name, int opponent_points, int player_points)
    {
        long newRowId;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String date = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss").format(new Date());

        ContentValues values = new ContentValues();
        values.put(NerdQuizContract.GamesTable.COLUMN_OPPONENT_NAME, opponent_name);
        values.put(NerdQuizContract.GamesTable.COLUMN_OPPONENT_POINTS, opponent_points);
        values.put(NerdQuizContract.GamesTable.COLUMN_POINTS, player_points);
        values.put(NerdQuizContract.GamesTable.COLUMN_DATE, date);
        newRowId = db.insert(NerdQuizContract.GamesTable.TABLE_NAME, null, values);

        return newRowId;
    }

    public boolean remove(int id_register)
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        int _deleted;

        String selection = NerdQuizContract.GamesTable._ID + " = ?";
        String[] selectionArgs = { Integer.toString(id_register) };
        _deleted = db.delete(NerdQuizContract.GamesTable.TABLE_NAME, selection, selectionArgs);

        return (_deleted != 0);
    }

    public void search(String opponent_name)
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                NerdQuizContract.GamesTable.COLUMN_OPPONENT_NAME,
                NerdQuizContract.GamesTable.COLUMN_OPPONENT_POINTS,
                NerdQuizContract.GamesTable.COLUMN_POINTS,
                NerdQuizContract.GamesTable.COLUMN_DATE
        };

        String selection = NerdQuizContract.GamesTable.COLUMN_OPPONENT_NAME + " = ?";
        String[] selectionArgs = { opponent_name };

        Cursor c = db.query(
                NerdQuizContract.GamesTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        //get opponent player profile by its name!
        //maybe there is more than one result! work on it!

        //return (c.getCount() > 0);
    }

    public void search(Date date)
    {
        //
    }

    public ArrayList<Game> listAll()
    {
        ArrayList<Game> games = new ArrayList<>();
        //
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                NerdQuizContract.GamesTable.COLUMN_OPPONENT_NAME,
                NerdQuizContract.GamesTable.COLUMN_OPPONENT_POINTS,
                NerdQuizContract.GamesTable.COLUMN_POINTS,
                NerdQuizContract.GamesTable.COLUMN_DATE
        };

        Cursor c = db.query(
                NerdQuizContract.GamesTable.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        if(c.getCount() > 0)
        {
            String opponent_name;
            int opponent_points;
            int player_points;
            String date;

            c.moveToFirst();

            do
            {
                opponent_name = c.getString(
                        c.getColumnIndexOrThrow(NerdQuizContract.GamesTable.COLUMN_OPPONENT_NAME)
                );
                opponent_points = c.getInt(
                        c.getColumnIndexOrThrow(NerdQuizContract.GamesTable.COLUMN_OPPONENT_POINTS)
                );
                player_points = c.getInt(
                        c.getColumnIndexOrThrow(NerdQuizContract.GamesTable.COLUMN_POINTS)
                );
                date = c.getString(
                        c.getColumnIndexOrThrow(NerdQuizContract.GamesTable.COLUMN_DATE)
                );
                games.add(new Game(opponent_name, opponent_points, player_points, date));

            } while(c.moveToNext());
        }
        //
        return games;
    }
}
