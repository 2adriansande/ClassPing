package com.example.classping;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.classping.LoginActivity;

public class LandingPage extends AppCompatActivity {

    private Button studentBtn, adminBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page);

        studentBtn = findViewById(R.id.btnStudentLogin);
        adminBtn = findViewById(R.id.btnAdminLogin);

        studentBtn.setOnClickListener(v -> openLogin("student"));
        adminBtn.setOnClickListener(v -> openLogin("admin"));
    }

    private void openLogin(String role) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
    }
}
