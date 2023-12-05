package com.cs407.wakeguard;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Calendar;

/**
 * This activity will be brought to the foreground when the alarm goes off.
 */
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

        // Set window flags to show the activity over the lock screen and wake up the screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_alarm_alert);



        dbHelper = DBHelper.getInstance(this);
        timeText = findViewById(R.id.timeText);
        wakeGuardStatus = findViewById(R.id.wakeGuardStatus);
        alarmTitleNoWakeGuard = findViewById(R.id.alarmTitleNoWakeGuard);
        alarmTitleWithWakeGuard = findViewById(R.id.alarmTitleWithWakeGuard);

        // Makes the WakeGuard logo in this activity rounded (to make it look smoother)
        wakeGuardLogo = findViewById(R.id.wakeGuardLogo);
        wakeGuardLogo.setClipToOutline(true);

        int alarmId = getIntent().getIntExtra("alarmId", -1);
        if (alarmId != -1){ // Making sure alarmId is valid before fetching from DB
            loadAlarmDetails(alarmId);
        }

    }

    public void loadAlarmDetails(int alarmId){
        triggeredAlarm = dbHelper.getAlarmById(alarmId);
        if (triggeredAlarm != null){
            timeText.setText(triggeredAlarm.getFormattedTime());
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
    }

    /**
     * Dismisses the initial alarm screen when the user clicks
     * the "Stop" button.
     *
     */
    public void onStopClick(View view) {
        /*If the alarm is non-repeating, then toggle it off. Otherwise, just return to the dashboard. */
            boolean isAlarmRepeating = !triggeredAlarm.getRepeatingDays().equals("");
            if (!isAlarmRepeating){ // If alarm is not repeating
                triggeredAlarm.setActive(false);
                dbHelper.toggleAlarm(triggeredAlarm.getId(), false);
            }else { // If alarm is indeed repeating, we're going to remove the request code of the alarm that just went off
                // If alarm is repeating, get the specific request code for this instance and delete it
                // Assuming you have a method to get today's day string (e.g., "Mo", "Tu", etc.)
                String todayDayString = getTodayDayString();
                String alarmIdentifier = triggeredAlarm.getTitle() + triggeredAlarm.getTime() + triggeredAlarm.getFormattedTime() + todayDayString;
                int requestCode = dbHelper.getRequestCode(alarmIdentifier);

                if (requestCode != -1) {
                    System.out.println("GENERATED REQ CODE TO DELETE");
                    System.out.println(requestCode);
                    cancelAlarmByRequestCode(requestCode);
                    dbHelper.deleteRequestCodeByRequestCode(requestCode);
                }else{
                    System.out.println("ERROR @ AlarmAlertActivity");
                }
            }

            // Releasing wake lock
            if (AlarmReceiver.wakeLock != null && AlarmReceiver.wakeLock.isHeld()){
                AlarmReceiver.wakeLock.release();
            }

        finish();
    }

    public void cancelAlarmByRequestCode(int reqCode){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent (this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                reqCode, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    private String getTodayDayString() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return getDayString(dayOfWeek); // Use the existing method from DashboardActivity to get the day string
    }

    /**
     * Converts a calendar day to a 2-letter string.
     *
     * @param calendarDay where 0= sunday and 6 = Saturday
     * @return The string representing the day
     */
    private String getDayString(int calendarDay) {
        switch (calendarDay) {
            case Calendar.SUNDAY:
                return "Su";
            case Calendar.MONDAY:
                return "Mo";
            case Calendar.TUESDAY:
                return "Tu";
            case Calendar.WEDNESDAY:
                return "We";
            case Calendar.THURSDAY:
                return "Th";
            case Calendar.FRIDAY:
                return "Fr";
            case Calendar.SATURDAY:
                return "Sa";
            default:
                return "";
        }
    }

    /**
     * Intended to prevent the instantiation of duplicate AlarmAlertActivity
     * @param intent The new intent that was started for the activity.
     *
     */
    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        setIntent(intent);
        int alarmId = intent.getIntExtra("alarmId", -1);
        if (alarmId != -1){
            loadAlarmDetails(alarmId);
        }
    }
}