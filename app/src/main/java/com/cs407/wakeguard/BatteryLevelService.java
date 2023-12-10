package com.cs407.wakeguard;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;

public class BatteryLevelService extends Service {
    private BroadcastReceiver batteryLevelReceiver;
    private int LOW_BATTERY_THRESHOLD = 10;
    private DBHelper dbHelper= DBHelper.getInstance(this);
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        batteryLevelReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPref = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE);
                boolean isBatteryMonitoringOn = sharedPref.getBoolean("isLowPowerMode", false);
                if (isBatteryMonitoringOn) {
                    int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    float batteryPct = level * 100 / (float) scale;
                    if (batteryPct == LOW_BATTERY_THRESHOLD) {
                        // Schedule Alarm
                        System.out.println("Low Battery Detected at " + batteryPct + "%. Setting off warning alarm...");
                        AlarmCard warningAlarm = new AlarmCard("Low Battery!", "", "Battery Is Less Than 10%",
                                "Default", true, false, true);
                        scheduleAlarm(warningAlarm);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelReceiver, filter);

        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        unregisterReceiver(batteryLevelReceiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Used only in case of bound services.
        return null;
    }

    private void scheduleAlarm (AlarmCard alarmCard){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // This intent is sent to AlarmReceiver.java with the alarm's ID as an extra
        Intent alarmReceiverIntent = new Intent(this, AlarmReceiver.class);
        alarmReceiverIntent.putExtra("alarmId", alarmCard.getId());
        alarmReceiverIntent.putExtra("vibrationOn", alarmCard.isVibrationOn());
        alarmReceiverIntent.putExtra("alarmToneName", alarmCard.getAlarmTone());

        long immediateAlarmTime = System.currentTimeMillis();

        // Generate a unique request code
        int requestCode = generateRequestCode();
        dbHelper.addRequestCode("LOW BATTERY ALERT", requestCode, "");

        // Use the unique request code for the PendingIntent
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, requestCode,
                alarmReceiverIntent, PendingIntent.FLAG_IMMUTABLE);

        // Set the alarm to go off immediately
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, immediateAlarmTime, alarmPendingIntent);
                } else {
                    // Show a dialog or notification to inform the user they need to grant the permission
                    Intent permissionIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(permissionIntent);
                }
            } else {
                // For older versions, set the alarm without checking the permission
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, immediateAlarmTime, alarmPendingIntent);
            }
        }
    }

    private synchronized int generateRequestCode(){
        int reqCode = DashboardActivity.requestCodeCreator++;
        saveRequestCode(DashboardActivity.requestCodeCreator); // saving it to SharedPreference for persistence.
        return reqCode;
    }

    private void saveRequestCode(int requestCode){
        SharedPreferences sharedPreferences = getSharedPreferences("com.cs407.wakeguard",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("nextRequestCode", DashboardActivity.requestCodeCreator);
        editor.apply();
    }
}

