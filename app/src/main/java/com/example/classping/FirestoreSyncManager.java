package com.example.classping;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages real-time Firestore schedule synchronization and updates.
 * This class listens for any changes in the user's "schedules" subcollection
 * and updates local data accordingly.
 */
public class FirestoreSyncManager {

    private static final String TAG = "FirestoreSyncManager";

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final Context context;
    private ListenerRegistration registration;
    private final ScheduleManager scheduleManager;

    public FirestoreSyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.scheduleManager = new ScheduleManager(context);
    }

    /**
     * ✅ Start listening for real-time updates from the user's schedules
     */
    public void startListening() {
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "⚠️ No user logged in — cannot start Firestore listener.");
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        Log.d(TAG, "📡 Starting Firestore listener for user: " + uid);

        registration = firestore.collection("users")
                .document(uid)
                .collection("schedules")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "❌ Firestore listener failed: ", e);
                            return;
                        }

                        if (snapshots == null) {
                            Log.d(TAG, "⚠️ No schedule snapshot data.");
                            return;
                        }

                        Log.d(TAG, "📥 Firestore updated: " + snapshots.size() + " documents");

                        List<Schedule> validSchedules = new ArrayList<>();
                        int skipped = 0;

                        for (QueryDocumentSnapshot doc : snapshots) {
                            try {
                                Schedule s = doc.toObject(Schedule.class);

                                if (s == null ||
                                        s.getSubject() == null || s.getSubject().trim().isEmpty() ||
                                        s.getProgram() == null || s.getProgram().trim().isEmpty()) {
                                    skipped++;
                                    continue;
                                }

                                // ✅ Auto-generate missing "courseCode" if not present
                                String existingCode = doc.getString("courseCode");
                                if (existingCode == null || existingCode.trim().isEmpty()) {
                                    String courseCode = s.getProgram().trim() + "-" + s.getSubject().trim();
                                    firestore.collection("users")
                                            .document(uid)
                                            .collection("schedules")
                                            .document(doc.getId())
                                            .update("courseCode", courseCode)
                                            .addOnSuccessListener(aVoid ->
                                                    Log.d(TAG, "✅ Added missing courseCode: " + courseCode))
                                            .addOnFailureListener(err ->
                                                    Log.w(TAG, "⚠️ Failed to update courseCode: " + err.getMessage()));
                                }

                                validSchedules.add(s);

                            } catch (Exception ex) {
                                Log.w(TAG, "⚠️ Failed to parse schedule document: " + ex.getMessage());
                            }
                        }

                        Log.i(TAG, "✅ Loaded " + validSchedules.size() + " valid schedules (" + skipped + " skipped)");

                        // Optionally update local DB here
                        for (Schedule s : validSchedules) {
                            scheduleManager.addSchedule(s);
                        }
                    }
                });
    }

    /**
     * ✅ Stop listening to Firestore changes to avoid memory leaks.
     */
    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
            Log.d(TAG, "🛑 Firestore listener stopped.");
        }
    }
}
