package tgs.screen.toggle.log.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import tgs.screen.toggle.log.service.UpdateService;

public class BootReceiver extends BroadcastReceiver {
    Context mContext;
    private final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        String action = intent.getAction();
        if (action.equalsIgnoreCase(BOOT_ACTION)) {
            Intent serviceIntent = new Intent(context, UpdateService.class);
            context.startService(serviceIntent);
        }
    }
}