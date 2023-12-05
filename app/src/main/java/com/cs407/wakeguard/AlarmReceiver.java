package com.cs407.wakeguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

/**
 * This receiver is responsible for catching alarm events.
 * Enabling the alarm to go off in the background and perform the actions needed.
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static PowerManager.WakeLock wakeLock = null;

    /**
     * This funciton is called when this class, the AlarmReceiver class, receives an intent
     * broadcast (which is, in this case, the alarm) This is where we define what happens when the
     * alarm goes off.
     *
     * @param context
     */
    @Override
    public void onReceive(Context context, Intent intent){

        // Acquiring a wake lock to get the phone to wake up when alarm goes off
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeGuard:AlarmWakeLock");
        wakeLock.acquire(10*60*1000L /*10 minutes*/);

        // Starting the AlarmAlertActivityIntent to bring up the alarm alert screen.
        Intent alarmAlertActivityIntent = new Intent(context, AlarmAlertActivity.class);
        alarmAlertActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Receving the alarmReceiverIntent from DashboardActivity's scheduleAlarm() method

        int alarmId = intent.getIntExtra("alarmId", -1);

        // Taking the id from alarmReceiverIntent and putting it in alarmAlertActivityIntent
        alarmAlertActivityIntent.putExtra("alarmId", alarmId); // Passing alarm Id to AlarmAlertActivity
        context.startActivity(alarmAlertActivityIntent);

        // Starting the AlarmService to play the alarm tone
        Intent alarmServiceIntent = new Intent(context, AlarmService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(alarmServiceIntent);
        else
            context.startForegroundService(alarmServiceIntent);
    }

}
