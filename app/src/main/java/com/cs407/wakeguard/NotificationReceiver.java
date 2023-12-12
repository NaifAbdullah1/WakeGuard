package com.cs407.wakeguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Perform the same action as DashboardActivity's Disable Motion Monitoring button
        // Also cancel the notification
        SharedPreferences sharedPrefs = context.getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        //editor.putBoolean("isMotionMonitoringActive", false).apply();
        editor.putBoolean("showDisableMotionMonitoringButton", false).apply();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(1);
    }
}
