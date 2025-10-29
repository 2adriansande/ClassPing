package com.example.classping;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button btnManageAnnouncements, btnManageSchedule, btnViewFeedback, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize buttons
        btnManageAnnouncements = findViewById(R.id.btnManageAnnouncements);
        btnManageSchedule = findViewById(R.id.btnManageSchedule);
        btnViewFeedback = findViewById(R.id.btnViewFeedback);
        btnLogout = findViewById(R.id.btnLogout);

        // Open Manage Announcements
        btnManageAnnouncements.setOnClickListener(v ->
                startActivity(new Intent(this, com.example.classping.announcements.AnnouncementActivity.class))
        );

        // Open Manage Schedule
        btnManageSchedule.setOnClickListener(v ->
                startActivity(new Intent(this, ScheduleActivity.class))
        );

        // Open View Feedback
        btnViewFeedback.setOnClickListener(v ->
                startActivity(new Intent(this, FeedbackActivity.class))
        );

        // Logout Confirmation Dialog
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Firebase Sign Out
                        FirebaseAuth.getInstance().signOut();

                        // Mark user as logged out (to stop auto-login)
                        getSharedPreferences("UserSession", MODE_PRIVATE)
                                .edit()
                                .putBoolean("isLoggedOut", true)
                                .apply();

                        // Go to LoginActivity and clear activity stack
                        Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        // Optional toast
                        // Toast.makeText(AdminDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}
