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

    private boolean lpm;
    private boolean dnd;
    private boolean mtf;
    private int atm;
    private int amd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Assign Variables
        lpm = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE).getBoolean("lpm", false);
        dnd = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE).getBoolean("dnd", false);
        mtf = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE).getBoolean("mtf", false);
        atm = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE).getInt("atm", 0);
        amd = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE).getInt("amd", 0);
        Log.i("lpm", "" + lpm);
        Log.i("dnd", "" + dnd);
        Log.i("mtf", "" + mtf);
        Log.i("atm", "" + atm);
        Log.i("amd", "" + amd);

        Switch lp_switch = findViewById(R.id.lowbatterybutton);
        Switch dnd_switch = findViewById(R.id.doNotDisturbButton);
        Switch mtf_switch = findViewById(R.id.militaryTimeButton);
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
        lp_switch.setChecked(lpm);
        dnd_switch.setChecked(dnd);
        mtf_switch.setChecked(mtf);
        activity_duration.setValue(amd);
        activityMonitorThreshold.setSelection(atm);

        //Infomation Popup Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);


        // LISTENERS HERE
        activity_duration.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Log.i("num changed", "" + newVal);
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lpm = false;
                dnd = false;
                mtf = false;
                atm = 1;
                amd = 0;
                lp_switch.setChecked(lpm);
                dnd_switch.setChecked(dnd);
                mtf_switch.setChecked(mtf);
                activityMonitorThreshold.setSelection(atm);
            }
        });
        lp_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lpm = !lpm;
            }
        });
        dnd_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dnd = !dnd;
            }
        });
        mtf_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mtf = !mtf;
            }
        });

        ImageButton backSettings = findViewById(R.id.backSettings);
        backSettings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent backDashboard = new Intent(SettingsActivity.this, DashboardActivity.class);
                SharedPreferences sharedPref = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE);
                sharedPref.edit().putBoolean("lpm", lpm).apply();
                sharedPref.edit().putBoolean("dnd", dnd).apply();
                sharedPref.edit().putBoolean("mtf", mtf).apply();
                sharedPref.edit().putInt("amd", amd).apply();
                sharedPref.edit().putInt("atm", atm).apply();
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