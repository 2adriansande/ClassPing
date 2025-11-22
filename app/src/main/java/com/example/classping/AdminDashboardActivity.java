package com.example.classping;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button btnManageAnnouncements;
    private Button btnManageSchedule;
    private Button btnViewFeedback;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard); // your updated XML layout

        // Initialize buttons (use same types as in XML)
        btnManageAnnouncements = findViewById(R.id.btnManageAnnouncements);
        btnManageSchedule = findViewById(R.id.btnManageSchedule);


        btnLogout = findViewById(R.id.btnLogout);

        // Manage Announcements
        btnManageAnnouncements.setOnClickListener(v ->
                startActivity(new Intent(this, com.example.classping.announcements.AnnouncementActivity.class))
        );

        // Manage Schedule
        btnManageSchedule = findViewById(R.id.btnManageSchedule);

        btnManageSchedule.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageScheduleActivity.class);
            startActivity(intent);
        });
        // View Feedback


        // Logout confirmation
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();

                    // Stop auto-login
                    getSharedPreferences("UserSession", MODE_PRIVATE)
                            .edit()
                            .putBoolean("isLoggedOut", true)
                            .apply();

                    // Redirect to LoginActivity
                    Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
