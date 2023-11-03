package com.cs407.wakeguard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;


public class AlarmEditorActivity extends AppCompatActivity {
    // Switch for enabling/disabling vibration
    SwitchCompat vibrationSwitch;
    // Switch for enabling/disabling motion monitoring
    SwitchCompat motionMonitoringSwitch;
    // EditText for alarm name
    EditText alarmNameText;
    TimePicker tPicker;
    // TextView showing selected alarm date or repeating days
    TextView selectedDateText;
    // Whether or not the alarm will repeat on one or more days
    private boolean repeating;
    // Date chosen in DatePicker (for convenience with scrolling between times today vs tomorrow)
    private Calendar dpDate;
    // Actual alarm date
    int year, month, dayOfMonth;
    // Constants for Calendar fields
    private final int YEAR = Calendar.YEAR,
                      DAY_OF_YEAR = Calendar.DAY_OF_YEAR,
                      MONTH = Calendar.MONTH,
                      DAY_OF_MONTH = Calendar.DAY_OF_MONTH;
    // Strings for selectedDateText when repeating on one or more days
    private final String[] dayOfWeekStrings = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    // Days on which the alarm repeats: Sun    Mon    Tue    Wed    Thu    Fri    Sat
    private boolean[] repeatingDays = {false, false, false, false, false, false, false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_editor);

