package tgs.screen.toggle.log.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DatabaseHelper extends SQLiteOpenHelper implements BaseColumns {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "screen_log.db";

    public static final String TABLE_SCREEN_STATES = "screen_states";

    public static final String COLUMN_DATE = "col_date";
    public static final String COLUMN_TOGGLE = "col_state";
    public static final String COLUMN_DIFF_ON = "col_diff_on";
    public static final String COLUMN_DIFF_OFF = "col_diff_off";

    private static final String DATABASE_CREATE_SCRIPT = "create table "
            + TABLE_SCREEN_STATES + " (" + BaseColumns._ID
            + " integer primary key autoincrement, " + COLUMN_DATE
            + " integer, " + COLUMN_TOGGLE + " integer, " + COLUMN_DIFF_ON + " integer, "
            + COLUMN_DIFF_OFF + " integer);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF IT EXIST " + TABLE_SCREEN_STATES);
        onCreate(db);
    }
}
