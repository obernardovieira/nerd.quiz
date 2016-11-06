package a21240068.isec.nerdquiz;

import android.content.Context;

/**
 * Created by bernardovieira on 06-11-2016.
 */

public class GamesData {

    private NerdQuizDBHelper mDbHelper;

    public GamesData(Context context)
    {
        mDbHelper = new NerdQuizDBHelper(context);
    }

    public boolean add(String username)
    {
        return true;
    }

    public boolean remove(String username)
    {
        return true;
    }

    public boolean search(String username)
    {
        return false;
    }
}
