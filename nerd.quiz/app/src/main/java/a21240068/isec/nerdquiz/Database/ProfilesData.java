package a21240068.isec.nerdquiz.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import a21240068.isec.nerdquiz.Database.NerdQuizContract;
import a21240068.isec.nerdquiz.Database.NerdQuizDBHelper;


public class ProfilesData
{
    private NerdQuizDBHelper mDbHelper;

    public ProfilesData(Context context)
    {
        mDbHelper = new NerdQuizDBHelper(context);
    }

    public boolean add(String username, String photo)
    {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NerdQuizContract.ProfilesTable.COLUMN_NAME, username);
        values.put(NerdQuizContract.ProfilesTable.COLUMN_PHOTO, photo);
        long newRowId = db.insert(NerdQuizContract.ProfilesTable.TABLE_NAME, null, values);

        return (newRowId != -1);
    }

    public boolean remove(String username)
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        int _deleted;

        String selection = NerdQuizContract.ProfilesTable.COLUMN_NAME + " LIKE ?";
        String[] selectionArgs = { username };
        _deleted = db.delete(NerdQuizContract.ProfilesTable.TABLE_NAME, selection, selectionArgs);

        return (_deleted != 0);
    }

    public String getProfilePic(String username)
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                NerdQuizContract.ProfilesTable.COLUMN_PHOTO
        };

        String selection = NerdQuizContract.ProfilesTable.COLUMN_NAME + " = ?";
        String[] selectionArgs = { username };

        Cursor c = db.query(
                NerdQuizContract.ProfilesTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        c.moveToFirst();

        return c.getString(c.getColumnIndexOrThrow(NerdQuizContract.ProfilesTable.COLUMN_PHOTO));
    }
    public boolean updateProfilePic(String username, String profile_pic)
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(NerdQuizContract.ProfilesTable.COLUMN_PHOTO, profile_pic);
        //
        int id = db.update(NerdQuizContract.ProfilesTable.TABLE_NAME, values,
                NerdQuizContract.ProfilesTable.COLUMN_NAME + "= ?" + username, null);

        return (id != 0);
    }

    public boolean search(String username)
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                NerdQuizContract.ProfilesTable.COLUMN_NAME
        };

        String selection = NerdQuizContract.ProfilesTable.COLUMN_NAME + " = ?";
        String[] selectionArgs = { username };

        Cursor c = db.query(
                NerdQuizContract.ProfilesTable.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );


        return (c.getCount() > 0);
    }
}