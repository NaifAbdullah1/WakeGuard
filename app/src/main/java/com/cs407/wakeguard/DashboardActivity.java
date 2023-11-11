package com.cs407.wakeguard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.arbelkilani.clock.Clock;
import com.arbelkilani.clock.enumeration.ClockType;

import java.util.ArrayList;
import java.util.List;


/**
 * Dashboard's clock: https://github.com/arbelkilani/Clock-view
 *
 * NATHAN'S TODO:
 * 1- Consider using the same switches that James used, to stay consistent.
 * 2- Add the icons for the main Dashboard. 
 */
public class DashboardActivity extends AppCompatActivity {

    // This is what will display the created alarms, both active and inactive alarms
    private RecyclerView recyclerView;

    /* A custom adapter class that bridges between the RecyclerView in UI and
     * the data that should go in it. More details on what this is for can be
     * found by going to the class definition "AlarmAdapter.java"
     */
    private AlarmAdapter adapter;

    // A list obect containing all the AlarmCard objects, both active and inactive alarms
    private List<AlarmCard> alarmList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        recyclerView = findViewById(R.id.createdAlarmsRecycerView);
        // Initializing the list of created alarms + the adapter
        alarmList = new ArrayList<>();
        adapter = new AlarmAdapter(alarmList);

        /*Layout managers are needed because they help us define what gets
        * displayed on the RecyclerView (which displays the alarm cards) and how to arrange
        * the items. It also determines other functions like scroll direction etc..*/
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager((getApplicationContext()));
        recyclerView.setLayoutManager(layoutManager);

        /*An ItemAnimator handles animations for item views when changes such
        * as items being added, removed, or moved occur (Within the RecyclerView)
        * Specifically, the DefaultItemAnimator provides default animations for
        * common item changes. For example, when you insert a new item into
        * the RecyclerView, it will animate in from offscreen. When you remove an
        * item, the DefaultItemAnimator will animate its disappearance.*/
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        // Getting the object representing the dashboard's clock
        Clock dashboardClock = findViewById(R.id.dashboardClock);

        // Setting the clock type to numeric (instead of analog)
        dashboardClock.setClockType(ClockType.numeric);

        /*Removing padding all around the clock so that if there's
        * anything close to the clock, it would be clickable */
        dashboardClock.setPadding(0, 0, 0, 0);


        /* Creating a listening for adding alarms button (the "+" button).
        This will take us to the alarm editor page to create a new alarm*/
        AppCompatImageButton addAlarmButton = findViewById(R.id.addAlarmButton);
        addAlarmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent addNewAlarmIntent = new Intent(DashboardActivity.this, AlarmEditorActivity.class);
                startActivity(addNewAlarmIntent);
            }
        });

        TextView upcomingAlarmsTextView = findViewById(R.id.upcomingAlarmsTextView);
        // TODO: Add code to change this text view such that if there's >= 1 upcoming active alarm, the TextView should change as per the wireframe


        //TODO: For testing only
        alarmList.add(new AlarmCard("10:00 AM", "Gym", false));
        alarmList.add(new AlarmCard("1:00 AM", "Nap", true));
        alarmList.add(new AlarmCard("2:00 AM", "Extra Pump", false));
        alarmList.add(new AlarmCard("3:00 PM", "Gym Again", true));
        alarmList.add(new AlarmCard("11:00 AM", "Class", true));

        // Tells the adapted to update the content of the list in the UI
        adapter.notifyDataSetChanged();

    }
}