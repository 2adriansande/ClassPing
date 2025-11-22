package com.example.classping;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Handles user login for students, professors, and admins.
 * Automatically redirects to their dashboards if already logged in.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private String selectedRole = "student"; // default role

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // 🧭 Get role passed from LandingPage
        if (getIntent() != null && getIntent().hasExtra("role")) {
            selectedRole = getIntent().getStringExtra("role");
        }

        // 🧠 Auto-skip login if already signed in
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        boolean isLoggedOut = prefs.getBoolean("isLoggedOut", false);
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null && !isLoggedOut) {
            autoLogin(currentUser.getUid());
            return;
        }

        prefs.edit().putBoolean("isLoggedOut", false).apply();

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> loginUser());
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    // 🔹 Handle manual login
    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    getSharedPreferences("UserSession", MODE_PRIVATE)
                            .edit()
                            .putBoolean("isLoggedOut", false)
                            .apply();

                    autoLogin(result.getUser().getUid());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // 🔹 Redirect user to the correct dashboard
    private void autoLogin(String uid) {
        firestore.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String role = doc.getString("role");
                        if (role == null) role = selectedRole; // fallback if not yet stored

                        Intent intent;
                        switch (role.toLowerCase()) {
                            case "admin":
                                intent = new Intent(this, AdminDashboardActivity.class);
                                break;
                            case "professor":
                                intent = new Intent(this, ProfessorDashboardActivity.class);
                                break;
                            default:
                                intent = new Intent(this, MainActivity.class); // student
                                break;
                        }

                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "User data not found in Firestore.", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching user info: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    auth.signOut();
                });
    }
}
