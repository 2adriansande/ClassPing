package com.example.classping;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Landing page shown only to logged-out users.
 * If a user is already signed in, it redirects automatically
 * to their respective dashboard based on their Firestore "role".
 */
public class LandingPage extends AppCompatActivity {

    private Button studentBtn, professorBtn;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();

        // ✅ If user already logged in, skip the landing page
        if (currentUser != null) {
            String uid = currentUser.getUid();
            firestore.collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String role = doc.getString("role");
                            if (role == null) role = "student"; // default fallback

                            Intent intent;
                            if ("admin".equalsIgnoreCase(role)) {
                                intent = new Intent(this, AdminDashboardActivity.class);
                            } else if ("professor".equalsIgnoreCase(role)) {
                                intent = new Intent(this, ProfessorDashboardActivity.class);
                            } else {
                                intent = new Intent(this, MainActivity.class); // student dashboard
                            }

                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            showLanding();
                        }
                    })
                    .addOnFailureListener(e -> showLanding());
        } else {
            showLanding();
        }
    }

    private void showLanding() {
        setContentView(R.layout.landing_page);

        studentBtn = findViewById(R.id.btnStudentLogin);
        professorBtn = findViewById(R.id.btnProfessorLogin);

        studentBtn.setOnClickListener(v -> openLogin("student"));
        professorBtn.setOnClickListener(v -> openLogin("admin"));
    }

    private void openLogin(String role) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
    }
}
