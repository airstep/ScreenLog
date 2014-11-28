package tgs.screen.toggle.log;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;

import java.util.Date;

import tgs.screen.toggle.log.db.DatabaseHelper;

public class UpdateService extends Service {
    public static final String ACTION_SCREEN_STATE = "tgs.screen.toggle.log.SCREEN_STATE";

    private DatabaseHelper dbHelper;
    private SQLiteDatabase dbWriter;
    private ScreenReceiver mReceiver;
    private long mPreviousRecordDateLong;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DatabaseHelper(this);
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(ACTION_SCREEN_STATE)) {
                    boolean screenState = intent.getBooleanExtra(ACTION_SCREEN_STATE, false);
                    writeToLog(screenState);
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        super.onDestroy();
    }

    // db part
    private void writeToLog(boolean toggle) {
        ContentValues row = new ContentValues();
        Date date = new Date();
        row.put(DatabaseHelper.COLUMN_DATE, date.getTime());
        row.put(DatabaseHelper.COLUMN_TOGGLE, toggle ? 1 : 0);
        if (toggle)
            row.put(DatabaseHelper.COLUMN_DIFF_OFF, getDateDiff(date));
        else
            row.put(DatabaseHelper.COLUMN_DIFF_ON, getDateDiff(date));
        insertToDB(row);
        mPreviousRecordDateLong = date.getTime();
    }

    private void insertToDB(ContentValues row) {
        if (dbWriter == null)
            dbWriter = dbHelper.getWritableDatabase();
        dbWriter.insert(DatabaseHelper.TABLE_SCREEN_STATES, null, row);
    }

    private long getDateDiff(Date date) {
        if (mPreviousRecordDateLong == 0) {
            SQLiteDatabase dbReader = dbHelper.getReadableDatabase();
            Cursor cursor = dbReader.rawQuery(String.format("SELECT * FROM %s ORDER BY %s DESC LIMIT 1;",
                    DatabaseHelper.TABLE_SCREEN_STATES, DatabaseHelper._ID), null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE);
                long previousDateLong = cursor.getLong(columnIndex);
                return date.getTime() - previousDateLong;
            }
        } else {
            return date.getTime() - mPreviousRecordDateLong;
        }
        return 0;
    }
}