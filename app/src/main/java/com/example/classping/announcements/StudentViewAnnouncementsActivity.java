package com.example.classping.announcements;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.classping.DatabaseHelper;
import com.example.classping.MainActivity;
import com.example.classping.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentViewAnnouncementsActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "announcements_channel";
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1001;

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> announcementList;
    private DatabaseHelper dbHelper;
    private FirebaseFirestore firestore;
    private CollectionReference announcementRef;
    private boolean initialLoadComplete = false;
    private ImageButton btnBackToMain;  // ✅ Use ImageButton here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_announcements);

        listView = findViewById(R.id.listViewAnnouncements);
        btnBackToMain = findViewById(R.id.btnBackToMain);  // ImageButton
        dbHelper = new DatabaseHelper(this);
        firestore = FirebaseFirestore.getInstance();
        announcementRef = firestore.collection("announcements");

        announcementList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, announcementList);
        listView.setAdapter(adapter);

        createNotificationChannel();
        requestNotificationPermission(); // Ask for permission on Android 13+
        loadAnnouncementsRealtime();

        // Handle Back Button
        btnBackToMain.setOnClickListener(v -> {
            Intent intent = new Intent(StudentViewAnnouncementsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadAnnouncementsRealtime() {
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

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        QueryDocumentSnapshot doc = dc.getDocument();
                        Announcement a = doc.toObject(Announcement.class);

                        switch (dc.getType()) {
                            case ADDED:
                                if (initialLoadComplete) {
                                    showLocalNotification(a.getTitle(), a.getMessage());
                                }
                                dbHelper.addAnnouncement(a);
                                announcementList.add(a.getTitle() + " (" + a.getDepartment() + ")\n" + a.getMessage());
                                adapter.notifyDataSetChanged();
                                break;
                            case MODIFIED:
                                // Optional: update modified announcements
                                break;
                            case REMOVED:
                                // Optional: remove deleted announcements
                                break;
                        }
                    }

                    if (!initialLoadComplete) {
                        initialLoadComplete = true;
                    }
                });
    }

    private void loadFromLocal() {
        List<Announcement> localList = dbHelper.getAllAnnouncements();
        announcementList.clear();

        for (Announcement a : localList) {
            announcementList.add(a.getTitle() + " (" + a.getDepartment() + ")\n" + a.getMessage());
        }

        adapter.notifyDataSetChanged();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Announcements";
            String description = "Channel for announcement notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showLocalNotification(@NonNull String title, @NonNull String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_announcement)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications enabled!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notifications disabled — please enable in settings.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
