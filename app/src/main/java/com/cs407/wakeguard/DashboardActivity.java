package com.cs407.wakeguard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.arbelkilani.clock.Clock;
import com.arbelkilani.clock.enumeration.ClockType;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Getting the object representing the dashboard's clock
        Clock dashboardClock = findViewById(R.id.dashboardClock);

        // Setting the clock type to numeric (instead of analog)
        dashboardClock.setClockType(ClockType.numeric);

        dashboardClock.setPadding(0, 0, 0, 0);
    }
}