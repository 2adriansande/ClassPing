package com.example.classping;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LandingPage extends AppCompatActivity {

    Button btnScan, btnManual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page);

        btnScan = findViewById(R.id.btnScan);
        btnManual = findViewById(R.id.manual);

        btnScan.setOnClickListener(v -> {
            Intent intent = new Intent(LandingPage.this, MainActivity.class);
            startActivity(intent);
        });

        btnManual.setOnClickListener(v -> {
            Intent intent = new Intent(LandingPage.this, MainActivity.class);
            intent.putExtra("openMode", "manual"); // tell MainActivity it's manual mode
            startActivity(intent);

        });
    }
}
