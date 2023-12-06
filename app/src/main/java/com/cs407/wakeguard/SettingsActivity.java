package com.cs407.wakeguard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Assign Variables
        sharedPref = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE);
        isLowPowerMode = sharedPref.getBoolean("isLowPowerMode", false);
        isDoNotDisturb = sharedPref.getBoolean("isDoNotDisturb", false);
        isMilitaryTimeFormat = sharedPref.getBoolean("isMilitaryTimeFormat", false);
        activityThresholdMonitoringLevel = sharedPref.getInt("activityThresholdMonitoringLevel", 0);
        activityMonitoringDuration = sharedPref.getInt("activityMonitoringDuration", 0);
        ImageButton infoLowBattery = findViewById(R.id.lowBatteryInfo);
        Switch isLowPowerModeSwitch = findViewById(R.id.lowbatterybutton);
        Switch isDoNotDisturbSwitch = findViewById(R.id.doNotDisturbButton);
        Switch isMilitaryTimeFormatSwitch = findViewById(R.id.militaryTimeButton);
        ImageButton infoDoNotDisturb = findViewById(R.id.doNotDistubInfo);
        ImageButton backButtonSettings = findViewById(R.id.backSettings);
        NumberPicker activityMonitoringDurationNumberPicker = findViewById(R.id.minuteNumberPicker);
        Spinner activityMonitorThreshold=findViewById(R.id.activityMonitorMenu);
        Button reset = findViewById(R.id.resetButton);
        ArrayAdapter<CharSequence>adapter=ArrayAdapter.createFromResource(this, R.array.monitorThresholds, android.R.layout.simple_spinner_item);
        //Infomation Popup Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);

        // Settings for variables
        activityMonitoringDurationNumberPicker.setMinValue(1);
        activityMonitoringDurationNumberPicker.setMaxValue(15);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        activityMonitorThreshold.setAdapter(adapter);
        isLowPowerModeSwitch.setChecked(isLowPowerMode);
        isDoNotDisturbSwitch.setChecked(isDoNotDisturb);
        isMilitaryTimeFormatSwitch.setChecked(isMilitaryTimeFormat);
        activityMonitoringDurationNumberPicker.setValue(activityMonitoringDuration);
        activityMonitorThreshold.setSelection(activityThresholdMonitoringLevel);


        // LISTENERS HERE
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLowPowerMode = false;
                isDoNotDisturb = false;
                isMilitaryTimeFormat = false;
                activityThresholdMonitoringLevel = 1; // 2 = low, 1=medium, 0 = high
                activityMonitoringDuration = 5;
                isLowPowerModeSwitch.setChecked(isLowPowerMode);
                isDoNotDisturbSwitch.setChecked(isDoNotDisturb);
                isMilitaryTimeFormatSwitch.setChecked(isMilitaryTimeFormat);
                activityMonitorThreshold.setSelection(activityThresholdMonitoringLevel);
                // Persisting the changes
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("isLowPowerMode", isLowPowerMode);
                editor.putBoolean("isDoNotDisturb", isDoNotDisturb);
                editor.putBoolean("isMilitaryTimeFormat", isMilitaryTimeFormat);
                editor.putInt("activityThresholdMonitoringLevel", activityThresholdMonitoringLevel);
                editor.putInt("activityMonitoringDuration", activityMonitoringDuration);
                editor.apply();

            }
        });
        isLowPowerModeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLowPowerMode = !isLowPowerMode;
                System.out.println("Low power clicked, new val: " + isLowPowerMode);
                sharedPref.edit().putBoolean("isLowPowerMode", isLowPowerMode).apply();
            }
        });
        isDoNotDisturbSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDoNotDisturb = !isDoNotDisturb;
                System.out.println("dnd new val: " + isDoNotDisturb);
                sharedPref.edit().putBoolean("isDoNotDisturb", isDoNotDisturb).apply();
            }
        });
        isMilitaryTimeFormatSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMilitaryTimeFormat = !isMilitaryTimeFormat;
                System.out.println("isMilitaryTimeFormat, new val: " + isMilitaryTimeFormat);
                sharedPref.edit().putBoolean("isMilitaryTimeFormat", isMilitaryTimeFormat).apply();
            }
        });
        backButtonSettings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                /*
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("isLowPowerMode", isLowPowerMode);
                editor.putBoolean("isDoNotDisturb", isDoNotDisturb);
                editor.putBoolean("isMilitaryTimeFormat", isMilitaryTimeFormat);
                editor.putInt("activityThresholdMonitoringLevel", activityThresholdMonitoringLevel);
                editor.putInt("activityMonitoringDuration", activityMonitoringDuration);
                editor.apply();
                */
                finish();
            }
        });
        infoDoNotDisturb.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                builder.setMessage(R.string.do_not_disturb_message)
                        .setTitle(R.string.do_not_disturb_title);

                AlertDialog dnd_info = builder.create();
                dnd_info.show();
            }
        });
        infoLowBattery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                builder.setMessage(R.string.low_battery_message)
                        .setTitle(R.string.low_battery_title);

                AlertDialog low_battery_info = builder.create();
                low_battery_info.show();
            }
        });
        activityMonitorThreshold.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                activityThresholdMonitoringLevel = position;
                System.out.println("Activity monitor threshold new val: " + activityThresholdMonitoringLevel);
                sharedPref.edit().putInt("activityThresholdMonitoringLevel", activityThresholdMonitoringLevel).apply();
                System.out.println("testing: " + sharedPref.getInt("activityThresholdMonitoringLevel", 69));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
        activityMonitoringDurationNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener(){
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                activityMonitoringDuration = newVal;
                System.out.println("activityMonitoringDurationNumberPicker new val: " + activityMonitoringDuration);
                sharedPref.edit().putInt("activityMonitoringDuration", activityMonitoringDuration).apply();
            }
        });

    }
}