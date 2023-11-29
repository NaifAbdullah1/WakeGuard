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
 *  the 24 Hr format depending on the settings. We're waiting on team members to finish
 *  implementing the settings screen
 *
 *  TODO: 2- The current alarms in the screen have a problem, you must ensure that we're
 *      correctly capturing the nearest alarm. Fix the findnearestalarm() function.
 *
 *  TODO: 3- Investigate whether it's necessary to return intent from the alarm editor
 *      activity after saving the alarm to the DB. ASK CHAT GPT AND test it on your own. It's
 *      probably not needed.
 */
public class DashboardActivity extends AppCompatActivity {

    // This is what will display the created alarms, both active and inactive alarms
    private RecyclerView recyclerView;

    /* A custom adapter class that bridges between the RecyclerView in UI and
     * the data that should go in it. More details on what this is for can be
     * found by going to the class definition "AlarmAdapter.java"
     */
    private AlarmAdapter adapter;

    // A list object containing all the AlarmCard objects, that appear on the screen to the user
    private List<AlarmCard> alarmList;

    // SelectionMode is when the user press-and-hold one of the alarms and checkboxes appear
    private boolean isSelectionMode = false;

    // The button that looks like a gear icon.
    private AppCompatImageButton settingsButton;

    // The button that looks like a "+" sign
    private AppCompatImageButton addAlarmButton;

    // To do CRUD operations on alarms.
    private DBHelper dbHelper;

    // The text right under the clock in he dashboard. It counts down time for the nearest active alarm
    private TextView upcomingAlarmsTextView;

    // The next two variables are responsible for keeping the alarm countdown in dashboard live
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

        // Must initialize DB everywhere were we do CRUD operations
        dbHelper = DBHelper.getInstance(this);

        // ############### VARIABLE INITIALIZATION GOES HERE #########################
        ConstraintLayout parentLayout = findViewById(R.id.parentLayout);
        upcomingAlarmsTextView = findViewById(R.id.upcomingAlarmsTextView);
        settingsButton = findViewById(R.id.settingsButton);
        addAlarmButton = findViewById(R.id.addAlarmButton);
        recyclerView = findViewById(R.id.createdAlarmsRecycerView);
        Clock dashboardClock = findViewById(R.id.dashboardClock);

        /*Layout managers help us define what gets displayed on the RecyclerView (which displays
         the alarm cards) and how to arrange the items. It also determines other functions
         like scroll direction etc..*/
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager((getApplicationContext()));
        alarmList = dbHelper.getAllAlarms();
        adapter = new AlarmAdapter(alarmList, dbHelper, this);
        adapter.notifyDataSetChanged();
        //_______________________________________________________________________________________


        // ################## LISTENERS GO HERE #######################################

        // Exiting selection mode when the user clicks anywhere except an alarm card
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

        // Navigating to alarmEditor activity when pressing the add button
        addAlarmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent addNewAlarmIntent = new Intent(DashboardActivity.this, AlarmEditorActivity.class);
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
     * This function runs every time the Dashboard activity is brought up to the screen (Whether
     * it was because the user switched to another app and returned to the app, or user was in the
     * app but navigated away and back to the screen)
     */
    @Override
    protected void onResume(){
        super.onResume();
        dbHelper = DBHelper.getInstance(this);
        // Clearing the alarmList and refreshing it from the DB to ensure data is up to date
        alarmList.clear();
        alarmList.addAll(dbHelper.getAllAlarms());
        adapter.notifyDataSetChanged(); // signaling the adapter to refresh
        alarmCountdownHandler.post(alarmCountdownRunnable); // Running the alarm countdown again.
    }

    /**
     * If the application is paused (for example, user switches to another app). This function runs
     */
    @Override
    protected void onPause(){
        super.onPause();

        /* Preventing the countdown from running when the application is
        paused (for example, user switching to another app). This minimizes the
        apps resource consumption in the background. */
        alarmCountdownHandler.removeCallbacks(alarmCountdownRunnable);
    }

    /**
     * Updates the text under the clock in the dashboard to reflect the countdown for the nearest
     * active alarm.
     */
    protected void updateUpcomingAlarmText(){
        AlarmCard nearestAlarm = findNearestUpcomingAlarm();

        if (nearestAlarm != null){
            // Calculating difference in time
            long timeDiff = convertToTimestamp(nearestAlarm.getTime(), nearestAlarm.getRepeatingDays()) - System.currentTimeMillis();
            // Convert diff to Days, Hours, and Minutes
            String countdownText = formatCountdownText(timeDiff);
            upcomingAlarmsTextView.setText("The next alarm is in " + countdownText);
        }else{
            upcomingAlarmsTextView.setText("No Upcoming Alarms");
        }
    }

    /**
     * Formats how the text under the clock appears. If there's an upcoming alarm, that's too close,
     * we won't display the days or hours, we will only display as much as we need to.
     * This prevent us from saying that an alarm that's, for example, 30 minutes away, is "in 0
     * days and 0 hours and 30 minutes", rather, this function will make it say that it's
     * only "in 30 minutes"
     * @param diff the difference in time between the current time and the neares time, this makes
     *             up the countdown.
     * @return the text that'll appear on the screen as the countdown.
     */
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

