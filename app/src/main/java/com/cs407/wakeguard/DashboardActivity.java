package com.cs407.wakeguard;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.arbelkilani.clock.Clock;
import com.arbelkilani.clock.enumeration.ClockType;

import java.util.Iterator;
import java.util.List;


/**
 * Dashboard's clock: https://github.com/arbelkilani/Clock-view
 *
 * NATHAN'S TODO:
 * TODO: 1- The alarm cards do show the time correctly, but we want to make it such that the
 *      format of the time changes from the hh:mm a format to the 24 Hr format depending on the settings.
 *
 *
 * TODO: 2- When there are no alarms created, have a TextView in the place of the recycler stating that
 * there are no created alarms (or even keep it empty)
 *
 * TODO: 3- Work on updating the alarms. This involves two steps:
 *   A- When the user switches an alarm off or on, a switch event listener should update the alarm in
 *   the DB accordingly
 *   B- When the user clicks the alarm to edit its details, you're going to want to update it in a
 *   similar way to how you create one. Maybe use the boolean value "isEditing" and have a two-way
 *   if-statement in the onActivityResult (use the intent to pass whether you're editing or creating
 *   an alarm)
 *
 *   TODO: 4- Make sure to prevent the user from saving an alarm if the title field is empty
 *
 */
public class DashboardActivity extends AppCompatActivity {

    // This is what will display the created alarms, both active and inactive alarms
    private RecyclerView recyclerView;

    /* A custom adapter class that bridges between the RecyclerView in UI and
     * the data that should go in it. More details on what this is for can be
     * found by going to the class definition "AlarmAdapter.java"
     */
    private AlarmAdapter adapter;

    // A list object containing all the AlarmCard objects, both active and inactive alarms
    private List<AlarmCard> alarmList;

    // SelectionMode is when the checkboxes are visible next to every alarm card
    private boolean isSelectionMode = false;

    private AppCompatImageButton settingsButton;

    private AppCompatImageButton addAlarmButton;

    // TODO: MAKE SURE YOU UNDERSTAND WHAT THIS DOES
    private static final int NEW_ALARM_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // ############### VARIABLE INITIALIZATION GOES HERE #########################
        // Exiting selection mode when the user clicks anywhere except an alarm card
        ConstraintLayout parentLayout = findViewById(R.id.parentLayout);
        // Initializing the settingsButton so that its functions (such as showDeleteIcon) work
        settingsButton = findViewById(R.id.settingsButton);
        addAlarmButton = findViewById(R.id.addAlarmButton);
        recyclerView = findViewById(R.id.createdAlarmsRecycerView);
        // Getting the object representing the dashboard's clock
        Clock dashboardClock = findViewById(R.id.dashboardClock);

        /*Layout managers are needed because they help us define what gets
         * displayed on the RecyclerView (which displays the alarm cards) and how to arrange
         * the items. It also determines other functions like scroll direction etc..*/
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager((getApplicationContext()));
        DBHelper dbHelper = new DBHelper(this);
        // Initializing the list of created alarms + the adapter
        alarmList = dbHelper.getAllAlarms();
        adapter = new AlarmAdapter(alarmList);
        adapter.notifyDataSetChanged();
        //_______________________________________________________________________________________


        // ################## LISTENERS GO HERE #######################################

