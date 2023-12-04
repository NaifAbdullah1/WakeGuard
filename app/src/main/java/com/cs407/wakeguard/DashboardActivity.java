package com.cs407.wakeguard;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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
 * NATHAN'S Notes:
 * TODO: 1- We want the alarm cards to adjust the format of the time from the hh:mm a format to
 *  the 24 Hr format depending on the settings. We're waiting on team members to finish
 *  implementing the settings screen
 *
 *  TODO 5: When an alarm is deleted, it should be deactivated first, when it's deactivated, the pending intent should be canceled.
 *
 *  TODO: When an alarm is switched off, it doesn't get canceled, it still rings .
 *  
 *  TODO 4: In alarmAlertActivity, make sure PM/AM is working. Account for 24hr time too.
 *
 *  TODO 2: Find a way to make the countdown update every second.
 *
 *  TODO 3: In alarmAlertActivity, ensure that when wakeguard is disabled, space the elements out well
 *
 * TODO: Ensure alarms work after phone reboot too
 *
 *  TODO: Make the app refresh the shceduling every time the user opens the app. Or with a background script
 *
 *  TODO 6: After dismissing a non-repeating alarm, the alarm should be set inactive. Use DB operation to make the change. Consider editing the setters in AlarmCard
 *
 * AlarmService:
 *  This service will handle playing the alarm tone.
 *  It can also manage other tasks like vibration or flashing the screen.
 *  If you're playing media or sounds, make sure to handle audio focus appropriately.
 *
 * AlarmAlertActivity:
 *  This activity will be brought to the foreground when the alarm goes off.
 *  Here, you can provide options to dismiss or snooze the alarm.
 *  Ensure that the activity handles cases where the user might have already dismissed the alarm through another means (e.g., a notification).
 *
 * Optional: Wake Locks:
 *  Depending on your app's requirements and target Android versions, you might need to handle wake locks to ensure that your service continues to run even if the device goes to sleep.
 *  From Android 8.0 (API level 26), you should use startForegroundService() instead of startService() if the service will perform long-running operations in the background.
 *
 * We might also need to register the AlarmService by adding the following to the manifest.xml: <service android:name=".AlarmService" />
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

    //Settings Configuration Variables
    private boolean isLowPowerMode;
    private boolean isDoNotDisturb;
    private boolean isMilitaryTimeFormat;
    private int activityThresholdMonitoringLevel;
    private int activityMonitoringDuration;

    private int WEEKS_TO_SCHEDULE_AHEAD = 2;

    private static int requestCodeCreator = 1;

    private final Runnable alarmCountdownRunnable = new Runnable() {
        @Override
        public void run() {
            updateUpcomingAlarmText();
            alarmCountdownHandler.postDelayed(this, 30000); // Update every 0.5 minute
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Must initialize DB everywhere were we do CRUD operations
        dbHelper = DBHelper.getInstance(this);

        //dbHelper.deleteAllAlarms();
        //dbHelper.deleteAllRequestCodes();
        //requestCodeCreator = 1;

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

        //Setting Configuration Variables
        SharedPreferences sharedPref = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE);
        requestCodeCreator = sharedPref.getInt("nextRequestCode", 1);
        isLowPowerMode = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE).getBoolean("isLowPowerMode", false);
        isDoNotDisturb = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE).getBoolean("isDoNotDisturb", false);
        isMilitaryTimeFormat = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE).getBoolean("isMilitaryTimeFormat", false);
        activityThresholdMonitoringLevel = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE).getInt("activityThresholdMonitoringLevel", 1);
        activityMonitoringDuration = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE).getInt("activityMonitoringDuration", 0);
        //_______________________________________________________________________________________


        // ################## LISTENERS GO HERE #######################################

        // Exiting selection mode when the user clicks anywhere except an alarm card
        parentLayout.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                dbHelper.printAllRequestCodes(); // For debugging
                dbHelper.printAllAlarms(); // For debugging
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

        settingsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                sharedPref.edit().putBoolean("isLowPowerMode", isLowPowerMode).apply();
                sharedPref.edit().putBoolean("isDoNotDisturb", isDoNotDisturb).apply();
                sharedPref.edit().putBoolean("isMilitaryTimeFormat", isMilitaryTimeFormat).apply();
                sharedPref.edit().putInt("activityMonitoringDuration", activityMonitoringDuration).apply();
                sharedPref.edit().putInt("activityThresholdMonitoringLevel", activityThresholdMonitoringLevel).apply();

                Intent settingsIntent = new Intent(DashboardActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

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
        // Scheduling all active alarms
        rescheduleAllAlarms();
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
     * Re-schedules all alarms using AlarmManager. This function ensures that all alarms
     * are set to go off at the specified time.
     */
    public void rescheduleAllAlarms(){
        //dbHelper.deleteAllRequestCodes();
        int [] reqs = dbHelper.getAllRequestCodes();
        for (int req: reqs){
            cancelAlarmByRequestCode(req);
            dbHelper.deleteRequestCodeByRequestCode(req);
        }

        for (AlarmCard alarm: alarmList) {
            if (alarm.isActive())
                scheduleAlarm(alarm);
        }
        dbHelper.printAllRequestCodes();
    }

    public void scheduleAlarm(AlarmCard alarmCard){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // This intent is sent to AlarmReceiver.java with the alarm's ID as an extra
        Intent alarmReceiverIntent = new Intent(this, AlarmReceiver.class);
        alarmReceiverIntent.putExtra("alarmId", alarmCard.getId());

        // Check if the alarm is repeating
        if (!alarmCard.getRepeatingDays().equals("")) {
            scheduleMultipleAlarm(alarmCard, alarmManager, alarmReceiverIntent);

        } else {
            long alarmTimeInEpoch = convertToTimestamp(alarmCard.getTime(), alarmCard.getRepeatingDays());
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTimeInMillis(alarmTimeInEpoch);

            // Generate a unique request code
            int requestCode = generateRequestCode();
            System.out.println("SAVING REQUEST CODE SINGLE ALARM");
            dbHelper.addRequestCode(alarmCard.getTitle()+alarmCard.getTime()+alarmCard.getFormattedTime(), requestCode, "");

            // Use the unique request code for the PendingIntent
            PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, requestCode,
                    alarmReceiverIntent, PendingIntent.FLAG_IMMUTABLE);

            // Set a single alarm
            // Set a single, exact alarm

            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                                alarmTime.getTimeInMillis(), alarmPendingIntent);
                    } else {
                        // Show a dialog or notification to inform the user they need to grant the permission
                        // Redirect to the system settings for your app
                        Intent permissionIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        startActivity(permissionIntent);
                    }
                } else {
                    // For older versions, set the alarm without checking the permission
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), alarmPendingIntent);
                }
            }
        }
    }

    private void scheduleMultipleAlarm(AlarmCard alarmCard, AlarmManager alarmManager,
                                        Intent alarmReceiverIntent) {
        String[] days = alarmCard.getRepeatingDays().split(","); // Assuming this returns days like "Mo,Tu,We"

        for (String day : days) {
            int dayOfWeek = convertDayStringToCalendarDay(day);
            Calendar nextAlarmTime = getNextAlarmTime(alarmCard.getTime(), dayOfWeek);

            for (int weekOffset = 0; weekOffset < WEEKS_TO_SCHEDULE_AHEAD; weekOffset++){
                Calendar alarmTime = (Calendar) nextAlarmTime.clone();
                alarmTime.add(Calendar.WEEK_OF_YEAR, weekOffset);

                int requestCode = generateRequestCode();
                dbHelper.addRequestCode(alarmCard.getTitle()+alarmCard.getTime()+alarmCard.getFormattedTime(), requestCode, day);

                PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, requestCode,
                        alarmReceiverIntent, PendingIntent.FLAG_IMMUTABLE);

                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        alarmTime.getTimeInMillis(), alarmPendingIntent);
            }
        }
    }

    private synchronized int generateRequestCode(){
        int reqCode = requestCodeCreator++;
        saveRequestCode(requestCodeCreator); // saving it to SharedPreference for persistence.
        return reqCode;
    }

    /**
     * Every time you create and activate an alarm, we need a "request code" that goes along with
     * that alarm to help Android distinguish between alarms. It's like an alarm id.
     * We use the static variable "requestCodeCreator" to make request codes when needed. But since
     * it's a static variable, it will reset when restarting the app. Therefore, we need to save it
     * in SharedPreferences so that it doesn't reset. This makes it persistent.
     * @param requestCode
     */
    private void saveRequestCode(int requestCode){
        SharedPreferences sharedPreferences = getSharedPreferences("com.cs407.wakeguard",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("nextRequestCode", requestCodeCreator);
        editor.apply();
    }

    /**
     * Cancels an alarm such that it won't go off anymore.
     * @param alarmId
     */
    public void cancelAlarm(int alarmId){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent (this, AlarmReceiver.class);
        System.out.println("Cancelling: ------------------------");

        AlarmCard alarmToCancel = dbHelper.getAlarmById(alarmId);
        if (alarmToCancel != null && alarmToCancel.getRepeatingDays().equals("")){ // Canceling a non-repeating alarm
            // Cancelling non-repeating alarm
            String alarmIdentifier = alarmToCancel.getTitle()+alarmToCancel.getTime()+alarmToCancel.getFormattedTime();
            System.out.println("GENERATED KEY: " + alarmIdentifier);
            int nonRepeatingAlarmRequestCode = dbHelper.getRequestCode(alarmIdentifier);
            System.out.println("REQ CODE WE GOT: " + nonRepeatingAlarmRequestCode);
            // Deleting the request code from the DB
            dbHelper.deleteRequestCode(alarmIdentifier);
            PendingIntent nonRepeatingIntent = PendingIntent.getBroadcast(this,
                    nonRepeatingAlarmRequestCode, intent, PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(nonRepeatingIntent);
        }else { // Canceling a repeating alarm
            if (alarmToCancel == null){ // Unexpected error
                System.out.println("ERROR: ALARM NOT FOUND");
            }else{ // No errors encountered, deleting all instances of the repeating alarm.
                // Canceling the alarm if it's a repeated one
                for (int dayOfWeek = Calendar.SUNDAY; dayOfWeek <= Calendar.SATURDAY; dayOfWeek++){
                    for (int weekOffset = 0; weekOffset < WEEKS_TO_SCHEDULE_AHEAD; weekOffset++){
                        String alarmIdentifier = alarmToCancel.getTitle()+alarmToCancel.getTime()+alarmToCancel.getFormattedTime()+getDayString(dayOfWeek);
                        Object requestCode = dbHelper.getRequestCode(alarmIdentifier);
                        if (requestCode == null)
                            continue;
                        else{
                            dbHelper.deleteRequestCode(alarmIdentifier);
                            PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this,
                                    (int)requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
                            alarmManager.cancel(alarmPendingIntent);
                        }
                    }
                }
            }
        }
    }

    public void cancelAlarmByRequestCode(int reqCode){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent (this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                reqCode, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    private Calendar getNextAlarmTime(String time, int dayOfWeek) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        Calendar nextAlarm = Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        try {
            Date alarmTime = timeFormat.parse(time);
            nextAlarm.setTime(alarmTime);

            // Set initial date to today
            nextAlarm.set(Calendar.YEAR, now.get(Calendar.YEAR));
            nextAlarm.set(Calendar.MONTH, now.get(Calendar.MONTH));
            nextAlarm.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));

            // Set the day of the week
            nextAlarm.set(Calendar.DAY_OF_WEEK, dayOfWeek);

            // If the calculated alarm time is before the current time, set it for the next week
            if (nextAlarm.before(now)) {
                nextAlarm.add(Calendar.WEEK_OF_YEAR, 1);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // Handle exception - maybe return null or set a default alarm time
        }

        return nextAlarm;
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

    private int convertDayStringToCalendarDay(String day) {
        switch (day.trim()) {
            case "Su":
                return Calendar.SUNDAY;
            case "Mo":
                return Calendar.MONDAY;
            case "Tu":
                return Calendar.TUESDAY;
            case "We":
                return Calendar.WEDNESDAY;
            case "Th":
                return Calendar.THURSDAY;
            case "Fr":
                return Calendar.FRIDAY;
            case "Sa":
                return Calendar.SATURDAY;
            default:
                return -1; // Invalid day
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
                SharedPreferences sharedPref = getSharedPreferences("com.cs407.wakeguard",
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                sharedPref.edit().putBoolean("isLowPowerMode", isLowPowerMode).apply();
                sharedPref.edit().putBoolean("isDoNotDisturb", isDoNotDisturb).apply();
                sharedPref.edit().putBoolean("isMilitaryTimeFormat", isMilitaryTimeFormat).apply();
                sharedPref.edit().putInt("activityMonitoringDuration", activityMonitoringDuration).apply();
                sharedPref.edit().putInt("activityThresholdMonitoringLevel", activityThresholdMonitoringLevel).apply();

                Intent settingsIntent = new Intent(DashboardActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
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
                alarmToDelete.setActive(false);
                dbHelper.toggleAlarm(alarmToDelete.getId(), false);
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

        rescheduleAllAlarms();

        // Displaying a toast message to let user know that deletion was successful.
        if (numOfDeletedAlarms == 1){
            Toast.makeText(getApplicationContext(), "Alarm Deleted",
                    Toast.LENGTH_SHORT).show();
        }else if (numOfDeletedAlarms > 1){
            Toast.makeText(getApplicationContext(), numOfDeletedAlarms + " Alarms Deleted",
                    Toast.LENGTH_SHORT).show();
        }
        updateUpcomingAlarmText();
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
            dbHelper.toggleAlarm(currentAlarm.getId(), false);
        }
        adapter.notifyDataSetChanged();
        rescheduleAllAlarms();
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