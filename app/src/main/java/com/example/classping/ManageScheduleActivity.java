package com.example.classping;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Admin screen that displays every student's subjects (courseCode)
 * combined from all users' schedules in Firestore.
 */
public class ManageScheduleActivity extends AppCompatActivity {

    private ListView listViewSchedules;
    private ArrayAdapter<String> adapter;
    private final List<String> scheduleCodes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_schedule);

        listViewSchedules = findViewById(R.id.listViewSchedules);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, scheduleCodes);
        listViewSchedules.setAdapter(adapter);

        fetchAllStudentSchedules();
    }

    /**
     * Fetches all schedule subcollections from every student in Firestore
     * using a collectionGroup query.
     */
    private void fetchAllStudentSchedules() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collectionGroup("schedules")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    scheduleCodes.clear();
                    Set<String> uniqueCodes = new HashSet<>(); // avoid duplicates

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String courseCode = doc.getString("courseCode");
                        String program = doc.getString("program");
                        String subject = doc.getString("subject");

                        // ✅ Prefer using the stored "courseCode" field if available
                        if (courseCode != null && !courseCode.trim().isEmpty()) {
                            uniqueCodes.add(courseCode.trim());
                        } else if (program != null && subject != null &&
                                !program.trim().isEmpty() && !subject.trim().isEmpty()) {
                            uniqueCodes.add(program.trim() + "-" + subject.trim());
                        }
                    }

                    scheduleCodes.addAll(uniqueCodes);

                    if (scheduleCodes.isEmpty()) {
                        scheduleCodes.add("No schedules found.");
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "❌ Failed to load schedules: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
