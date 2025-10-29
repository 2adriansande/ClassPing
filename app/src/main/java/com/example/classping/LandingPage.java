package com.example.classping;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class LandingPage extends AppCompatActivity {

    private Button studentBtn, professorBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page); // Make sure this XML name matches!

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
