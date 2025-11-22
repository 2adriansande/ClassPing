package com.example.classping;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail, tvStudentNumber, tvRole;
    private Button btnChangePassword;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvStudentNumber = findViewById(R.id.tvStudentNumber);
        tvRole = findViewById(R.id.tvRole);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            uid = user.getUid();
            loadStudentInfo(uid);
        }

        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void loadStudentInfo(String uid) {
        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        tvUsername.setText("Name: " + doc.getString("username"));
                        tvEmail.setText("Email: " + doc.getString("email"));
                        tvStudentNumber.setText("Student Number: " + doc.getString("studentNumber"));
                        tvRole.setText("Role: " + doc.getString("role"));
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("New Password");
        builder.setView(input);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String newPassword = input.getText().toString().trim();
            if (!newPassword.isEmpty()) {
                auth.getCurrentUser().updatePassword(newPassword)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } else {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}
