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
    private static final int DATABASE_VERSION = 2;

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

        String CREATE_REQUEST_CODES_TABLE = "CREATE TABLE IF NOT EXISTS requestCodes " +
                "(id INTEGER PRIMARY KEY, " +
                "alarmIdentifier TEXT, " +
                "requestCode INTEGER)";
        db.execSQL(CREATE_REQUEST_CODES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS alarms");
        db.execSQL("DROP TABLE IF EXISTS requestCodes");
        onCreate(db);
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

        // Add request code now


        db.close();
    }

    // Old
    /*
    public void addRequestCode(String alarmIdentifier, int requestCode, String day){
        SQLiteDatabase db = this.getWritableDatabase();
        System.out.println("IN DB, PREPPING TO INSERT REQ CODE");
        ContentValues values = new ContentValues();

        if (day.equals(""))
            values.put("alarmIdentifier", alarmIdentifier);
        else
            values.put("alarmIdentifier", alarmIdentifier+day);

        values.put("requestCode", requestCode);

        db.insert("requestCodes", null, values);
        db.close();
    }

    */

    public void addRequestCode(String alarmIdentifier, int requestCode, String day){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try {
            // Construct the alarm identifier based on day
            String identifier = day.equals("") ? alarmIdentifier : alarmIdentifier + day;

            // Check if the alarm identifier already exists
            cursor = db.query("requestCodes", new String[]{"requestCode"}, "alarmIdentifier=?",
                    new String[]{identifier}, null, null, null);

            // If cursor is empty, add the requestCode
            if (cursor == null || !cursor.moveToFirst()) {
                ContentValues values = new ContentValues();
                values.put("alarmIdentifier", identifier);
                values.put("requestCode", requestCode);
                db.insert("requestCodes", null, values);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
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
     * Deletes all alarms from the database.
     */
    public void deleteAllAlarms() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("alarms", null, null);
        db.close();
    }

    public void deleteAllRequestCodes() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("requestCodes", null, null);
        db.close();
    }

    public void deleteRequestCode(String alarmIdentifier) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("requestCodes", "alarmIdentifier=?", new String[]{alarmIdentifier});
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

    public void updateRequestCode(String alarmIdentifier, int newRequestCode) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("requestCode", newRequestCode);

        db.update("requestCodes", values, "alarmIdentifier=?", new String[]{alarmIdentifier});
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

    public Integer getRequestCode(String alarmIdentifier){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query("requestCodes", new String[]{"requestCode"},
                "alarmIdentifier=?", new String[]{alarmIdentifier}, null,
                null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int requestCodeColumnIndex = cursor.getColumnIndex("requestCode");
            if (requestCodeColumnIndex != -1){
                int requestCode = cursor.getInt(requestCodeColumnIndex);
                cursor.close();
                return requestCode;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return null; // or a default value

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

    public void printAllRequestCodes() {
        System.out.println("REQ CODES DUMP:----------------------------------------------------------------------");
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM requestCodes";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex("id");
            int alarmIdentifierIndex = cursor.getColumnIndex("alarmIdentifier");
            int requestCodeIndex = cursor.getColumnIndex("requestCode");

            if (idIndex != -1 && alarmIdentifierIndex != -1 && requestCodeIndex != -1) {
                do {
                    String requestCodeData = String.format("ID: %d, Alarm Identifier: %s, Request Code: %d",
                            cursor.getInt(idIndex),
                            cursor.getString(alarmIdentifierIndex),
                            cursor.getInt(requestCodeIndex));

                    Log.i("DBHelper", requestCodeData);
                } while (cursor.moveToNext());
            } else {
                Log.i("DBHelper", "ERROR: COLUMN WASN'T FOUND");
            }
        } else {
            Log.i("DBHelper", "No request codes found in the database.");
        }
        cursor.close();
        db.close();
    }


}
