package com.example.classping;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfessorDashboardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private TextView tvCurrentDate, tvCurrentDay, tvCurrentTime;
    private final Handler timeHandler = new Handler();
    private Runnable timeRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.professor_dashboard);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.profToolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.profDrawerLayout);
        navView = findViewById(R.id.profNavView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvCurrentDay = findViewById(R.id.tvCurrentDay);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);

        updateDateAndDay();
        startRealtimeClock();

        // 🟢 NEW: Button to go to Make Announcement screen
        findViewById(R.id.btnMakeAnnouncement).setOnClickListener(v ->
                startActivity(new Intent(this, ProfessorAnnouncementActivity.class))
        );

        // 🔹 Navigation
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_manage_classes) {
                startActivity(new Intent(this, ManageClassesActivity.class));
            } else if (id == R.id.nav_logout) {
                auth.signOut();
                Intent i = new Intent(this, LandingPage.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void updateDateAndDay() {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        tvCurrentDate.setText(dateFormat.format(now));
        tvCurrentDay.setText(dayFormat.format(now));
    }

    private void startRealtimeClock() {
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
                tvCurrentTime.setText(timeFormat.format(new Date()));
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
}
