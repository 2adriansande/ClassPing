package com.example.classping;

import android.content.Context;
import java.util.List;

public class ScheduleManager {
    private final DatabaseHelper db;
    public ScheduleManager(Context ctx) { db = new DatabaseHelper(ctx); }
    public void addSchedule(Schedule s) { db.addSchedule(s); }
    public void updateSchedule(int id, Schedule s) { db.updateSchedule(id, s); }
    public void deleteSchedule(int id) { db.deleteSchedule(id); }
    public List<Schedule> getAllSchedules() { return db.getAllSchedules(); }
    public List<Schedule> getSchedulesForDay(String day) { return db.getSchedulesByDay(day); }
}
