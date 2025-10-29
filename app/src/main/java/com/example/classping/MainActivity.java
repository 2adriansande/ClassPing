package com.example.classping;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import com.example.classping.announcements.ViewAnnouncementsActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Main activity for ClassPing.
 * Displays schedules, allows OCR upload, and handles sidebar navigation.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private TextView headerTitle;
    private Spinner spinnerDays;
    private RecyclerView recyclerView;
    private ScheduleManager manager;
    private ScheduleAdapter adapter;
    private ActivityResultLauncher<String> pickImageLauncher;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnMenu;

    private static final int NOTIF_REQ_CODE = 1001;

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

        // 🧠 Schedule Manager + Adapter
        manager = new ScheduleManager(this);
        adapter = new ScheduleAdapter(manager.getAllSchedules(), manager, this::refreshScheduleList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        headerTitle.setText("ClassPing");

        // ➕ Add Schedule manually
        btnAdd.setOnClickListener(v ->
                new AddScheduleDialog(MainActivity.this, manager, this::refreshScheduleList).show()
        );

        // 📸 OCR Image picker
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

        // 📅 Spinner (Days of Week)
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
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    /**
     * Refresh RecyclerView when schedule data changes.
     */
    private void refreshScheduleList() {
        String selectedDay = spinnerDays.getSelectedItem() != null
                ? spinnerDays.getSelectedItem().toString()
                : "Monday";
        adapter.updateSchedules(manager.getSchedulesForDay(selectedDay));
    }

    /**
     * Handle sidebar navigation selections.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(this, StudentProfileActivity.class));
        }
        else if (id == R.id.nav_announcements) {
            startActivity(new Intent(this, ViewAnnouncementsActivity.class));
        }
        else if (id == R.id.nav_logout) {
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

    /**
     * Refresh schedules when returning to activity.
     */
    @Override
    protected void onResume() {
        super.onResume();
        refreshScheduleList();
    }

    /**
     * Handle permission result.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIF_REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            }
        }
    }
}
