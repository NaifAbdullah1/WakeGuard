package com.cs407.wakeguard;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

public class AlarmEditorActivity extends AppCompatActivity {
    TimePicker picker;
    TextView selectedDateText;
    int year;
    int month;
    int dayOfMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_editor);

        // Get 24-hour mode from SharedPreferences. Use it below to set time picker 24 hour mode
        SharedPreferences sp = getSharedPreferences("com.cs407.wakeguard", Context.MODE_PRIVATE);
        Boolean mode24Hr = sp.getBoolean("24hourMode", false);

        // TODO Test alarm tone
        //Uri ringtoneUri  = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_NOTIFICATION);
        //MediaPlayer mp = MediaPlayer.create(getApplicationContext(), ringtoneUri);
        //mp.start();
        // TODO Ringtone version instead of MediaPlayer (Don't use this one, but maybe it'd be useful for notification and/or alarm)
        //Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
        //r.play();

        Intent intent = getIntent();

        // Get the current (or existing alarm's) year, month, and day of month and set dateText accordingly
        Calendar c = Calendar.getInstance();
        year = intent.getIntExtra("year", c.get(Calendar.YEAR));
        month = intent.getIntExtra("month", c.get(Calendar.MONTH));
        dayOfMonth = intent.getIntExtra("dayOfMonth", c.get(Calendar.DAY_OF_MONTH));
        selectedDateText = (TextView) findViewById(R.id.dateText);
        String dateStr = DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime());
        if((year == c.get(Calendar.YEAR)) &&
           (month == c.get(Calendar.MONTH)) &&
           (dayOfMonth == c.get(Calendar.DAY_OF_MONTH))) {
            selectedDateText.setText("Today - " + dateStr);
        } else {
            selectedDateText.setText(dateStr);
        }

        // Set time picker to show 6am or the existing alarm's time
        picker = (TimePicker) findViewById(R.id.timePicker);
        picker.setHour(intent.getIntExtra("hour", 6));
        picker.setMinute(intent.getIntExtra("minute", 0));
        picker.setIs24HourView(mode24Hr);

        // TODO Use setOnTimeChangedListener() to listen for time changes and move the date to tomorrow if the chosen time is less than the current time
        // TODO Should also check the time differences when saving the alarm so that the date is changed if the chosen time ends up being sooner than the current time (e.g. if the user chooses a minute past the current time and then sets the alarm 2 minutes later)

        // TODO Use picker.getMinute() to get currently selected minute
        // TODO Use picker.getHour() to get currently selected hour
    }

    public void openDatePicker(View v) {
        // Create DatePickerDialog
        DatePickerDialog dpDialog = new DatePickerDialog(
                this,
                0, // TODO See if we want to use a different theme
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker dp, int y, int m, int dom) {
                        Calendar tempCal = Calendar.getInstance();
                        Boolean today = (y == tempCal.get(Calendar.YEAR)) &&
                                        (m == tempCal.get(Calendar.MONTH)) &&
                                        (dom == tempCal.get(Calendar.DAY_OF_MONTH));
                        tempCal.set(Calendar.YEAR, y);
                        tempCal.set(Calendar.MONTH, m);
                        tempCal.set(Calendar.DAY_OF_MONTH, dom);
                        year = y;
                        month = m;
                        dayOfMonth = dom;
                        String dateStr = DateFormat.getDateInstance(DateFormat.FULL).format(tempCal.getTime());
                        if(today) {
                            selectedDateText.setText("Today - " + dateStr);
                        } else {
                            selectedDateText.setText(dateStr);
                        }
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
}