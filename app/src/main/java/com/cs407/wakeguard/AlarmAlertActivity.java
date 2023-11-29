package com.cs407.wakeguard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class AlarmAlertActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_alert);

        // TODO: Update component info on Initial Alarm Screen. Listed below are the components that need live updating
        // TimeText: set to time alarm scheduled to go off
        // WakeGuardStatus: set to if WakeGuard is enabled or not on current alarm
        // AlarmTitle: the title given to the current alarm by the user
    }

    /**
     * Dismisses the initial alarm screen when the user clicks
     * the "Stop" button.
     *
     */
    public void onStopClick(View view) {

    }
}