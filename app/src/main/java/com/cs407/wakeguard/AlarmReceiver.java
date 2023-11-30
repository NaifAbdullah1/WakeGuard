package com.cs407.wakeguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * This receiver is responsible for catching alarm events.
 * Enabling the alarm to go off in the background and perform the actions needed.
 */
public class AlarmReceiver extends BroadcastReceiver {

    /**
     * This funciton is called when this class, the AlarmReceiver class, receives an intent
     * broadcast (which is, in this case, the alarm) This is where we define what happens when the
     * alarm goes off.
     *
     * @param context
     */
    @Override
    public void onReceive(Context context, Intent intent){

        // Starting the AlarmAlertActivityIntent to bring up the alarm alert screen. TODO: Implement the AlarmAlertActivity screen
        //Intent AlarmAlertActivityIntent = new Intent(context, AlarmAlertActivity.class);
        //AlarmAlertActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //context.startActivity(AlarmAlertActivityIntent);


        // Starting the AlarmService to play the alarm tone
        Intent alarmServiceIntent = new Intent(context, AlarmService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(alarmServiceIntent);
        else
            context.startService(alarmServiceIntent);
    }
}
