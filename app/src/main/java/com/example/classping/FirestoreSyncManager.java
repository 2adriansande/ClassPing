package com.example.classping;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class FirestoreSyncManager {

    private final FirebaseFirestore firestore;
    private ListenerRegistration registration;
    private final Context context;

    // ✅ Constructor that accepts Context
    public FirestoreSyncManager(Context context) {
        this.context = context;
        firestore = FirebaseFirestore.getInstance();
    }

    // ✅ Start listening for real-time Firestore updates
    public void startListening() {
        registration = firestore.collection("schedules")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("FirestoreSync", "Listen failed: ", e);
                            return;
                        }

                        if (snapshots != null) {
                            Log.d("FirestoreSync", "Schedules updated: " + snapshots.size() + " documents");
                        } else {
                            Log.d("FirestoreSync", "No schedule data available.");
                        }
                    }
                });
    }

    // ✅ Stop listening to avoid memory leaks
    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }
}
