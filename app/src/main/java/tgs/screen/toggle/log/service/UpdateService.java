package tgs.screen.toggle.log.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import tgs.screen.toggle.log.model.ScreenState;
import tgs.screen.toggle.log.receiver.ScreenReceiver;

public class UpdateService extends Service {
    public static final String ACTION_SCREEN_STATE = "tgs.screen.toggle.log.SCREEN_STATE";

    private ScreenReceiver mReceiver;
    private long mPreviousRecordDateLong;
    private Realm db;

    @Override
    public void onCreate() {
        super.onCreate();
        db = Realm.getDefaultInstance();
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
        db.close();
        super.onDestroy();
    }

    private void writeToLog(final boolean toggle) {
        ScreenState state = new ScreenState();
        state.date = new Date();
        state.toggle = toggle;
        if (toggle)
            state.diffOff = getDateDiff(state.date);
        else state.diffOn = getDateDiff(state.date);
        mPreviousRecordDateLong = state.date.getTime();

        db.beginTransaction();
        db.insert(state);
        db.commitTransaction();
    }

    private long getDateDiff(Date date) {
        if (mPreviousRecordDateLong == 0) {
            RealmResults<ScreenState> stateList = db.where(ScreenState.class)
                    .findAllSorted("date", Sort.DESCENDING);
            if (stateList != null && stateList.size() > 0) {
                ScreenState screenState = stateList.first();
                return date.getTime() - screenState.date.getTime();
            }
        }
        else {
            return date.getTime() - mPreviousRecordDateLong;
        }
        return 0;
    }
}