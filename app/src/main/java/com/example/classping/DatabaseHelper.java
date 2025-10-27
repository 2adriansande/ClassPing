package com.example.classping;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "classping.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_SCHEDULE = "schedules";
    private static final String COL_ID = "id";
    private static final String COL_DAY = "day";
    private static final String COL_SUBJECT = "subject";
    private static final String COL_PROGRAM = "program";
    private static final String COL_ROOM = "room";
    private static final String COL_START = "start_time";
    private static final String COL_END = "end_time";
    private static final String COL_NOTES = "notes";
    private static final String COL_REMINDER = "reminder";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create = "CREATE TABLE " + TABLE_SCHEDULE + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DAY + " TEXT, " +
                COL_SUBJECT + " TEXT, " +
                COL_PROGRAM + " TEXT, " +
                COL_ROOM + " TEXT, " +
                COL_START + " TEXT, " +
                COL_END + " TEXT, " +
                COL_NOTES + " TEXT, " +
                COL_REMINDER + " TEXT)";
        db.execSQL(create);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULE);
        onCreate(db);
    }

    public long addSchedule(Schedule s) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_DAY, s.getDay());
        v.put(COL_SUBJECT, s.getSubject());
        v.put(COL_PROGRAM, s.getProgram());
        v.put(COL_ROOM, s.getRoom());
        v.put(COL_START, s.getStartTime());
        v.put(COL_END, s.getEndTime());
        v.put(COL_NOTES, s.getNotes());
        v.put(COL_REMINDER, s.getReminder());
        long id = db.insert(TABLE_SCHEDULE, null, v);
        db.close();
        return id;
    }

    public void updateSchedule(int id, Schedule s) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_DAY, s.getDay());
        v.put(COL_SUBJECT, s.getSubject());
        v.put(COL_PROGRAM, s.getProgram());
        v.put(COL_ROOM, s.getRoom());
        v.put(COL_START, s.getStartTime());
        v.put(COL_END, s.getEndTime());
        v.put(COL_NOTES, s.getNotes());
        v.put(COL_REMINDER, s.getReminder());
        db.update(TABLE_SCHEDULE, v, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteSchedule(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SCHEDULE, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<Schedule> getAllSchedules() {
        List<Schedule> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_SCHEDULE, null);
        if (c.moveToFirst()) {
            do {
                Schedule s = new Schedule(
                        c.getInt(c.getColumnIndexOrThrow(COL_ID)),
                        c.getString(c.getColumnIndexOrThrow(COL_DAY)),
                        c.getString(c.getColumnIndexOrThrow(COL_SUBJECT)),
                        c.getString(c.getColumnIndexOrThrow(COL_PROGRAM)),
                        c.getString(c.getColumnIndexOrThrow(COL_ROOM)),
                        c.getString(c.getColumnIndexOrThrow(COL_START)),
                        c.getString(c.getColumnIndexOrThrow(COL_END)),
                        c.getString(c.getColumnIndexOrThrow(COL_NOTES)),
                        c.getString(c.getColumnIndexOrThrow(COL_REMINDER))
                );
                list.add(s);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public List<Schedule> getSchedulesByDay(String day) {
        List<Schedule> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_SCHEDULE, null, COL_DAY + "=?", new String[]{day}, null, null, COL_START + " ASC");
        if (c.moveToFirst()) {
            do {
                Schedule s = new Schedule(
                        c.getInt(c.getColumnIndexOrThrow(COL_ID)),
                        c.getString(c.getColumnIndexOrThrow(COL_DAY)),
                        c.getString(c.getColumnIndexOrThrow(COL_SUBJECT)),
                        c.getString(c.getColumnIndexOrThrow(COL_PROGRAM)),
                        c.getString(c.getColumnIndexOrThrow(COL_ROOM)),
                        c.getString(c.getColumnIndexOrThrow(COL_START)),
                        c.getString(c.getColumnIndexOrThrow(COL_END)),
                        c.getString(c.getColumnIndexOrThrow(COL_NOTES)),
                        c.getString(c.getColumnIndexOrThrow(COL_REMINDER))
                );
                list.add(s);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }
}
