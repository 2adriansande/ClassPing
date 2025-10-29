package com.example.classping;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReminderScheduler {

    private static final String TAG = "ReminderScheduler";

    /**
     * Schedule a single reminder for the provided schedule.
     * Reminder is set 10 minutes before start time.
     */
    public static void scheduleReminder(Context context, Schedule s) {
        if (s == null) return;
        try {
            String startTimeRaw = s.getStartTime(); // expected "hh:mm AM" or "HH:mm"
            Calendar classTime = parseScheduleTimeToCalendar(startTimeRaw, s.getDay());
            if (classTime == null) {
                Log.w(TAG, "Unable to parse start time: " + startTimeRaw);
                return;
            }

            // Reminder time: 10 minutes before
            classTime.add(Calendar.MINUTE, -10);

            // If already passed, skip scheduling
            if (classTime.before(Calendar.getInstance())) {
                Log.d(TAG, "Reminder time already passed for: " + s.getSubject());
                return;
            }

            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("subject", s.getSubject());
            intent.putExtra("startTime", s.getStartTime());
            intent.putExtra("day", s.getDay());

            int requestCode = makeRequestCode(s);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        classTime.getTimeInMillis(),
                        pendingIntent
                );
                Log.d(TAG, "Scheduled reminder (req=" + requestCode + ") for " + s.getSubject() + " at " + classTime.getTime());
            }
        } catch (Exception e) {
            Log.e(TAG, "scheduleReminder exception", e);
        }
    }

    /**
     * Cancel a previously scheduled reminder for a schedule.
     */
    public static void cancelReminder(Context context, Schedule s) {
        if (s == null) return;
        try {
            int requestCode = makeRequestCode(s);
            Intent intent = new Intent(context, ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );
            if (pendingIntent != null) {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    alarmManager.cancel(pendingIntent);
                }
                pendingIntent.cancel();
                Log.d(TAG, "Canceled reminder (req=" + requestCode + ") for " + s.getSubject());
            } else {
                Log.d(TAG, "No existing pending intent to cancel for req=" + requestCode);
            }
        } catch (Exception e) {
            Log.e(TAG, "cancelReminder exception", e);
        }
    }

    /**
     * Create deterministic request code for PendingIntent based on schedule identity.
     */
    public static int makeRequestCode(Schedule s) {
        String key = (s.getDay() == null ? "" : s.getDay()) + "|" +
                (s.getSubject() == null ? "" : s.getSubject()) + "|" +
                (s.getStartTime() == null ? "" : s.getStartTime());
        return key.hashCode();
    }

    /**
     * Parse various time formats into a Calendar object set to the upcoming day matching `dayName`.
     * Accepts "hh:mm a" (e.g. 10:30 AM) or "HH:mm" (24-hour).
     */
    private static Calendar parseScheduleTimeToCalendar(String timeRaw, String dayName) {
        if (timeRaw == null || timeRaw.trim().isEmpty()) return null;
        String[] patterns = {"hh:mm a", "h:mm a", "HH:mm", "H:mm"};
        Calendar now = Calendar.getInstance();
        Calendar cal = Calendar.getInstance();

        // Set calendar to the next date that matches dayName (0..6 offset)
        int targetDay = dayToCalendar(dayName);
        int today = now.get(Calendar.DAY_OF_WEEK);
        int offset = (targetDay - today + 7) % 7;
        cal.add(Calendar.DAY_OF_MONTH, offset);

        ParseException lastEx = null;
        for (String p : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(p, Locale.getDefault());
                sdf.setLenient(true);
                java.util.Date dt = sdf.parse(timeRaw);
                if (dt == null) continue;
                Calendar temp = Calendar.getInstance();
                temp.setTime(dt);
                int hh = temp.get(Calendar.HOUR_OF_DAY);
                int mm = temp.get(Calendar.MINUTE);
                cal.set(Calendar.HOUR_OF_DAY, hh);
                cal.set(Calendar.MINUTE, mm);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                return cal;
            } catch (ParseException e) {
                lastEx = e;
            }
        }
        // fallback: return null (parsing failed)
        return null;
    }

    private static int dayToCalendar(String day) {
        if (day == null) return Calendar.MONDAY;
        switch (day.trim().toLowerCase(Locale.ROOT)) {
            case "sunday": return Calendar.SUNDAY;
            case "monday": return Calendar.MONDAY;
            case "tuesday": return Calendar.TUESDAY;
            case "wednesday": return Calendar.WEDNESDAY;
            case "thursday": return Calendar.THURSDAY;
            case "friday": return Calendar.FRIDAY;
            case "saturday": return Calendar.SATURDAY;
            default: return Calendar.MONDAY;
        }
    }
}
