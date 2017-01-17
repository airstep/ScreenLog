package tgs.screen.toggle.log.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import tgs.screen.toggle.log.R;
import tgs.screen.toggle.log.model.ScreenState;

public class HistoryAdapter extends ArrayAdapter<ScreenState> {

    private final int mResourceId;

    public HistoryAdapter(Context context, int resource) {
        super(context, resource);
        mResourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View row, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (row == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            row = inflater.inflate(mResourceId, parent, false);
            holder.title = (TextView) row.findViewById(R.id.title);
            holder.state = (ImageView) row.findViewById(R.id.state);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        ScreenState item = getItem(position);
        if (item != null) {
            holder.title.setText(getDateString(item.getDate().getTime()));
            if (item.toggle) {
                holder.title.setTextColor(Color.WHITE);
                row.setBackgroundColor(Color.parseColor("#6E7273"));
                holder.state.setImageResource(R.drawable.off);
            } else {
                holder.state.setImageResource(R.drawable.on);
                row.setBackgroundColor(Color.parseColor("#ffffff"));
                holder.title.setTextColor(Color.BLACK);
            }
        }
        return row;
    }

    private String getDateString(long value) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
        Date date = new Date(value);
        return dateFormat.format(date);
    }

    private static class ViewHolder {
        TextView title;
        ImageView state;
    }
}
