package com.example.classping;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.classping.announcements.AdminAnnouncementActivity;
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
                        if (snapshot.exists() && "admin".equals(snapshot.getString("role"))) {
                            startActivity(new Intent(this, AdminAnnouncementActivity.class));
                        } else {
                            startActivity(new Intent(this, MainActivity.class));
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
