package com.example.classping;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.classping.data.AppDatabase;
import com.example.classping.data.User;
import com.example.classping.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getInstance(this);

        String prefsId = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getString("userId", null);
        String studentId = getIntent().getStringExtra("studentId");
        if (studentId == null) studentId = prefsId;

        if (studentId == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        User user = db.userDao().getUserById(studentId);
        binding.tvWelcome.setText("Welcome, " + (user != null ? user.getName() : studentId));

        binding.btnLogout.setOnClickListener(v -> {
            getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().remove("userId").apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
