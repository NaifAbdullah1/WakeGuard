package com.cs407.wakeguard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class AlarmAlertActivity extends AppCompatActivity {

    private TextView timeText;
    private ImageView wakeGuardLogo;
    private TextView wakeGuardStatus;
    private TextView alarmTitleNoWakeGuard;
    private TextView alarmTitleWithWakeGuard;

    private AlarmCard triggeredAlarm;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_alert);

        dbHelper = DBHelper.getInstance(this);
        timeText = findViewById(R.id.timeText);
        wakeGuardStatus = findViewById(R.id.wakeGuardStatus);
        alarmTitleNoWakeGuard = findViewById(R.id.alarmTitleNoWakeGuard);
        alarmTitleWithWakeGuard = findViewById(R.id.alarmTitleWithWakeGuard);

        // Makes the WakeGuard logo in this activity rounded (to make it look smoother)
        wakeGuardLogo = findViewById(R.id.wakeGuardLogo);
        wakeGuardLogo.setClipToOutline(true);

        // TODO: Update component info to match active alarm. Listed below are the components ids from activity _alarm_alert.xml that need live updating
        // TimeText: set to time alarm scheduled to go off
        // WakeGuardStatus: set to if WakeGuard is enabled or not on current alarm
        // AlarmTitle: the title given to the current alarm by the user

        int alarmId = getIntent().getIntExtra("alarmId", -1);
        Log.d("ALARM ID", ""+alarmId);
        if (alarmId != -1){ // Making sure alarmId is valid before fetching from DB
            loadAlarmDetails(alarmId);
        }

    }

    public void loadAlarmDetails(int alarmId){
        triggeredAlarm = dbHelper.getAlarmById(alarmId);
        timeText.setText(triggeredAlarm.getTime());
        alarmTitleWithWakeGuard.setText(triggeredAlarm.getTitle());
        alarmTitleNoWakeGuard.setText(triggeredAlarm.getTitle());
        if (triggeredAlarm.isMotionMonitoringOn()){
            wakeGuardLogo.setVisibility(View.VISIBLE);
            wakeGuardStatus.setVisibility(View.VISIBLE);
            alarmTitleWithWakeGuard.setVisibility(View.VISIBLE);
            alarmTitleNoWakeGuard.setVisibility(View.GONE);
        }else if (!triggeredAlarm.isMotionMonitoringOn()){
            wakeGuardLogo.setVisibility(View.GONE);
            wakeGuardStatus.setVisibility(View.GONE);
            alarmTitleWithWakeGuard.setVisibility(View.GONE);
            alarmTitleNoWakeGuard.setVisibility(View.VISIBLE);
        }else {
            Log.d("ERROR", "ERROR IN AlarmAlertActivity");
        }

    }

    /**
     * Dismisses the initial alarm screen when the user clicks
     * the "Stop" button.
     *
     */
    public void onStopClick(View view) {
        /*If the alarm is non-repeating, then toggle it off. Otherwise, just return to the dashboard. */
        boolean isAlarmRepeating = !triggeredAlarm.getRepeatingDays().equals("");
        if (!isAlarmRepeating){
          triggeredAlarm.setActive(false);
        }

        finish();
    }
}