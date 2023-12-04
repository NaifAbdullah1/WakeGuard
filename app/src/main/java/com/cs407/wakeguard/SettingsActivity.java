package com.cs407.wakeguard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private boolean isLowPowerMode;
    private boolean isDoNotDisturb;
    private boolean isMilitaryTimeFormat;
    private int activityThresholdMonitoringLevel;
    private int activityMonitoringDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Assign Variables
        isLowPowerMode = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE).getBoolean("isLowPowerMode", false);
        isDoNotDisturb = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE).getBoolean("isDoNotDisturb", false);
        isMilitaryTimeFormat = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE).getBoolean("isMilitaryTimeFormat", false);
        activityThresholdMonitoringLevel = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE).getInt("activityThresholdMonitoringLevel", 0);
        activityMonitoringDuration = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE).getInt("activityMonitoringDuration", 0);
        Log.i("isLowPowerMode", "" + isLowPowerMode);
        Log.i("isDoNotDisturb", "" + isDoNotDisturb);
        Log.i("isMilitaryTimeFormat", "" + isMilitaryTimeFormat);
        Log.i("activityThresholdMonitoringLevel", "" + activityThresholdMonitoringLevel);
        Log.i("activityMonitoringDuration", "" + activityMonitoringDuration);

        Switch isLowPowerModeSwitch = findViewById(R.id.lowbatterybutton);
        Switch isDoNotDisturbSwitch = findViewById(R.id.doNotDisturbButton);
        Switch isMilitaryTimeFormatSwitch = findViewById(R.id.militaryTimeButton);
        NumberPicker activity_duration = findViewById(R.id.minuteNumberPicker);
        activity_duration.setMinValue(0);
        activity_duration.setMaxValue(59);
        Button reset = findViewById(R.id.resetButton);


        //Activity Monitor Threshold Menu
        Spinner activityMonitorThreshold=findViewById(R.id.activityMonitorMenu);
        ArrayAdapter<CharSequence>adapter=ArrayAdapter.createFromResource(this, R.array.monitorThresholds, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        activityMonitorThreshold.setAdapter(adapter);

        //Initialize Values
        isLowPowerModeSwitch.setChecked(isLowPowerMode);
        isDoNotDisturbSwitch.setChecked(isDoNotDisturb);
        isMilitaryTimeFormatSwitch.setChecked(isMilitaryTimeFormat);
        activity_duration.setValue(activityMonitoringDuration);
        activityMonitorThreshold.setSelection(activityThresholdMonitoringLevel);

        //Infomation Popup Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);


        // LISTENERS HERE
        activity_duration.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                activityMonitoringDuration = newVal;
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLowPowerMode = false;
                isDoNotDisturb = false;
                isMilitaryTimeFormat = false;
                activityThresholdMonitoringLevel = 1;
                activityMonitoringDuration = 0;
                isLowPowerModeSwitch.setChecked(isLowPowerMode);
                isDoNotDisturbSwitch.setChecked(isDoNotDisturb);
                isMilitaryTimeFormatSwitch.setChecked(isMilitaryTimeFormat);
                activityMonitorThreshold.setSelection(activityThresholdMonitoringLevel);
            }
        });
        isLowPowerModeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLowPowerMode = !isLowPowerMode;
            }
        });
        isDoNotDisturbSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDoNotDisturb = !isDoNotDisturb;
            }
        });
        isMilitaryTimeFormatSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMilitaryTimeFormat = !isMilitaryTimeFormat;
            }
        });

        ImageButton backSettings = findViewById(R.id.backSettings);
        backSettings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent backDashboard = new Intent(SettingsActivity.this, DashboardActivity.class);
                SharedPreferences sharedPref = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE);
                sharedPref.edit().putBoolean("isLowPowerMode", isLowPowerMode).apply();
                sharedPref.edit().putBoolean("isDoNotDisturb", isDoNotDisturb).apply();
                sharedPref.edit().putBoolean("isMilitaryTimeFormat", isMilitaryTimeFormat).apply();
                sharedPref.edit().putInt("activityMonitoringDuration", activityMonitoringDuration).apply();
                sharedPref.edit().putInt("activityThresholdMonitoringLevel", activityThresholdMonitoringLevel).apply();
                startActivity(backDashboard);
            }
        });

        ImageButton infoDoNotDisturb = findViewById(R.id.doNotDistubInfo);
        infoDoNotDisturb.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                builder.setMessage(R.string.do_not_disturb_message)
                        .setTitle(R.string.do_not_disturb_title);

                AlertDialog dnd_info = builder.create();
                dnd_info.show();
            }
        });
        ImageButton infoLowBattery = findViewById(R.id.lowBatteryInfo);
        infoLowBattery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                builder.setMessage(R.string.low_battery_message)
                        .setTitle(R.string.low_battery_title);

                AlertDialog low_battery_info = builder.create();
                low_battery_info.show();
            }
        });
    }
}