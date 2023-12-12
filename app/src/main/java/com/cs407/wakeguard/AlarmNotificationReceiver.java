package com.cs407.wakeguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("id", -1);

        Toast.makeText(context.getApplicationContext(), "Time to stop the alarm", Toast.LENGTH_LONG).show();
    }
}
