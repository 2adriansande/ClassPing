package com.example.classping.announcements;
import com.google.firebase.firestore.FirebaseFirestoreException;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.classping.DatabaseHelper;
import com.example.classping.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewAnnouncementsActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> announcementList;
    private DatabaseHelper dbHelper;
    private FirebaseFirestore firestore;
    private CollectionReference announcementRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_announcements);

        listView = findViewById(R.id.listViewAnnouncements);
        dbHelper = new DatabaseHelper(this);
        firestore = FirebaseFirestore.getInstance();
        announcementRef = firestore.collection("announcements");

        announcementList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, announcementList);
        listView.setAdapter(adapter);

        loadAnnouncementsRealtime();
    }

    private void loadAnnouncementsRealtime() {
        // Listen for live updates in Firestore
        announcementRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((QuerySnapshot snapshots, FirebaseFirestoreException e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading announcements: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadFromLocal();
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        Toast.makeText(this, "No announcements yet.", Toast.LENGTH_SHORT).show();
                        announcementList.clear();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    announcementList.clear();
                    dbHelper.getWritableDatabase().delete("announcements", null, null); // Clear local

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Announcement a = doc.toObject(Announcement.class);
                        dbHelper.addAnnouncement(a);
                        announcementList.add(a.getTitle() + " (" + a.getDepartment() + ")\n" + a.getMessage());
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void loadFromLocal() {
        // Fallback: if Firestore fails or offline
        List<Announcement> localList = dbHelper.getAllAnnouncements();
        announcementList.clear();

        for (Announcement a : localList) {
            announcementList.add(a.getTitle() + " (" + a.getDepartment() + ")\n" + a.getMessage());
        }

        adapter.notifyDataSetChanged();
    }
}
