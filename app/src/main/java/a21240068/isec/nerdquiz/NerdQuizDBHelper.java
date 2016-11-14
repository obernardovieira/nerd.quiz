package a21240068.isec.nerdquiz;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import a21240068.isec.nerdquiz.NerdQuizContract.*;

/**
 * Created by bernardovieira on 05-11-2016.
 */

public class NerdQuizDBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "NerdQuiz.db";
    //
    private static final String SQL_CREATE_PROFILES_TABLES =
            "CREATE TABLE " + ProfilesTable.TABLE_NAME + " (" +
                    ProfilesTable._ID + " INTEGER PRIMARY KEY," +
                    ProfilesTable.COLUMN_NAME + " TEXT, " +
                    ProfilesTable.COLUMN_PHOTO + " TEXT)";

    private static final String SQL_DELETE_PROFILES_TABLES =
            "DROP TABLE IF EXISTS " + ProfilesTable.TABLE_NAME;
    //
    private static final String SQL_CREATE_GAMES_TABLES =
            "CREATE TABLE " + GamesTable.TABLE_NAME + " (" +
                    GamesTable._ID + " INTEGER PRIMARY KEY," +
                    GamesTable.COLUMN_OPPONENT_NAME + " TEXT, " +
                    GamesTable.COLUMN_OPPONENT_POINTS + " INT, " +
                    GamesTable.COLUMN_POINTS + " INT, " +
                    GamesTable.COLUMN_DATE + " TEXT)";

    private static final String SQL_DELETE_GAMES_TABLES =
            "DROP TABLE IF EXISTS " + GamesTable.TABLE_NAME;
    //
    private static final String SQL_CREATE_QUESTIONS_TABLES =
            "CREATE TABLE " + QuestionsTable.TABLE_NAME + " (" +
                    QuestionsTable._ID + " INTEGER PRIMARY KEY," +
                    QuestionsTable.COLUMN_QUESTION + " TEXT, " +
                    QuestionsTable.COLUMN_RIGHT_ANSWER + " TEXT, " +
                    QuestionsTable.COLUMN_QUESTION_SUBJECT + " TEXT)";

    private static final String SQL_DELETE_QUESTIONS_TABLES =
            "DROP TABLE IF EXISTS " + QuestionsTable.TABLE_NAME;

    public NerdQuizDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PROFILES_TABLES);
        db.execSQL(SQL_CREATE_GAMES_TABLES);
        db.execSQL(SQL_CREATE_QUESTIONS_TABLES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_PROFILES_TABLES);
        db.execSQL(SQL_DELETE_GAMES_TABLES);
        db.execSQL(SQL_DELETE_QUESTIONS_TABLES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}