package com.cs407.wakeguard;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;

import android.util.Log;

import androidx.core.app.NotificationCompat;


/**
 * Handles the logic to play an alarm tone or show a notification, vibrate, etc...
 * This service will handle playing the alarm tone.
 * It can also manage other tasks like vibration or flashing the screen.
 * If you're playing media or sounds, make sure to handle audio focus appropriately.
 */
public class AlarmService extends Service {
    private Vibrator vibrator;
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "alarm_service_channel";


    @Override
    public void onCreate(){
        super.onCreate();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * This is where the vibration is triggered when this service is
     * started by the AlarmReceiver.java
     *
     * @param intent The Intent supplied to {@link android.content.Context#startService},
     * as given.  This may be null if the service is being restarted after
     * its process has gone away, and it had previously returned anything
     * except {@link #START_STICKY_COMPATIBILITY}.
     * @param flags Additional data about this start request.
     * @param startId A unique integer representing this specific request to
     * start.  Use with {@link #stopSelfResult(int)}.
     *
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        // Create Notification, it's required by Android to notify user of background services.
        Notification alarmNotification = createNotification();
        // Start service in the foreground
        startForeground(NOTIFICATION_ID, alarmNotification);

        // Example vibration pattern: Vibrate for 500 milliseconds, pause for 1000 milliseconds, then repeat.
        long[] pattern = {0, 500, 1000};

        // The -1 here means to vibrate once, as it represents the index to stop the pattern.
        // To repeat indefinitely, you can pass '0' instead.
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));

        //TODO: Handle other alarm functionalities like playing a tone.

        // Return START_NOT_STICKY or START_REDELIVER_INTENT as needed
        return START_NOT_STICKY;
    }

    private Notification createNotification(){
        // Creating a notification channel for Android O and newer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            String channelId = "alarm_service_channel";
            String channelName = "Alarm Service Channel";
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }

        // Build and return the notification
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Alarm Active")
                .setContentText("Your alarm is ringing.")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        vibrator.cancel(); // Stop vibration when service is destroyed
    }

    @Override
    public IBinder onBind(Intent intent) {
        // For services that are bound to an activity
        return null;
    }


}
