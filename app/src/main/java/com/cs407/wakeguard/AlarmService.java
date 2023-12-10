package com.cs407.wakeguard;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
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
    private MediaPlayer mediaPlayer; // This is what plays the alarm tone


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

        boolean vibrationOn = intent.getBooleanExtra("vibrationOn", false);

        if (vibrationOn) {
            // Vibration pattern: 2 seconds on, 1 second off, repeat
            long[] pattern = {0, 2000, 1000}; // Start immediately, vibrate for 2s, pause for 1s

            // Repeat the pattern at index 1 (0 is the delay before starting)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 1));
        }

        String alarmToneName = intent.getStringExtra("alarmToneName");

        if (alarmToneName.equals("Default")){
            // Initializing media player and playing alarm sound
            // Use intents to get the name of the sound file such that if it's "none", don't play the sound
            mediaPlayer = MediaPlayer.create(this, R.raw.default_alarm_tone);
            mediaPlayer.setLooping(true); // To loop the alarm tone sound instead of playing it once
            mediaPlayer.start();
        }

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
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        vibrator.cancel(); // Stop vibration when service is destroyed
    }

    @Override
    public IBinder onBind(Intent intent) {
        // For services that are bound to an activity
        return null;
    }


}