        parentLayout.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if(isSelectionModeActive()){
                    exitSelectionMode();
                    return true; // consuming the touch event
                }
                return false; // Do not consume the event, let it propagate
            }
        });

        /* Creating a listening for adding alarms button (the "+" button).
        This will take us to the alarm editor page to create a new alarm*/
        addAlarmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent addNewAlarmIntent = new Intent(DashboardActivity.this, AlarmEditorActivity.class);
                //startActivity(addNewAlarmIntent, NEW_ALARM_REQUEST_CODE);
                startActivity(addNewAlarmIntent);
            }
        });

        // TODO: ADD THE SETTINGS' OnClickListener HERE

        //_______________________________________________________________________________________


        //// ################## OBJECTS' SETTINGS GO HERE #######################################

        /*An ItemAnimator handles animations for item views when changes such
        * as items being added, removed, or moved occur (Within the RecyclerView)
        * Specifically, the DefaultItemAnimator provides default animations for
        * common item changes. For example, when you insert a new item into
        * the RecyclerView, it will animate in from offscreen. When you remove an
        * item, the DefaultItemAnimator will animate its disappearance.*/
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        // Setting the clock type to numeric (instead of analog)
        dashboardClock.setClockType(ClockType.numeric);
        /*Removing padding all around the clock so that if there's
        * anything close to the clock, it would be clickable */
        dashboardClock.setPadding(0, 0, 0, 0);
        //_______________________________________________________________________________________


        TextView upcomingAlarmsTextView = findViewById(R.id.upcomingAlarmsTextView);
        // TODO: Add code to change this textview such that if there's >= 1 upcoming active alarm, the TextView should change as per the wireframe

    }

    /**
     * This ensures that the list of alarms in the dashboard is always synced with DB contents.
     */
    @Override
    protected void onResume(){
        super.onResume();
        DBHelper dbHelper = new DBHelper(this);
        alarmList.clear();
        alarmList.addAll(dbHelper.getAllAlarms());
        adapter.notifyDataSetChanged();
    }

    /**
     * When AlarmEditorActivity finishes, this function will be called. This is where
     * we receive alarm data from the AlarmEditorActivity
     * TODO: QUESTION: if an activity starts another activity using intents, does the starting activity (in this case, DashboardActivity.java by default calls onActivityResult() always? Try to understand the control-flow here.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_ALARM_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            /* Unpacking the data we got from the Intent (which started the
             AlarmEditorActivity. We're retrieving the new alarm data*/
            String time = data.getStringExtra("time");
            String daysActive = data.getStringExtra("daysActive");
            String title = data.getStringExtra("title");
            String alarmTone = data.getStringExtra("alarmTone");
            boolean isVibrationOn = data.getBooleanExtra("isVibrationOn", true);
            boolean isMotionMonitoringOn = data.getBooleanExtra("isMotionMonitoringOn", true);
            boolean isActive = data.getBooleanExtra("isActive", true);

            AlarmCard newAlarmCard = new AlarmCard(time, daysActive,
                    title, alarmTone, isVibrationOn, isMotionMonitoringOn, isActive);

            // Next 2 lines: Saving alarm to DB
            DBHelper dbHelper = new DBHelper(this);
            dbHelper.addAlarm(newAlarmCard);

            /* Next 3 lines: Updating recyclerView by adding it to the local copy
            of alarms and notifying the recyclerView */
            alarmList.add(newAlarmCard);
            adapter.setAlarms(alarmList); // Update the adapter's data
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Entering selection mode
     */
    public void enterSelectionMode(){
        isSelectionMode = true;
        adapter.setSelectionMode(true);
        adapter.notifyDataSetChanged();

        // replacing settings button with delete icon
        settingsButton.setImageResource(R.drawable.ic_delete);
        // Changing the associated onClickListener
        settingsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                deleteSelectedAlarms();
                exitSelectionMode();
            }
        });

        addAlarmButton.setImageResource(R.drawable.ic_volume_off);
        addAlarmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                silenceAllAlarms();
                exitSelectionMode();
            }
        });

    }

    /**
     * Exits selection mode
     */
    public void exitSelectionMode(){
        isSelectionMode = false;
        adapter.setSelectionMode(false);
        adapter.notifyDataSetChanged();
        // Changing the Delete Icon back to the Settings Icon

        // Replacing the delete icon with the settings icon
        settingsButton.setImageResource(R.drawable.ic_settings);

        settingsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // Handing settings funcitonality
                // TODO: Use intents to go to the settings.
            }
        });

        addAlarmButton.setImageResource(R.drawable.ic_add);
        addAlarmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent addNewAlarmIntent = new Intent(DashboardActivity.this, AlarmEditorActivity.class);
                startActivity(addNewAlarmIntent);
            }
        });

        deselectAllAlarms();
    }

    /**
     * Checks if selection mode is enabled
     */
    public boolean isSelectionModeActive(){
        return isSelectionMode;
    }

    /**
     * This function runs when the user clicks the "Delete" icon.
     */
    private void deleteSelectedAlarms(){
        // Loop through all the created alarms and delete the ones where isSelected = true
        Iterator<AlarmCard> alaramIterator = alarmList.iterator();
        DBHelper dbHelper = new DBHelper(this);
        int numOfDeletedAlarms = 0;

        while (alaramIterator.hasNext()){
            AlarmCard alarmToDelete = alaramIterator.next();
            if (alarmToDelete.isSelected()){
                // Deleting the row from the recycler view
                alaramIterator.remove();
                numOfDeletedAlarms++;

                // Deleting the row from the DB
                dbHelper.deleteAlarm(alarmToDelete.getId());
            }
        }

        /* Notifying the RecyclerView adapter of the change in the List
        of created alarms to refresh This updates the UI */
        adapter.notifyDataSetChanged();

        // Displaying a toast message to let user know that deletion was successful.
        if (numOfDeletedAlarms == 1){
            Toast.makeText(getApplicationContext(), "Alarm Deleted", Toast.LENGTH_SHORT).show();
        }else if (numOfDeletedAlarms > 1){
            Toast.makeText(getApplicationContext(), numOfDeletedAlarms + " Alarms Deleted", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * This function runs when the user clicks the "Silence Alarms" button. It sets all the selected
     * alarms to inactive.
     */
    private void silenceAllAlarms(){
        Iterator<AlarmCard> alaramIterator = alarmList.iterator();

        while (alaramIterator.hasNext()){
            AlarmCard currentAlarm = alaramIterator.next();
                currentAlarm.setActive(false);
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * Un-ticks the checkboxes of every alarm
     */
    private void deselectAllAlarms(){
        for (AlarmCard alarm : alarmList) {
            alarm.setSelected(false);
        }
    }

}