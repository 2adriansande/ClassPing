package com.example.classping;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.classping.data.AppDatabase;
import com.example.classping.data.User;
import com.example.classping.databinding.ActivityLoginBinding;
import com.example.classping.util.Utils;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getInstance(this);

        // Auto-login if session
        String savedId = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getString("userId", null);
        if (savedId != null) {
            navigateToMain(savedId);
            return;
        }

        binding.cbShowPasswordLogin.setOnCheckedChangeListener((buttonView, checked) -> {
            if (checked) {
                binding.etPasswordLogin.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                binding.etPasswordLogin.setInputType(
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                );
            }
            binding.etPasswordLogin.setSelection(binding.etPasswordLogin.getText().length());
        });

        binding.tvRegisterLink.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        binding.btnLogin.setOnClickListener(v -> {
            String studentId = binding.etStudentIDLogin.getText().toString().trim();

            String password = binding.etPasswordLogin.getText().toString();

            if (studentId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = db.userDao().getUserById(studentId);
            if (user == null) {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                return;
            }

            String hash = Utils.hashPassword(password);
            if (hash.equals(user.getPasswordHash())) {
                getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        .edit().putString("userId", studentId).apply();
                navigateToMain(studentId);
            } else {
                Toast.makeText(this, "Incorrect credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToMain(String studentId) {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("studentId", studentId);
        startActivity(i);
        finish();
    }
}
