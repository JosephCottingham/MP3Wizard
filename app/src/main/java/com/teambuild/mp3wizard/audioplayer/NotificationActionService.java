package com.teambuild.mp3wizard.audioplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationActionService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent("NotificationAction").putExtra("actionname", intent.getAction()));
    }
}
