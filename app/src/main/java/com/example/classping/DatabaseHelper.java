package com.example.classping;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.classping.announcements.Announcement;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "classping.db";
    private static final int DATABASE_VERSION = 3;

    // ==========================
    // TABLE: Schedule
    // ==========================
    private static final String TABLE_SCHEDULE = "schedule";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DAY = "day";
    private static final String COLUMN_SUBJECT = "subject";
    private static final String COLUMN_PROGRAM = "program";
    private static final String COLUMN_ROOM = "room";
    private static final String COLUMN_START = "start_time";
    private static final String COLUMN_END = "end_time";
    private static final String COLUMN_NOTES = "notes";
    private static final String COLUMN_REMINDER = "reminder";

    // ==========================
    // TABLE: Announcements
    // ==========================
    private static final String TABLE_ANNOUNCEMENTS = "announcements";
    private static final String ANNOUNCE_ID = "id";
    private static final String ANNOUNCE_TITLE = "title";
    private static final String ANNOUNCE_MESSAGE = "message";
    private static final String ANNOUNCE_DEPARTMENT = "department";
    private static final String ANNOUNCE_AUTHOR = "author";
    private static final String ANNOUNCE_TIMESTAMP = "timestamp";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Schedule Table
        String CREATE_SCHEDULE_TABLE = "CREATE TABLE " + TABLE_SCHEDULE + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_DAY + " TEXT," +
                COLUMN_SUBJECT + " TEXT," +
                COLUMN_PROGRAM + " TEXT," +
                COLUMN_ROOM + " TEXT," +
                COLUMN_START + " TEXT," +
                COLUMN_END + " TEXT," +
                COLUMN_NOTES + " TEXT," +
                COLUMN_REMINDER + " TEXT)";
        db.execSQL(CREATE_SCHEDULE_TABLE);

        // Create Announcements Table
        String CREATE_ANNOUNCEMENTS_TABLE = "CREATE TABLE " + TABLE_ANNOUNCEMENTS + "(" +
                ANNOUNCE_ID + " TEXT PRIMARY KEY," +
                ANNOUNCE_TITLE + " TEXT," +
                ANNOUNCE_MESSAGE + " TEXT," +
                ANNOUNCE_DEPARTMENT + " TEXT," +
                ANNOUNCE_AUTHOR + " TEXT," +
                ANNOUNCE_TIMESTAMP + " INTEGER)";
        db.execSQL(CREATE_ANNOUNCEMENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANNOUNCEMENTS);
        onCreate(db);
    }

    // ==========================
    // SCHEDULE METHODS
    // ==========================
    public void addSchedule(Schedule s) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DAY, s.getDay());
        values.put(COLUMN_SUBJECT, s.getSubject());
        values.put(COLUMN_PROGRAM, s.getProgram());
        values.put(COLUMN_ROOM, s.getRoom());
        values.put(COLUMN_START, s.getStartTime());
        values.put(COLUMN_END, s.getEndTime());
        values.put(COLUMN_NOTES, s.getNotes());
        values.put(COLUMN_REMINDER, s.getReminder());
        db.insert(TABLE_SCHEDULE, null, values);
        db.close();
    }

    public void updateSchedule(int id, Schedule s) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DAY, s.getDay());
        values.put(COLUMN_SUBJECT, s.getSubject());
        values.put(COLUMN_PROGRAM, s.getProgram());
        values.put(COLUMN_ROOM, s.getRoom());
        values.put(COLUMN_START, s.getStartTime());
        values.put(COLUMN_END, s.getEndTime());
        values.put(COLUMN_NOTES, s.getNotes());
        values.put(COLUMN_REMINDER, s.getReminder());
        db.update(TABLE_SCHEDULE, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteSchedule(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SCHEDULE, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<Schedule> getAllSchedules() {
        List<Schedule> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SCHEDULE, null);

        if (cursor.moveToFirst()) {
            do {
                Schedule s = new Schedule(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBJECT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROGRAM)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROOM)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER))
                );
                list.add(s);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    public List<Schedule> getSchedulesByDay(String day) {
        List<Schedule> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SCHEDULE + " WHERE " + COLUMN_DAY + "=?", new String[]{day});

        if (cursor.moveToFirst()) {
            do {
                Schedule s = new Schedule(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBJECT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROGRAM)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROOM)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_END)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER))
                );
                list.add(s);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    /**
     * Add or update schedule based on uniqueness (day + subject + start_time).
     */
    public void addOrUpdateSchedule(Schedule s) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(
                TABLE_SCHEDULE,
                new String[]{COLUMN_ID},
                COLUMN_DAY + "=? AND " + COLUMN_SUBJECT + "=? AND " + COLUMN_START + "=?",
                new String[]{s.getDay(), s.getSubject(), s.getStartTime()},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            updateSchedule(id, s);
            cursor.close();
        } else {
            addSchedule(s);
        }

        db.close();
    }

    // ==========================
    // ANNOUNCEMENT METHODS
    // ==========================
    public void addAnnouncement(Announcement announcement) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ANNOUNCE_ID, announcement.getId());
        values.put(ANNOUNCE_TITLE, announcement.getTitle());
        values.put(ANNOUNCE_MESSAGE, announcement.getMessage());
        values.put(ANNOUNCE_DEPARTMENT, announcement.getDepartment());
        values.put(ANNOUNCE_AUTHOR, announcement.getAuthor());
        values.put(ANNOUNCE_TIMESTAMP, announcement.getTimestamp());
        db.insertWithOnConflict(TABLE_ANNOUNCEMENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public List<Announcement> getAllAnnouncements() {
        List<Announcement> announcements = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ANNOUNCEMENTS + " ORDER BY " + ANNOUNCE_TIMESTAMP + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Announcement a = new Announcement(
                        cursor.getString(cursor.getColumnIndexOrThrow(ANNOUNCE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ANNOUNCE_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ANNOUNCE_MESSAGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ANNOUNCE_DEPARTMENT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ANNOUNCE_AUTHOR)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(ANNOUNCE_TIMESTAMP))
                );
                announcements.add(a);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return announcements;
    }

    public void deleteAnnouncement(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ANNOUNCEMENTS, ANNOUNCE_ID + "=?", new String[]{id});
        db.close();
    }
}
