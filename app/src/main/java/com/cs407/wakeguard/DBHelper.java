package com.cs407.wakeguard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * A singleton-pattern class for storing alarms.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "alarms.db";
    private static final int DATABASE_VERSION = 1;

    private static DBHelper instance;

    /**
     * This helps us in making this class use a singleton pattern.
     * Every time we want to initialize the DB in another class, we call this static function
     * like so: DBHelper.getinstance(this);
     *
     * @param context Context of the application where the DB is initialized.
     * @return the singleton instance of the DB
     */
    public static synchronized DBHelper getInstance(Context context){
        if (instance == null){
            instance = new DBHelper(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Private constructor to prevent direct instantiation
     *
     * @param context
     */
    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE_ALARMS_TABLE = "CREATE TABLE IF NOT EXISTS alarms " +
                "(id INTEGER PRIMARY KEY, " +
                "time TEXT, " +
                "repeatingDays TEXT, " +
                "title TEXT, " +
                "alarmToneName TEXT, " +
                "isVibrationOn INTEGER, " +
                "isMotionMonitoringOn INTEGER, " +
                "isActive INTEGER)";
        db.execSQL(CREATE_ALARMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Since we're not planning on upgrading the database, you can keep this simple.
        // However, it's good practice to handle the upgrade scenario.
    }

    /**
     * Takes an alarm object, strips its values, then adds it to the DB at a tuple
     *
     * @param alarmCard the alarm object to add to the DB
     */
    public void addAlarm(AlarmCard alarmCard){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("time", alarmCard.getTime());
        values.put("repeatingDays", String.join(", ", alarmCard.getRepeatingDays()));
        values.put("title", alarmCard.getTitle());
        values.put("alarmToneName", alarmCard.getAlarmTone());
        values.put("isVibrationOn", alarmCard.isVibrationOn() ? 1 : 0);
        values.put("isMotionMonitoringOn", alarmCard.isMotionMonitoringOn() ? 1 : 0);
        values.put("isActive", alarmCard.isActive() ? 1 : 0);

        db.insert("alarms", null, values);
        db.close();
    }

    /**
     * Deletes a single alarm from the DB given its ID.
     *
     * @param alarmID the id number of the alarm to delete.
     */
    public void deleteAlarm(int alarmID){
        SQLiteDatabase db = this.getWritableDatabase();

        // Defining the query's WHERE clause (the row to delete)
        String whereClause = "id=?";

        // Defining the WHERE arguments (values to bind to the WHERE clause, what goes in the spot of the ? mark)
        String [] whereArgs = new String[]{String.valueOf(alarmID)};

        // Performing the DELETE operation
        int deletedRows = db.delete("alarms", whereClause, whereArgs);

        //Closing DB connection.
        db.close();

    }

    /**
     * When tapping on an alarm to edit it, this function runs when you click the check mark button
     * @param alarmToUpdate
     */
    public void updateAlarm(AlarmCard alarmToUpdate){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("time", alarmToUpdate.getTime());
        values.put("repeatingDays", alarmToUpdate.getRepeatingDays());
        values.put("title", alarmToUpdate.getTitle());
        values.put("alarmToneName", alarmToUpdate.getAlarmTone());
        values.put("isVibrationOn", alarmToUpdate.isVibrationOn() ? 1 : 0);

        values.put("isMotionMonitoringOn", alarmToUpdate.isMotionMonitoringOn() ? 1 : 0);
        values.put("isActive", alarmToUpdate.isActive() ? 1 : 0);

        String whereClause = "id=?";

        String []  whereArgs = new String[]{String.valueOf(alarmToUpdate.getId())};

        db.update("alarms", values, whereClause, whereArgs);
        db.close();
    }

    /**
     * When you switch an alarm on or off, this function runs to update the corresponding alarm's
     * isActive value in the DB
     *
     * @param alarmID the ID of the alarm to switch on or off.
     * @param isActive The new state of the alarm.
     */
    public void toggleAlarm(int alarmID, boolean isActive){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("isActive", isActive ? 1 : 0); // SQLite uses 1 for true and 0 for false

        // Defining the WHERE clause to specify which row (i.e., alarm) to update
        String whereClause = "id=?";

        String[] whereArgs = new String[]{String.valueOf(alarmID)}; // The WHERE argument

        db.update("alarms", values, whereClause, whereArgs); // Performing the Update

        // Closing connection to DB
        db.close();
    }

    /**
     * This is mostly used to refresh the RecyclerView (the object containing the list of the alarms
     * appearing on the Dashboard).
     *
     * @return List containing all the alarms currently in the DB
     */
    public List<AlarmCard> getAllAlarms() {
        List<AlarmCard> alarmList = new ArrayList<>();
        String selectQuery = "SELECT * FROM alarms";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()){
            int idIndex = cursor.getColumnIndex("id");
            int timeIndex = cursor.getColumnIndex("time");
            int repeatingDaysIndex = cursor.getColumnIndex("repeatingDays");
            int titleIndex = cursor.getColumnIndex("title");
            int alarmToneNameIndex = cursor.getColumnIndex("alarmToneName");
            int isVibrationOnIndex = cursor.getColumnIndex("isVibrationOn");
            int isMotionMonitoringOnIndex = cursor.getColumnIndex("isMotionMonitoringOn");
            int isActiveIndex = cursor.getColumnIndex("isActive");

            if (idIndex != -1 && timeIndex != -1 && repeatingDaysIndex != -1 && titleIndex != -1 &&
                    alarmToneNameIndex != -1 && isVibrationOnIndex != -1 &&
                    isMotionMonitoringOnIndex != -1 && isActiveIndex != -1) {
                do {
                    AlarmCard alarmCard = new AlarmCard(
                            cursor.getInt(idIndex),
                            cursor.getString(timeIndex),
                            cursor.getString(repeatingDaysIndex),
                            cursor.getString(titleIndex),
                            cursor.getString(alarmToneNameIndex),
                            cursor.getInt(isVibrationOnIndex) == 1,
                            cursor.getInt(isMotionMonitoringOnIndex) == 1,
                            cursor.getInt(isActiveIndex) == 1
                    );
                    alarmList.add(alarmCard);
                }while (cursor.moveToNext());
            }else{
                Log.i("D", "ERROR: COLUMN WASN'T FOUND ###############");
            }
        }
        cursor.close();
        db.close();
        return alarmList;

    }

    /**
     * Unused for now, but might be necessary later
     * TODO: At the end of the development of the app, see if you still need this function, if not,
     *  delete it.
     *
     * @param alarmId
     * @return
     */
    public AlarmCard getAlarmById(int alarmId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("alarms", new String[]{"id", "time", "repeatingDays",
                "title", "alarmToneName", "isVibrationOn", "isMotionMonitoringOn",
                "isActive"}, "id=?", new String[]{String.valueOf(alarmId)}, null,
                null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex("id");
            int timeIndex = cursor.getColumnIndex("time");
            int repeatingDaysIndex = cursor.getColumnIndex("repeatingDays");
            int titleIndex = cursor.getColumnIndex("title");
            int alarmToneNameIndex = cursor.getColumnIndex("alarmToneName");
            int isVibrationOnIndex = cursor.getColumnIndex("isVibrationOn");
            int isMotionMonitoringOnIndex = cursor.getColumnIndex("isMotionMonitoringOn");
            int isActiveIndex = cursor.getColumnIndex("isActive");

            if (idIndex != -1 && timeIndex != -1 && repeatingDaysIndex != -1 && titleIndex != -1 &&
                    alarmToneNameIndex != -1 && isVibrationOnIndex != -1 &&
                    isMotionMonitoringOnIndex != -1 && isActiveIndex != -1){
                AlarmCard alarmCard = new AlarmCard(
                        cursor.getInt(idIndex),
                        cursor.getString(timeIndex),
                        cursor.getString(repeatingDaysIndex),
                        cursor.getString(titleIndex),
                        cursor.getString(alarmToneNameIndex),
                        cursor.getInt(isVibrationOnIndex) == 1,
                        cursor.getInt(isMotionMonitoringOnIndex) == 1,
                        cursor.getInt(isActiveIndex) == 1
                );
                cursor.close();
                return alarmCard;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    /**
     * For debugging purposes.
     * Neatly logs all the alarms (and their details) currently stored in the DB
     */
    public void printAllAlarms() {
        String selectQuery = "SELECT * FROM alarms";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex("id");
            int timeIndex = cursor.getColumnIndex("time");
            int repeatingDaysIndex = cursor.getColumnIndex("repeatingDays");
            int titleIndex = cursor.getColumnIndex("title");
            int alarmToneNameIndex = cursor.getColumnIndex("alarmToneName");
            int isVibrationOnIndex = cursor.getColumnIndex("isVibrationOn");
            int isMotionMonitoringOnIndex = cursor.getColumnIndex("isMotionMonitoringOn");
            int isActiveIndex = cursor.getColumnIndex("isActive");

            if (idIndex != -1 && timeIndex != -1 && repeatingDaysIndex != -1 && titleIndex != -1 &&
                    alarmToneNameIndex != -1 && isVibrationOnIndex != -1 &&
                    isMotionMonitoringOnIndex != -1 && isActiveIndex != -1) {
                do {
                    String alarmData = String.format("id: %d\n Time: %s,\n Days Active: %s,\n Title: %s,\n Alarm Tone Name: %s,\n " +
                                    "Is Vibration On: %s,\n Is Motion Monitoring On: %s,\n Is Active: %s\n\n\n",
                            cursor.getInt(idIndex),
                            cursor.getString(timeIndex),
                            cursor.getString(repeatingDaysIndex),
                            cursor.getString(titleIndex),
                            cursor.getString(alarmToneNameIndex),
                            cursor.getInt(isVibrationOnIndex) == 1 ? "Yes" : "No",
                            cursor.getInt(isMotionMonitoringOnIndex) == 1 ? "Yes" : "No",
                            cursor.getInt(isActiveIndex) == 1 ? "Yes" : "No");

                    Log.i("DBHelper", alarmData);
                } while (cursor.moveToNext());
            } else {
                Log.i("DBHelper", "ERROR: COLUMN WASN'T FOUND");
            }
        } else {
            Log.i("DBHelper", "No alarms found in the database.");
        }
        cursor.close();
        db.close();
    }

}
