package tgs.screen.toggle.log.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import tgs.screen.toggle.log.service.UpdateService;

public class ScreenReceiver extends BroadcastReceiver {
    private boolean toggleState;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            toggleState = false;
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            toggleState = true;
        }
        Intent i = new Intent(context, UpdateService.class);
        i.setAction(UpdateService.ACTION_SCREEN_STATE);
        i.putExtra(UpdateService.ACTION_SCREEN_STATE, toggleState);
        context.startService(i);
    }

}
