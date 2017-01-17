package tgs.screen.toggle.log;

import android.app.ActivityManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.ArcProgress;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import tgs.screen.toggle.log.adapter.HistoryAdapter;
import tgs.screen.toggle.log.model.ScreenState;
import tgs.screen.toggle.log.service.UpdateService;

public class MainActivity extends AppCompatActivity {

    private static final String SETTINGS_HOUR = "current_hour_limit";
    private static final String SETTINGS_MINUTE = "current_minute_limit";
    private ArcProgress progress;
    private long mTimeLimit;
    private TextView tvTotalOn;
    private TextView tvTotalAllOn;
    private TextView tvLimit;
    private Realm db;
    private HistoryAdapter mAdapter;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTimeLimit = getTimeLimit();
        db = Realm.getDefaultInstance();

        lv = (ListView) findViewById(R.id.listView);

        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup)inflater.inflate(R.layout.listview_head_main, lv, false);
        lv.addHeaderView(header, null, false);
        ViewGroup footer = (ViewGroup)inflater.inflate(R.layout.listview_foot_main, lv, false);
        lv.addHeaderView(footer, null, false);
        mAdapter = new HistoryAdapter(this, R.layout.row_history_item);
        lv.setAdapter(mAdapter);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.app_name_wide);
        startService(new Intent(this, UpdateService.class));
        progress = (ArcProgress) header.findViewById(R.id.arc_progress);
        tvTotalOn = (TextView) header.findViewById(R.id.tvTotalON);
        tvTotalAllOn = (TextView) header.findViewById(R.id.tvTotalAllON);
        tvLimit = (TextView) header.findViewById(R.id.tvLimit);
        tvLimit.setText(toHumanStringDurationLimit(mTimeLimit));

        progress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimeDialog();
            }
        });
    }

    private void showTimeDialog() {
        TimePickerDialog.OnTimeSetListener event = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (hourOfDay == 0 && minute == 0) {
                    showMessage(R.string.time_zero_config_title);
                } else {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt(SETTINGS_HOUR, hourOfDay);
                    editor.putInt(SETTINGS_MINUTE, minute);
                    editor.apply();
                    mTimeLimit = calculateLimit(hourOfDay, minute);
                    tvLimit.setText(toHumanStringDurationLimit(mTimeLimit));
                    updatePercent();
                }
            }
        };
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        int hours = preferences.getInt(SETTINGS_HOUR, 1);
        int minutes = preferences.getInt(SETTINGS_MINUTE, 30);
        TimePickerDialog dlg = new TimePickerDialog(this, event, hours, minutes, true);
        dlg.setTitle(R.string.time_config_title);
        dlg.show();
    }

    private long getTimeLimit() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        int hours = preferences.getInt(SETTINGS_HOUR, 1);
        int minutes = preferences.getInt(SETTINGS_MINUTE, 30);
        return calculateLimit(hours, minutes);
    }

    public String toHumanStringDurationLimit(long duration) {
        long durationInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        int minutes = (int) ((durationInSeconds / 60) % 60);
        int hours = (int) (((durationInSeconds / 60) / 60) % 24);

        String result = String.format(Locale.getDefault(), "%d %s" , minutes, getString(R.string.minutes));

        if (hours > 0)
            result = String.format(Locale.getDefault(), "%d%s %02d%s", hours, getString(R.string.hours_short), minutes, getString(R.string.minutes_short));

        return result;
    }

    public String toHumanStringDuration(long duration) {
        long durationInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);

        long seconds = durationInSeconds % 60;
        int minutes = (int) ((durationInSeconds / 60) % 60);
        int hours = (int) (((durationInSeconds / 60) / 60) % 24);
        long days = TimeUnit.MILLISECONDS.toDays(duration);

        String result = String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);

        if (hours > 0)
            result = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);

        if (days > 0)
            result = String.format(Locale.getDefault(), "%d %s %d:%02d:%02d", days, getString(R.string.days), hours, minutes, seconds);
        return result;
    }

    private long calculateLimit(int hours, int minutes) {
        return TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(minutes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHistory();
        updatePercent();
        updateAllTime();
    }

    private Date getMidnight() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND,0);
        return cal.getTime();
    }

    private void updatePercent() {
        long total = db.where(ScreenState.class).greaterThan("date", getMidnight())
                .sum("diffOn").longValue();

        tvTotalOn.setText(toHumanStringDuration(total));

        int percent = Math.round((total / (float) mTimeLimit) * 100);
        progress.setMax(((percent / 100) == 0) ? 100 : (percent / 100 + 1) * 100);
        progress.setProgress(percent);
        setProgressColor(percent);
    }

    private void updateAllTime() {
        long total = db.where(ScreenState.class).sum("diffOn").longValue();
        tvTotalAllOn.setText(toHumanStringDuration(total));
    }

    private void setProgressColor(int percent) {
        if (percent >= 0 && percent < 60) {
            progress.setFinishedStrokeColor(getResources().getColor(R.color.progress_green));
        } else if (percent >= 60 && percent <= 90) {
            progress.setFinishedStrokeColor(getResources().getColor(R.color.progress_orange));
        } else if (percent > 90) {
            progress.setFinishedStrokeColor(getResources().getColor(R.color.progress_red));
        }
    }

    private void updateHistory() {
        RealmResults<ScreenState> historyItems = db.where(ScreenState.class)
                .greaterThan("date", getMidnight())
                .findAllSorted("date", Sort.DESCENDING);

        if (mAdapter != null)
            mAdapter.clear();

        if (historyItems.size() == 0) {
            showMessage(R.string.toggle_screen);
        } else {
            for (ScreenState item : historyItems)
                mAdapter.add(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem startItem = menu.findItem(R.id.action_start_service);
        MenuItem stopItem = menu.findItem(R.id.action_stop_service);
        if (isMyServiceRunning(UpdateService.class)){
            startItem.setVisible(false);
            stopItem.setVisible(true);
        } else {
            startItem.setVisible(true);
            stopItem.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_start_service) {
            startService(new Intent(this, UpdateService.class));
            supportInvalidateOptionsMenu();
            showMessage(R.string.service_was_started);
            return true;
        }
        if (id == R.id.action_stop_service) {
            stopService(new Intent(this, UpdateService.class));
            supportInvalidateOptionsMenu();
            showMessage(R.string.service_was_stopped);
            return true;
        }

        if (id == R.id.action_clear) {
            db.beginTransaction();
            db.delete(ScreenState.class);
            db.commitTransaction();
            showMessage(R.string.history_was_cleared);
            updateHistory();
            updatePercent();
            updateAllTime();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }

    private void showMessage(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
