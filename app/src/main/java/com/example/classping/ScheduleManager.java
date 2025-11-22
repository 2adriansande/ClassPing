package com.example.classping;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScheduleManager {
    private static final String TAG = "ScheduleManager";

    private final DatabaseHelper db;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final Context context;

    public ScheduleManager(Context ctx) {
        db = new DatabaseHelper(ctx);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        this.context = ctx.getApplicationContext();
    }

    // ===============================
    // Local Database Operations
    // ===============================
    public void addSchedule(Schedule s) {
        if (s == null) {
            Log.w(TAG, "⚠️ Tried to add null schedule.");
            return;
        }

        // ✅ STRICT VALIDATION: must have both program and subject
        if (s.getProgram() == null || s.getProgram().trim().isEmpty() ||
                s.getSubject() == null || s.getSubject().trim().isEmpty()) {
            Log.w(TAG, "⛔ Skipped adding invalid schedule (missing program or subject).");
            return;
        }

        db.addSchedule(s);
        ReminderScheduler.scheduleReminder(context, s);
    }

    public void updateSchedule(int id, Schedule s) {
        if (s == null) return;

        if (s.getProgram() == null || s.getProgram().trim().isEmpty() ||
                s.getSubject() == null || s.getSubject().trim().isEmpty()) {
            Log.w(TAG, "⛔ Skipped update — program or subject missing.");
            return;
        }

        Schedule old = getScheduleById(id);
        if (old != null) ReminderScheduler.cancelReminder(context, old);

        db.updateSchedule(id, s);
        ReminderScheduler.scheduleReminder(context, s);
    }

    public void deleteSchedule(int id) {
        Schedule old = getScheduleById(id);
        if (old != null) ReminderScheduler.cancelReminder(context, old);
        db.deleteSchedule(id);
    }

    public List<Schedule> getAllSchedules() {
        return db.getAllSchedules();
    }

    public List<Schedule> getSchedulesForDay(String day) {
        return db.getSchedulesByDay(day);
    }

    // ===============================
    // Firestore Sync (User-only)
    // ===============================
    public void syncToFirebase() {
        List<Schedule> allSchedules = getAllSchedules();
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "No user logged in — skipping Firestore sync.");
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        final CollectionReference userSchedules =
                firestore.collection("users").document(uid).collection("schedules");

        userSchedules.get()
                .addOnSuccessListener(snaps -> {
                    Set<String> remoteKeys = new HashSet<>();
                    for (QueryDocumentSnapshot doc : snaps) {
                        Schedule s = doc.toObject(Schedule.class);
                        if (s != null) remoteKeys.add(makeKey(s));
                    }

                    int uploadedCount = 0, skippedInvalid = 0;

                    for (Schedule s : allSchedules) {
                        if (s == null ||
                                s.getProgram() == null || s.getProgram().trim().isEmpty() ||
                                s.getSubject() == null || s.getSubject().trim().isEmpty()) {
                            skippedInvalid++;
                            Log.w(TAG, "⏭️ Skipped Firestore sync — invalid schedule (missing program/subject).");
                            continue;
                        }

                        String key = makeKey(s);
                        if (remoteKeys.contains(key)) continue;

                        userSchedules.add(scheduleToMap(s))
                                .addOnSuccessListener(ref ->
                                        Log.d(TAG, "✅ Synced schedule to Firestore: " + ref.getId()))
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "❌ Failed to sync schedule", e));

                        uploadedCount++;
                    }

                    Log.i(TAG, "✅ Uploaded " + uploadedCount + " valid schedules. Skipped " + skippedInvalid + " invalid ones.");
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to read remote schedules before sync", e));
    }

    // ===============================
    // Firestore Fetch (User-only)
    // ===============================
    public void fetchFromFirebase(Runnable onComplete) {
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "No user logged in — skipping fetch.");
            if (onComplete != null) onComplete.run();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        firestore.collection("users")
                .document(uid)
                .collection("schedules")
                .get()
                .addOnSuccessListener(snaps -> {
                    if (snaps == null || snaps.isEmpty()) {
                        Log.d(TAG, "No schedules found in Firestore.");
                        if (onComplete != null) onComplete.run();
                        return;
                    }

                    List<Schedule> local = getAllSchedules();
                    Set<String> localKeys = new HashSet<>();
                    for (Schedule s : local) localKeys.add(makeKey(s));

                    int insertedCount = 0, skippedInvalid = 0;

                    for (QueryDocumentSnapshot doc : snaps) {
                        try {
                            Schedule s = doc.toObject(Schedule.class);
                            if (s == null) continue;

                            if (s.getProgram() == null || s.getProgram().trim().isEmpty() ||
                                    s.getSubject() == null || s.getSubject().trim().isEmpty()) {
                                skippedInvalid++;
                                continue;
                            }

                            // ✅ Auto-generate courseCode if missing
                            if (s.getCourseCode() == null || s.getCourseCode().trim().isEmpty()) {
                                s.setCourseCode(s.getProgram().trim() + "-" + s.getSubject().trim());
                            }

                            String key = makeKey(s);
                            if (!localKeys.contains(key)) {
                                db.addSchedule(s);
                                ReminderScheduler.scheduleReminder(context, s);
                                insertedCount++;
                            }
                        } catch (Exception ex) {
                            Log.w(TAG, "⚠️ Error parsing Firestore schedule: " + ex.getMessage());
                        }
                    }

                    Log.i(TAG, "✅ Fetched " + insertedCount + " valid schedules, skipped " + skippedInvalid + ".");
                    if (onComplete != null) onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch schedules", e);
                    if (onComplete != null) onComplete.run();
                });
    }

    // ===============================
    // Helpers
    // ===============================
    private String makeKey(Schedule s) {
        if (s == null) return "";
        String day = safe(s.getDay()).toLowerCase();
        String subj = safe(s.getSubject()).toLowerCase();
        String st = safe(s.getStartTime());
        String et = safe(s.getEndTime());
        return day + "|" + subj + "|" + st + "|" + et;
    }

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

    // ✅ Include courseCode in Firestore upload
    private Map<String, Object> scheduleToMap(Schedule s) {
        Map<String, Object> m = new HashMap<>();
        m.put("day", s.getDay());
        m.put("subject", s.getSubject());
        m.put("program", s.getProgram());
        m.put("room", s.getRoom());
        m.put("startTime", s.getStartTime());
        m.put("endTime", s.getEndTime());
        m.put("notes", s.getNotes());
        m.put("reminder", s.getReminder());
        m.put("timestamp", System.currentTimeMillis());

        // ✅ Add computed or stored courseCode
        String courseCode = s.getCourseCode();
        if (courseCode == null || courseCode.trim().isEmpty()) {
            courseCode = s.getProgram().trim() + "-" + s.getSubject().trim();
        }
        m.put("courseCode", courseCode);

        Log.d(TAG, "🔥 Prepared Firestore map with courseCode: " + courseCode);
        return m;
    }

    public Schedule getScheduleById(int id) {
        List<Schedule> all = db.getAllSchedules();
        if (all == null) return null;
        for (Schedule s : all) {
            if (s != null && s.getId() == id) return s;
        }
        return null;
    }
}
