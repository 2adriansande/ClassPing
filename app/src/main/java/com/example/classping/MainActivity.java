package com.example.classping;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.classping.announcements.StudentViewAnnouncementsActivity;
import com.example.classping.notifications.NotificationHelper;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextView headerTitle, tvCurrentDate, tvCurrentDay, tvCurrentTime;
    private Spinner spinnerDays;
    private RecyclerView recyclerView;
    private ScheduleManager manager;
    private ScheduleAdapter adapter;
    private ActivityResultLauncher<String> pickImageLauncher;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnMenu;

    private static final int NOTIF_REQ_CODE = 1001;

    // 🕒 Realtime Clock Handler
    private final Handler timeHandler = new Handler();
    private Runnable timeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔔 Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIF_REQ_CODE);
            }
        }

        // 🔥 Initialize Firebase
        FirebaseApp.initializeApp(this);

        // 📨 Subscribe to "announcements" topic
        FirebaseMessaging.getInstance().subscribeToTopic("announcements")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        NotificationHelper.showAnnouncementNotification(
                                this,
                                "Subscribed",
                                "You’ll now receive announcement updates."
                        );
                    }
                });

        // 🎨 Initialize Drawer and UI elements
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));

        headerTitle = findViewById(R.id.header_title);
        spinnerDays = findViewById(R.id.spinnerDays);
        recyclerView = findViewById(R.id.scheduleRecycler);
        ImageButton btnAdd = findViewById(R.id.btnAdd);
        ImageButton btnUpload = findViewById(R.id.btnUploadImage);

        // 📅 Date & Time Views
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvCurrentDay = findViewById(R.id.tvCurrentDay);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);

        // 🕒 Initialize date/day and realtime clock
        updateDateAndDay();
        startRealtimeClock();

        manager = new ScheduleManager(this);
        adapter = new ScheduleAdapter(manager.getAllSchedules(), manager, this::refreshScheduleList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        headerTitle.setText("ClassPing");

        btnAdd.setOnClickListener(v ->
                new AddScheduleDialog(MainActivity.this, manager, this::refreshScheduleList).show()
        );

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), (Uri uri) -> {
            if (uri != null) {
                String defaultDay = spinnerDays.getSelectedItem() != null
                        ? spinnerDays.getSelectedItem().toString()
                        : "Monday";

                OCRProcessor processor = new OCRProcessor(manager, this::refreshScheduleList, this);
                processor.processAndSaveImage(uri, defaultDay);
            }
        });

        btnUpload.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // 📅 Spinner setup
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        spinnerDays.setAdapter(new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, days));

        spinnerDays.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedDay = days[position];
                adapter.updateSchedules(manager.getSchedulesForDay(selectedDay));
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    // 🔁 Refresh RecyclerView for selected day
    private void refreshScheduleList() {
        String selectedDay = spinnerDays.getSelectedItem() != null
                ? spinnerDays.getSelectedItem().toString()
                : "Monday";
        adapter.updateSchedules(manager.getSchedulesForDay(selectedDay));
    }

    // 📅 Update Date and Day
    private void updateDateAndDay() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

        tvCurrentDate.setText(dateFormat.format(now));
        tvCurrentDay.setText(dayFormat.format(now));
    }

    // 🕒 Start real-time clock updating every second
    private void startRealtimeClock() {
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
                String currentTime = timeFormat.format(new Date());
                tvCurrentTime.setText(currentTime);
                timeHandler.postDelayed(this, 1000);
            }
        };
        timeHandler.post(timeRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeHandler != null && timeRunnable != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(this, StudentProfileActivity.class));
        } else if (id == R.id.nav_announcements) {
            startActivity(new Intent(this, StudentViewAnnouncementsActivity.class));
        } else if (id == R.id.nav_logout) {
            drawerLayout.closeDrawers();
            drawerLayout.postDelayed(() -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }, 250);
        }

        drawerLayout.closeDrawers();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshScheduleList();
    }
}
