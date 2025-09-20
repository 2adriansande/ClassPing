package com.example.classping;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.classping.data.AppDatabase;
import com.example.classping.data.User;
import com.example.classping.databinding.ActivityRegisterBinding;
import com.example.classping.util.Utils;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getInstance(this);

        // ✅ Show / Hide password
        binding.cbShowPasswordRegister.setOnCheckedChangeListener((buttonView, checked) -> {
            if (checked) {
                binding.etPasswordRegister.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.etConfirmPasswordRegister.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                binding.etPasswordRegister.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.etConfirmPasswordRegister.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            binding.etPasswordRegister.setSelection(binding.etPasswordRegister.getText().length());
            binding.etConfirmPasswordRegister.setSelection(binding.etConfirmPasswordRegister.getText().length());
        });

        // ✅ Go to Login
        binding.tvLoginLink.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        // ✅ Register button click
        binding.btnRegister.setOnClickListener(v -> {
            String studentId = binding.etStudentIdRegister.getText().toString().trim();
            String name = binding.etNameRegister.getText().toString().trim();
            String email = binding.etEmailRegister.getText().toString().trim();
            String password = binding.etPasswordRegister.getText().toString().trim();
            String confirmPassword = binding.etConfirmPasswordRegister.getText().toString().trim();

            if (studentId.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Hash password before saving
            String hash = Utils.hashPassword(password);

            User newUser = new User(studentId, name, email, hash);

            // ✅ Insert into DB
            new Thread(() -> {
                User existing = db.userDao().getUserById(studentId);
                if (existing != null) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show());
                } else {
                    db.userDao().insertUser(newUser);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    });
                }
            }).start();
        });
    }
}