    /**
     * Iterates over all the alarms and finds the nearest alarm that's active. We need this to ensure
     * that the countdown is always visible for the user if they had an active alarm.
     * @return AlarmCard object representing the nearest active alarm.
     */
    private AlarmCard findNearestUpcomingAlarm(){
        AlarmCard nearestAlarm = null;
        long nearestTime = Long.MAX_VALUE;
        long currentTime = System.currentTimeMillis();

        // Going through the list of alarms to find an active one
        for (AlarmCard alarm : alarmList){
            if(alarm.isActive()){
                // Convert the alarm time to a timestamp
                long alarmTime = convertToTimestamp(alarm.getTime(), alarm.getRepeatingDays());
                if (alarmTime > currentTime && alarmTime < nearestTime){
                    nearestTime = alarmTime;
                    nearestAlarm = alarm;
                }
            }
        }
        return nearestAlarm;
    }

    /**
     * Converts the time of the nearest active alarm to EPOCH time for the purpose of comparing it
     * against the current time. This is so that we can display the countdown for the nearest
     * upcoming alarm in the dashboard.
     *
     *
     * @param time The string representing the time of the upcoming alarm in 24Hr format (HH:mm)
     *             for example, 13:57, which is 1:57 PM
     * @param repeatingDays The days in which the alarm repeats. it's "" if it's a
     *                      non-repeating alarm
     * @return the time of the alarm in EPOCH format.
     */
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

            // For non-repeating alarms
            if (repeatingDays == null || repeatingDays.trim().isEmpty()) {
                if (nextAlarm.after(now)) {
                    return nextAlarm.getTimeInMillis();
                } else {
                    nextAlarm.add(Calendar.DAY_OF_MONTH, 1);
                    return nextAlarm.getTimeInMillis();
                }
            }

            // Initialize minimum difference
            long minDiff = -1;
            boolean todayCheckedAndPassed = false;

            for (int i = 0; i < 7; i++) {
                int dayIndex = (now.get(Calendar.DAY_OF_WEEK) + i) % 7;
                if (dayIndex == 0) dayIndex = 7; // Adjust for Sunday

                String dayString = getDayString(dayIndex);
                if (repeatingDays.contains(dayString)) {
                    Calendar potentialNextAlarm = (Calendar) nextAlarm.clone();
                    potentialNextAlarm.add(Calendar.DATE, i);

                    if (i == 0 && !potentialNextAlarm.after(now)) {
                        todayCheckedAndPassed = true;
                        continue;
                    }

                    long diff = potentialNextAlarm.getTimeInMillis() - now.getTimeInMillis();
                    if (minDiff == -1 || diff < minDiff) {
                        minDiff = diff;
                        nextAlarm = potentialNextAlarm;
                    }
                }
            }

            // If today was the only repeating day and time has passed, set for next week
            if (todayCheckedAndPassed && minDiff == -1) {
                nextAlarm.add(Calendar.DATE, 7);
                return nextAlarm.getTimeInMillis();
            }

            return minDiff != -1 ? nextAlarm.getTimeInMillis() : -1;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
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
     * Entering selection mode. When in selection mode, the Setting icon will turn into the trash
     * icon and the "+" button will turn into the "Silence All alarms" button. Checkboxes will
     * appear next to every alarm too.
     */
    public void enterSelectionMode(){
        isSelectionMode = true;
        adapter.setSelectionMode(true); // So that checkboxes appear next to every alarm card.
        adapter.notifyDataSetChanged(); // refreshing the container of alarm cards.

        // replacing settings button with delete icon
        settingsButton.setImageResource(R.drawable.ic_delete);

        // The "Settings" button, which is now the trash button, will delete selected alarms.
        settingsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                deleteSelectedAlarms();
                exitSelectionMode();
            }
        });

        // Changing the "+" btn to the "Silence All alarms" button
        addAlarmButton.setImageResource(R.drawable.ic_volume_off);

        // Same as above with the settings button
        addAlarmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                silenceAllAlarms();
                exitSelectionMode();
            }
        });

    }

    /**
     * Exits selection mode to return to the normal state of the dahsboard.
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
                // TODO: Use intents to go to the settings screen.
            }
        });

        addAlarmButton.setImageResource(R.drawable.ic_add);
        addAlarmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent addNewAlarmIntent = new Intent(DashboardActivity.this,
                        AlarmEditorActivity.class);
                startActivity(addNewAlarmIntent);
            }
        });

        // To ensure that none of the alarm's checkboxes remain checked after exiting selection mode
        deselectAllAlarms();
    }

    /**
     * Getter to check  if selection mode is enabled
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
        int numOfDeletedAlarms = 0; // Keeping track of the # of deleted alarms for the toast message

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
            Toast.makeText(getApplicationContext(), "Alarm Deleted",
                    Toast.LENGTH_SHORT).show();
        }else if (numOfDeletedAlarms > 1){
            Toast.makeText(getApplicationContext(), numOfDeletedAlarms + " Alarms Deleted",
                    Toast.LENGTH_SHORT).show();
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