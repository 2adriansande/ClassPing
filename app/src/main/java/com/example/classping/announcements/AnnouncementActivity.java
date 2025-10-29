package com.example.classping.announcements;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.classping.DatabaseHelper;
import com.example.classping.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnnouncementActivity extends AppCompatActivity {

    private EditText etTitle, etMessage, etDepartment;
    private Button btnPost;
    private ListView listView;
    private DatabaseHelper dbHelper;
    private ArrayAdapter<String> adapter;
    private List<String> list;

    private FirebaseFirestore firestore;
    private CollectionReference announcementRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement);

        etTitle = findViewById(R.id.etTitle);
        etMessage = findViewById(R.id.etMessage);
        etDepartment = findViewById(R.id.etDepartment);
        btnPost = findViewById(R.id.btnPost);
        listView = findViewById(R.id.listView);

        dbHelper = new DatabaseHelper(this);

        firestore = FirebaseFirestore.getInstance();
        announcementRef = firestore.collection("announcements");

        loadAnnouncements();

        btnPost.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String msg = etMessage.getText().toString().trim();
            String dept = etDepartment.getText().toString().trim();

            if (title.isEmpty() || msg.isEmpty()) {
                Toast.makeText(this, "Title and message are required", Toast.LENGTH_SHORT).show();
                return;
            }

            String id = UUID.randomUUID().toString();
            long timestamp = System.currentTimeMillis();

            Announcement announcement = new Announcement(
                    id,
                    title,
                    msg,
                    dept,
                    "Admin",
                    timestamp
            );

            // ✅ Save locally (SQLite)
            dbHelper.addAnnouncement(announcement);

            // ✅ Save to Firestore
            announcementRef.document(id)
                    .set(announcement)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Announcement posted!", Toast.LENGTH_SHORT).show();
                        etTitle.setText("");
                        etMessage.setText("");
                        etDepartment.setText("");
                        loadAnnouncements();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });
    }

    private void loadAnnouncements() {
        list = new ArrayList<>();

        // ✅ Load from Firestore first
        announcementRef.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    dbHelper.getWritableDatabase().delete("announcements", null, null); // Clear old data

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Announcement a = doc.toObject(Announcement.class);
                        dbHelper.addAnnouncement(a);
                        list.add(a.getTitle() + " (" + a.getDepartment() + ")\n" + a.getMessage());
                    }

                    updateListView();
                })
                .addOnFailureListener(e -> {
                    // If Firestore fails, fallback to local
                    List<Announcement> offlineList = dbHelper.getAllAnnouncements();
                    for (Announcement a : offlineList) {
                        list.add(a.getTitle() + " (" + a.getDepartment() + ")\n" + a.getMessage());
                    }
                    updateListView();
                });
    }

    private void updateListView() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
    }
}
