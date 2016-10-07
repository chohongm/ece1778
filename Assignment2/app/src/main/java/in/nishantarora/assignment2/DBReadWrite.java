package in.nishantarora.assignment2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;


class DBReadWrite extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;
    // Create Database
    private static final String SQL_CREATE = "create table " +
            MovieStore.TABLE_NAME + "( " +
            BaseColumns._ID + " integer primary key autoincrement, " +
            MovieStore.COL_TITLE + " text not null, " +
            MovieStore.COL_ACTOR + " text not null, " +
            MovieStore.COL_YEAR + " int not null );";
    private static final String SQL_DELETE = "DROP TABLE IF EXISTS " +
            MovieStore.TABLE_NAME;

    DBReadWrite(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE);
        onCreate(db);
    }

    final class MovieStore implements BaseColumns {
        static final String TABLE_NAME = "movies";
        static final String COL_TITLE = "title";
        static final String COL_ACTOR = "actor";
        static final String COL_YEAR = "year";
    }
}

