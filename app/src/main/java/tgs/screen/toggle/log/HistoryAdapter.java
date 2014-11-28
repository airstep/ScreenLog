package tgs.screen.toggle.log;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import tgs.screen.toggle.log.db.DatabaseHelper;

public class HistoryAdapter extends CursorAdapter {

    private final LayoutInflater mInflater;

    public HistoryAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.history_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView title = (TextView) view.findViewById(R.id.title);
        ImageView state = (ImageView) view.findViewById(R.id.state);
        long dateNum = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE));
        boolean toggle = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_TOGGLE)) != 0;
        title.setText(getDateString(dateNum));
        if (toggle) {
            title.setTextColor(Color.WHITE);
            title.setBackgroundColor(Color.parseColor("#6E7273"));
            state.setImageResource(R.drawable.off);
        }
        else {
            state.setImageResource(R.drawable.on);
            title.setBackgroundColor(Color.parseColor("#ffffff"));
            title.setTextColor(Color.BLACK);
        }
    }

    public String getDateString(long value) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Date date = new Date(value);
        return dateFormat.format(date);
    }
}
