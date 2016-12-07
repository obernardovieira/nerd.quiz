package a21240068.isec.nerdquiz.Database;

import android.provider.BaseColumns;

/**
 * Created by bernardovieira on 05-11-2016.
 */

public final class NerdQuizContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private NerdQuizContract() {}

    /* Inner class that defines the table contents */
    public static class ProfilesTable implements BaseColumns {
        public static final String TABLE_NAME = "profiles";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PHOTO = "photo";
    }

    public static class GamesTable implements BaseColumns {
        public static final String TABLE_NAME = "games";
        public static final String COLUMN_OPPONENT_NAME = "opname";
        public static final String COLUMN_OPPONENT_POINTS = "oppoints";
        public static final String COLUMN_POINTS = "points";
        public static final String COLUMN_DATE = "date";
    }

    public static class QuestionsTable implements BaseColumns {
        public static final String TABLE_NAME = "questions";
        public static final String COLUMN_QUESTION = "question";
        public static final String COLUMN_RIGHT_ANSWER = "ranswer";
        public static final String COLUMN_QUESTION_SUBJECT = "subject";
    }
}

