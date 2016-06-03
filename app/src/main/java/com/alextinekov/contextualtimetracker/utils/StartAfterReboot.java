package com.alextinekov.contextualtimetracker.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Asus on 26.05.2016.
 */
public class StartAfterReboot extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, TrackingService.class));
    }
}
