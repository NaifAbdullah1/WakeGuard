package com.cs407.wakeguard;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;

import android.util.Log;


/**
 * Handles the logic to play an alarm tone or show a notification, vibrate, etc...
 */
public class AlarmService extends Service {
    private Vibrator vibrator;

    @Override
    public void onCreate(){
        super.onCreate();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * This is where the vibration is triggered when this serviec is
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

        // Example vibration pattern: Vibrate for 500 milliseconds, pause for 1000 milliseconds, then repeat.
        long[] pattern = {0, 500, 1000};

        // The -1 here means to vibrate once, as it represents the index to stop the pattern.
        // To repeat indefinitely, you can pass '0' instead.
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));

        //TODO: Handle other alarm functionalities like playing a tone.

        // Return START_NOT_STICKY or START_REDELIVER_INTENT as needed
        return START_NOT_STICKY;
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
