package com.example.classping;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles Firestore schedule uploads for ClassPing.
 * Saves schedules under each user's subcollection:
 *    /users/{uid}/schedules/{scheduleId}
 * Adds computed "courseCode" = program + "-" + subject.
 */
public class FirestoreHelper {

    private static final String TAG = "FirestoreHelper";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Saves a schedule to Firestore under:
     * /users/{uid}/schedules/{scheduleId}
     */
    public void saveSchedule(Schedule schedule,
                             OnSuccessListener<Void> successListener,
                             OnFailureListener failureListener) {
        try {
            if (schedule == null) {
                Log.w(TAG, "⚠️ Attempted to save null schedule.");
                return;
            }

            // Validation
            if (schedule.getProgram() == null || schedule.getProgram().trim().isEmpty() ||
                    schedule.getSubject() == null || schedule.getSubject().trim().isEmpty()) {
                Log.w(TAG, "Skipped saving schedule — program and subject are required.");
                return;
            }

            if (auth.getCurrentUser() == null) {
                Log.w(TAG, "Cannot save schedule — no user logged in.");
                return;
            }

            String uid = auth.getCurrentUser().getUid();

            //  Build Firestore data
            Map<String, Object> data = new HashMap<>();
            data.put("day", schedule.getDay());
            data.put("subject", schedule.getSubject());
            data.put("program", schedule.getProgram());
            data.put("room", schedule.getRoom());
            data.put("startTime", schedule.getStartTime());
            data.put("endTime", schedule.getEndTime());
            data.put("notes", schedule.getNotes());
            data.put("reminder", schedule.getReminder());
            data.put("timestamp", System.currentTimeMillis());

            //  Add computed "courseCode"
            String courseCode = schedule.getCourseCode();
            if (courseCode == null || courseCode.trim().isEmpty()) {
                courseCode = schedule.getProgram().trim() + "-" + schedule.getSubject().trim();
            }
            data.put("courseCode", courseCode);

            //  Make it effectively final for lambdas
            final String finalCourseCode = courseCode;

            // Log it for confirmation
            Log.d(TAG, "Generated courseCode: " + finalCourseCode);

            // Generate document ID
            String scheduleId = db.collection("dummy").document().getId();

            //  Save inside user's schedule subcollection
            db.collection("users")
                    .document(uid)
                    .collection("schedules")
                    .document(scheduleId)
                    .set(data)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, " Schedule saved with courseCode: " + finalCourseCode);
                        if (successListener != null) successListener.onSuccess(aVoid);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, " Failed to save schedule", e);
                        if (failureListener != null) failureListener.onFailure(e);
                    });

        } catch (Exception e) {
            Log.e(TAG, "saveSchedule Exception: ", e);
        }
    }
}
