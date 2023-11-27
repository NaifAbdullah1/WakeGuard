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
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.arbelkilani.clock.Clock;
import com.arbelkilani.clock.enumeration.ClockType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;



/**
 * Dashboard's clock: https://github.com/arbelkilani/Clock-view
 *
 * NATHAN'S TODO:
 * TODO: 1- We want the alarm cards to adjust the format of the time from the hh:mm a format to
 *  the 24 Hr format depending on the settings.
 *
 *
 * TODO: 2- When there are no alarms created, have a TextView in the place of the recycler stating that
 *      there are no created alarms (or even keep it empty)
 *
 * TODO: 3- We need to ensure that the next upcoming alarm is displayed in the dashboard.
 *
 *
 *   TODO: 4- Make sure to prevent the user from saving an alarm if the title field is empty
 *
 *   TODO 5- Put a cap on the length of the alarm title.
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

    private DBHelper dbHelper;

    private TextView upcomingAlarmsTextView;

    private final Handler alarmCountdownHandler = new Handler();

    private final Runnable alarmCountdownRunnable = new Runnable() {
        @Override
        public void run() {
            updateUpcomingAlarmText();
            alarmCountdownHandler.postDelayed(this, 60000); // Update every minute
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = DBHelper.getInstance(this);

        // ############### VARIABLE INITIALIZATION GOES HERE #########################
        // Exiting selection mode when the user clicks anywhere except an alarm card
        ConstraintLayout parentLayout = findViewById(R.id.parentLayout);
        upcomingAlarmsTextView = findViewById(R.id.upcomingAlarmsTextView);
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
        // Initializing the list of created alarms + the adapter
        alarmList = dbHelper.getAllAlarms();
        adapter = new AlarmAdapter(alarmList, dbHelper, this);
        adapter.notifyDataSetChanged();
        //_______________________________________________________________________________________


        // ################## LISTENERS GO HERE #######################################

        parentLayout.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                //dbHelper.printAllAlarms();
                for(AlarmCard alarm: alarmList){ // TODO: REMOVE
                    Log.i("ALARMS", "" + alarm.getId() + ", " + alarm.getTitle() + ", " + alarm.isActive() + "\n");
                }
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

    }

    /**
     * This ensures that the list of alarms in the dashboard is always synced with DB contents.
     */
    @Override
    protected void onResume(){
        super.onResume();
        dbHelper = DBHelper.getInstance(this);
        alarmList.clear();
        alarmList.addAll(dbHelper.getAllAlarms());
        adapter.notifyDataSetChanged();

        // Running the alarm countdown again.
        alarmCountdownHandler.post(alarmCountdownRunnable);
    }

    @Override
    protected void onPause(){
        super.onPause();

        /* Preventing the countdown from running when the application is
        paused (for example, user switching to another app). This minimizes the
        apps resource consumption in the background. */
        alarmCountdownHandler.removeCallbacks(alarmCountdownRunnable);
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
            dbHelper.addAlarm(newAlarmCard);

            /* Next 3 lines: Updating recyclerView by adding it to the local copy
            of alarms and notifying the recyclerView */
            alarmList.add(newAlarmCard);
            adapter.setAlarms(alarmList); // Update the adapter's data
            adapter.notifyDataSetChanged();
        }
    }

    protected void updateUpcomingAlarmText(){
        AlarmCard nearestAlarm = findNearestUpcomingAlarm();



        if (nearestAlarm != null){
            Log.i("TIMESTAMP", "" + convertToTimestamp(nearestAlarm.getTime(), nearestAlarm.getRepeatingDays()));
            // Calculating difference in time
            long timeDiff = convertToTimestamp(nearestAlarm.getTime(), nearestAlarm.getRepeatingDays()) - System.currentTimeMillis();

            // Convert diff to Days, Hours, and Minutes
            String countdownText = formatCountdownText(timeDiff);

            upcomingAlarmsTextView.setText("The next alarm is in " + countdownText);

        }else{
            upcomingAlarmsTextView.setText("No Upcoming Alarms");
        }
    }

    private String formatCountdownText(long diff) {
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days + " Days, ");
        if (hours > 0) sb.append(hours + " Hours, ");
        sb.append(minutes + " Minutes");
        return sb.toString();
    }

    private AlarmCard findNearestUpcomingAlarm(){
        // Assuming alarmList is already populated and contains the alarms.

        AlarmCard nearestAlarm = null;
        long nearestTime = Long.MAX_VALUE;
        long currentTime = System.currentTimeMillis();

        // Going through the list of alarms to find an active one
        for (AlarmCard alarm : alarmList){
            if(alarm.isActive()){
                // Convert the alarm time to a timestamp
                Log.i("nearest alarm", "WEW###########");
                long alarmTime = convertToTimestamp(alarm.getTime(), alarm.getRepeatingDays());
                Log.i("nearest alarm", "alarm time" + alarmTime);
                if (alarmTime > currentTime && alarmTime < nearestTime){
                    nearestTime = alarmTime;
                    nearestAlarm = alarm;
                }
            }
        }


        return nearestAlarm;
    }

    private long convertToTimestamp(String time, String repeatingDays) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Calendar now = Calendar.getInstance();

        try {
            // Parse the alarm time
            Date alarmTime = timeFormat.parse(time);
            Calendar nextAlarm = Calendar.getInstance();
            nextAlarm.setTime(alarmTime);

            // Set initial date to today
            nextAlarm.set(Calendar.YEAR, now.get(Calendar.YEAR));
            nextAlarm.set(Calendar.MONTH, now.get(Calendar.MONTH));
            nextAlarm.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));

            int today = now.get(Calendar.DAY_OF_WEEK);
            int daysUntilNextAlarm = 0;

            // Loop through the days to find the next active day
            for (int i = 0; i < 7; i++) {
                int checkDay = (today + i) % 7;
                String dayString = getDayString(checkDay);

                if (repeatingDays.contains(dayString)) {
                    daysUntilNextAlarm = i;
                    break;
                }
            }

            // Add the calculated days to the current date
            nextAlarm.add(Calendar.DATE, daysUntilNextAlarm);

            // If the calculated next alarm is before the current time, set it for the next week
            if (nextAlarm.before(now)) {
                nextAlarm.add(Calendar.DATE, 7);
            }

            return dateTimeFormat.parse(dateTimeFormat.format(nextAlarm.getTime())).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // Handle appropriately
        }
    }

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