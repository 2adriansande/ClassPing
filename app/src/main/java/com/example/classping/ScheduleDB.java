package com.example.classping;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class ScheduleDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "schedules.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "schedule";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DAY = "day";
    private static final String COLUMN_SUBJECT = "subject";
    private static final String COLUMN_PROGRAM = "program";
    private static final String COLUMN_ROOM = "room";
    private static final String COLUMN_START = "start_time";
    private static final String COLUMN_END = "end_time";

    public ScheduleDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_DAY + " TEXT," +
                COLUMN_SUBJECT + " TEXT," +
                COLUMN_PROGRAM + " TEXT," +
                COLUMN_ROOM + " TEXT," +
                COLUMN_START + " TEXT," +
                COLUMN_END + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Insert schedule
    public void insertSchedule(String day, String subject, String program, String room, String start, String end) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DAY, day);
        values.put(COLUMN_SUBJECT, subject);
        values.put(COLUMN_PROGRAM, program);
        values.put(COLUMN_ROOM, room);
        values.put(COLUMN_START, start);
        values.put(COLUMN_END, end);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    // Get all schedules
    public ArrayList<String> getSchedules() {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                String day = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY));
                String subject = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBJECT));
                String program = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROGRAM));
                String room = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROOM));
                String start = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START));
                String end = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END));

                String entry;
                if (!program.isEmpty() && !room.isEmpty()) {
                    entry = String.format("%s | %s [%s] (%s)", day, subject, program, room);
                } else if (!start.isEmpty() && !end.isEmpty()) {
                    entry = String.format("%s | %s (%s - %s)", day, subject, start, end);
                } else {
                    entry = String.format("%s | %s", day, subject);
                }
                list.add(entry);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public void clearSchedules() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

}
