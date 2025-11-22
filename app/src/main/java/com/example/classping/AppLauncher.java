package com.example.classping;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AppLauncher extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(this, LandingPage.class));
            finish();
        } else {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String role = snapshot.getString("role");
                            if ("admin".equals(role)) {
                                startActivity(new Intent(this, AdminDashboardActivity.class));
                            } else if ("professor".equals(role)) {
                                startActivity(new Intent(this, ProfessorDashboardActivity.class));
                            } else {
                                startActivity(new Intent(this, MainActivity.class)); // Student
                            }
                        } else {
                            startActivity(new Intent(this, LandingPage.class));
                        }
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        startActivity(new Intent(this, LandingPage.class));
                        finish();
                    });
        }
    }
}