        // Get 24-hour mode from SharedPreferences. Use it below to set time picker 24 hour mode
        SharedPreferences sp = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE);
        Boolean mode24Hr = sp.getBoolean("24hourMode", false);

        Intent intent = getIntent();

        // Initialize components with little setup
        vibrationSwitch = (SwitchCompat) findViewById(R.id.vibrationSwitch);
        vibrationSwitch.setChecked(intent.getBooleanExtra("vibration", true));
        motionMonitoringSwitch = (SwitchCompat) findViewById(R.id.motionMonitorSwitch);
        motionMonitoringSwitch.setChecked(intent.getBooleanExtra("motionMonitoring", true));
        alarmNameText = (EditText) findViewById(R.id.alarmNameText);
        alarmNameText.setText(intent.getStringExtra("alarmName"));

        // Set time picker to show 6am or the existing alarm's time
        tPicker = (TimePicker) findViewById(R.id.timePicker);
        tPicker.setHour(intent.getIntExtra("hour", 6));
        tPicker.setMinute(intent.getIntExtra("minute", 0));
        tPicker.setIs24HourView(mode24Hr);

        // If an existing alarm was clicked on the home page,
        // repeated days will be sent to this activity via an Intent
        boolean[] tempRepeatingDays = intent.getBooleanArrayExtra("repeatingDays");
        repeating = false;
        // Check if intent extra with repeating days was sent
        if(tempRepeatingDays != null) {
            // Check for at least one repeating day
            for(int i=0; i < tempRepeatingDays.length; i++) {
                repeatingDays[i] = tempRepeatingDays[i];
                if(tempRepeatingDays[i]) {
                    repeating = true;
                }
            }
        }

        selectedDateText = (TextView) findViewById(R.id.dateText);
        if(repeating) {
            // Repeating at least one day. Set dateText and buttons for repeating days accordingly
            selectedDateText.setText(getRepeatingDaysString());
            ((ToggleButton)findViewById(R.id.sundayBtn)).setChecked(repeatingDays[0]);
            ((ToggleButton)findViewById(R.id.mondayBtn)).setChecked(repeatingDays[1]);
            ((ToggleButton)findViewById(R.id.tuesdayBtn)).setChecked(repeatingDays[2]);
            ((ToggleButton)findViewById(R.id.wednesdayBtn)).setChecked(repeatingDays[3]);
            ((ToggleButton)findViewById(R.id.thursdayBtn)).setChecked(repeatingDays[4]);
            ((ToggleButton)findViewById(R.id.fridayBtn)).setChecked(repeatingDays[5]);
            ((ToggleButton)findViewById(R.id.saturdayBtn)).setChecked(repeatingDays[6]);
        } else {
            // Not repeating any days
            // Set DatePicker date against current alarm date or today
            // Setting to today allows for cycling between today and tomorrow using TimePicker initially
            dpDate = Calendar.getInstance();
            dpDate.set(YEAR, intent.getIntExtra("year", dpDate.get(YEAR)));
            dpDate.set(MONTH, intent.getIntExtra("month", dpDate.get(MONTH)));
            dpDate.set(DAY_OF_MONTH, intent.getIntExtra("dayOfMonth", dpDate.get(DAY_OF_MONTH)));

            // Get a valid date using the DatePicker initial date (existing alarm's date or today)
            // and the TimePicker's initial time
            // This ensures a valid initial alarm date given the initial time
            Calendar c = getValidDate(dpDate.get(YEAR), dpDate.get(MONTH), dpDate.get(DAY_OF_MONTH));
            year = c.get(YEAR);
            month = c.get(MONTH);
            dayOfMonth = c.get(DAY_OF_MONTH);

            // Set dateText according to the initial date
            Calendar today = Calendar.getInstance();
            String dateStr = DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime());
            if((year == today.get(YEAR)) &&
               (month == today.get(MONTH)) &&
               (dayOfMonth == today.get(DAY_OF_MONTH))) {
                selectedDateText.setText("Today - " + dateStr);
            } else {
                selectedDateText.setText(dateStr);
            }
        }

        // Set TimePicker onTimeChangedListener
        tPicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                Calendar c = Calendar.getInstance();
                // Check that there are no repeating days and that DatePicker selected date is today
                if(!repeating && dpDate.compareTo(c) <= 0) {
                    // Get valid date (today or tomorrow) according to the current time and chosen alarm time
                    c = getValidDate(dpDate.get(YEAR), dpDate.get(MONTH), dpDate.get(DAY_OF_MONTH));
                    year = c.get(YEAR);
                    month = c.get(MONTH);
                    dayOfMonth = c.get(DAY_OF_MONTH);
                    // Check if returned Calendar points to today
                    if(c.compareTo(Calendar.getInstance()) <= 0) {
                        // Setting alarm for today
                        selectedDateText.setText("Today - " + DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime()));
                    } else {
                        // Setting alarm for later date
                        selectedDateText.setText(DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime()));
                    }
                }
            }
        });

        // Spinner for selecting the tone of the alarm
        Spinner alarmToneSpinner = (Spinner) findViewById(R.id.alarmToneSpinner);
        // Create an ArrayAdapter with the default alarm tones
        String[] defaultAlarmTones = {"Default", "None"}; // TODO
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, defaultAlarmTones); // TODO Use 'this' instead of getApplicationContext()?
        // TODO Populate entries based off available alarm tone options

        // TODO Set initial tone to existing alarm's tone or the default
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        alarmToneSpinner.setAdapter(adapter);

        // TODO Set listener for item clicked

        // TODO Test alarm tone. Can use this to play tone when an item is selected
        //Uri ringtoneUri  = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_NOTIFICATION);
        //MediaPlayer mp = MediaPlayer.create(getApplicationContext(), ringtoneUri);
        //mp.start();
        // TODO Ringtone version instead of MediaPlayer (Don't use this one, but maybe it'd be useful for notification and/or alarm)
        //Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
        //r.play();
    }

    private String getRepeatingDaysString() {
        String repeatingDaysStr = "Every";
        int repCount = 0;

        // Add repeating days to the string
        for(int i=0; i < repeatingDays.length; i++) {
            if(repeatingDays[i]) {
                // Add commas before second and later repeating days
                if(repCount > 0) {
                    repeatingDaysStr += ",";
                }
                repeatingDaysStr += " " + dayOfWeekStrings[i];
                repCount++;
            }
        }

        if(repCount == dayOfWeekStrings.length) {
            return "Every day";
        }
        return repeatingDaysStr;
    }

    private Calendar getValidDate(int y, int m, int dom) {
        Calendar c = Calendar.getInstance();

        if((y == c.get(YEAR)) &&
           (m == c.get(MONTH)) &&
           (dom == c.get(DAY_OF_MONTH))) {
            // Passed date is today
            final int actualTimeOfDay = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
            final int selectedTimeOfDay = tPicker.getHour() * 60 + tPicker.getMinute();

            c.set(YEAR, y);
            c.set(MONTH, m);
            c.set(DAY_OF_MONTH, dom);

            // Only allow the date to be set for the current day if the selected alarm
            // time is less than the actual time
            if(actualTimeOfDay >= selectedTimeOfDay) {
                // Change date to tomorrow
                c.roll(DAY_OF_YEAR, true);
                // Handle moving to a new year (other fields are handled correctly)
                if((c.get(MONTH) == 0) && (c.get(DAY_OF_MONTH) == 1)) {
                    c.roll(YEAR, true);
                }
            }
        } else {
            c.set(YEAR, y);
            c.set(MONTH, m);
            c.set(DAY_OF_MONTH, dom);
        }

        return c;
    }

    public void openDatePicker(View v) {
        // Validate previously chosen date date before creating DatePickerDialog
        Calendar c = getValidDate(dpDate.get(YEAR), dpDate.get(MONTH), dpDate.get(DAY_OF_MONTH));
        year = c.get(YEAR);
        month = c.get(MONTH);
        dayOfMonth = c.get(DAY_OF_MONTH);

        // If time has skewed enough to change the valid date to tomorrow, update dateText
        if(!repeating && (c.compareTo(Calendar.getInstance()) > 0)) {
            selectedDateText.setText(DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime()));
        }

        // Create DatePickerDialog
        DatePickerDialog dpDialog = new DatePickerDialog(
                this,
                0,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker dp, int y, int m, int dom) {
                        // Set DatePicker date to actually selected date
                        dpDate = Calendar.getInstance();
                        dpDate.set(YEAR, y);
                        dpDate.set(MONTH, m);
                        dpDate.set(DAY_OF_MONTH, dom);
                        // Check date according to selected alarm time
                        Calendar c = getValidDate(y, m, dom);

                        year = c.get(YEAR);
                        month = c.get(MONTH);
                        dayOfMonth = c.get(DAY_OF_MONTH);
                        // Check if returned Calendar points to today
                        if(c.compareTo(Calendar.getInstance()) <= 0) {
                            // Setting alarm for today
                            selectedDateText.setText("Today - " + DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime()));
                        } else {
                            // Setting alarm for later date
                            selectedDateText.setText(DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime()));
                        }

                        // Reset repeating days
                        repeating = false;
                        for(int i=0; i < repeatingDays.length; i++) {
                            repeatingDays[i] = false;
                        }
                        ((ToggleButton)findViewById(R.id.sundayBtn)).setChecked(repeatingDays[0]);
                        ((ToggleButton)findViewById(R.id.mondayBtn)).setChecked(repeatingDays[1]);
                        ((ToggleButton)findViewById(R.id.tuesdayBtn)).setChecked(repeatingDays[2]);
                        ((ToggleButton)findViewById(R.id.wednesdayBtn)).setChecked(repeatingDays[3]);
                        ((ToggleButton)findViewById(R.id.thursdayBtn)).setChecked(repeatingDays[4]);
                        ((ToggleButton)findViewById(R.id.fridayBtn)).setChecked(repeatingDays[5]);
                        ((ToggleButton)findViewById(R.id.saturdayBtn)).setChecked(repeatingDays[6]);
                    }
                },
                year,
                month,
                dayOfMonth
        );

        // Only allow today and future dates to be chosen for the alarm
        dpDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());

        // Show DatePickerDialog
        dpDialog.show();
    }

    public void setRepeatingDay(View v) {
        if(v.getId() == R.id.sundayBtn) {
            repeatingDays[0] = ((ToggleButton)v).isChecked();
        } else if(v.getId() == R.id.mondayBtn) {
            repeatingDays[1] = ((ToggleButton)v).isChecked();
        } else if(v.getId() == R.id.tuesdayBtn) {
            repeatingDays[2] = ((ToggleButton)v).isChecked();
        } else if(v.getId() == R.id.wednesdayBtn) {
            repeatingDays[3] = ((ToggleButton)v).isChecked();
        } else if(v.getId() == R.id.thursdayBtn) {
            repeatingDays[4] = ((ToggleButton)v).isChecked();
        } else if(v.getId() == R.id.fridayBtn) {
            repeatingDays[5] = ((ToggleButton)v).isChecked();
        } else if(v.getId() == R.id.saturdayBtn) {
            repeatingDays[6] = ((ToggleButton)v).isChecked();
        } else {
            return;
        }

        // Check for at least one repeating day
        repeating = false;
        for(int i=0; i < repeatingDays.length; i++) {
            if(repeatingDays[i]) {
                repeating = true;
                break;
            }
        }

        // Reset date to today (or tomorrow if alarm is set too early for today)
        // This is to reset the DatePicker selection
        dpDate = Calendar.getInstance();
        Calendar c = getValidDate(dpDate.get(YEAR), dpDate.get(MONTH), dpDate.get(DAY_OF_MONTH));
        year = c.get(YEAR);
        month = c.get(MONTH);
        dayOfMonth = c.get(DAY_OF_MONTH);
        if(repeating) {
            // Set dateText to repeating days
            selectedDateText.setText(getRepeatingDaysString());
        } else {
            // Check if returned Calendar points to today
            if(c.compareTo(Calendar.getInstance()) <= 0) {
                // Setting alarm for today
                selectedDateText.setText("Today - " + DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime()));
            } else {
                // Setting alarm for tomorrow
                selectedDateText.setText(DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime()));
            }
        }
    }

    public void saveAlarm(View v) {
        // TODO Save alarm settings in database
        //      Account for other user settings in SharedPreferences
        //      Validate alarm date/time to ensure passing time does not result in saving an alarm set to go off before the current date/time
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void discardChanges(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}