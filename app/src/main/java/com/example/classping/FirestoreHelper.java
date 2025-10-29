package com.example.classping;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles Firestore schedule uploads for ClassPing.
 */
public class FirestoreHelper {

    private static final String TAG = "FirestoreHelper";
    private final FirebaseFirestore db;

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Saves a schedule to Firestore under collection "schedules".
     */
    public void saveSchedule(Schedule schedule,
                             OnSuccessListener<Void> successListener,
                             OnFailureListener failureListener) {
        try {
            if (schedule == null) {
                Log.w(TAG, "Attempted to save null schedule.");
                return;
            }

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

            // Use deterministic ID
            String docId = schedule.getDay() + "_" +
                    schedule.getSubject().replaceAll("[^a-zA-Z0-9]", "_") + "_" +
                    schedule.getStartTime().replace(":", "-");

            db.collection("schedules")
                    .document(docId)
                    .set(data)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Saved schedule: " + docId);
                        if (successListener != null) successListener.onSuccess(aVoid);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Error saving schedule", e);
                        if (failureListener != null) failureListener.onFailure(e);
                    });

        } catch (Exception e) {
            Log.e(TAG, "saveSchedule Exception: ", e);
        }
    }
}
