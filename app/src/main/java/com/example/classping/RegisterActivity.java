package com.example.classping;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etUsername, etNumber;
    private Spinner spinnerRole;
    private Button btnRegister;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etUsername = findViewById(R.id.etUsername);
        etNumber = findViewById(R.id.etStudentNumber); // reused for professor
        spinnerRole = findViewById(R.id.spinnerRole);
        btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Role dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"student", "professor", "admin"});
        spinnerRole.setAdapter(adapter);

        // Change hint based on role
        spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRole = spinnerRole.getSelectedItem().toString();
                if (selectedRole.equals("student")) {
                    etNumber.setVisibility(View.VISIBLE);
                    etNumber.setHint("Student Number");
                } else if (selectedRole.equals("professor")) {
                    etNumber.setVisibility(View.VISIBLE);
                    etNumber.setHint("Professor Number");
                } else { // admin
                    etNumber.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        btnRegister.setOnClickListener(v -> registerUser());

        tvGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();
        String number = etNumber.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || username.isEmpty() ||
                ((role.equals("student") || role.equals("professor")) && number.isEmpty())) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("email", email);
                    userMap.put("role", role);
                    userMap.put("username", username);

                    if (role.equals("student")) {
                        userMap.put("studentNumber", number);
                    } else if (role.equals("professor")) {
                        userMap.put("professorNumber", number);
                    }

                    firestore.collection("users").document(uid)
                            .set(userMap)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Registered successfully! Please log in.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error saving user: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
