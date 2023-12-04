package com.cs407.wakeguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

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
        Log.d("Printing intent", "kekmaster");
        Log.d("Printing intent", "Printing intent:");
        Log.d("wew", intentToString(intent));
        // Starting the AlarmAlertActivityIntent to bring up the alarm alert screen.
        Intent alarmAlertActivityIntent = new Intent(context, AlarmAlertActivity.class);
        alarmAlertActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // Receving the alarmReceiverIntent from DashboardActivity's scheduleAlarm() method


        Log.d("Printing intent", "Printing alarmAlertActivityIntent :");
        Log.d("wew", intentToString(alarmAlertActivityIntent ));

        int alarmId = intent.getIntExtra("alarmId", -1);

        Log.d("ID onReceive"," " + alarmId);
        // Taking the id from alarmReceiverIntent and putting it in alarmAlertActivityIntent
        alarmAlertActivityIntent.putExtra("alarmId", alarmId); // Passing alarm Id to AlarmAlertActivity
        context.startActivity(alarmAlertActivityIntent);

        // Starting the AlarmService to play the alarm tone
        Intent alarmServiceIntent = new Intent(context, AlarmService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(alarmServiceIntent);
        else
            context.startService(alarmServiceIntent);
    }

    public static String intentToString(Intent intent) {
        StringBuilder stringBuilder = new StringBuilder("Intent Extras: \n");
        if (intent.getExtras() != null) {
            for (String key : intent.getExtras().keySet()) {
                stringBuilder.append(key).append(" = ").append(intent.getExtras().get(key)).append("\n");
            }
        } else {
            stringBuilder.append("No extras");
        }
        return stringBuilder.toString();
    }
}
