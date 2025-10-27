package com.example.classping;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordEmailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_email);

        EditText etEmail = findViewById(R.id.etEmail);
        Button btnSendOTP = findViewById(R.id.btnSendOTP);

        btnSendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString().trim();
                if(email.isEmpty()){
                    Toast.makeText(ForgotPasswordEmailActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                } else {
                    // TODO: Send OTP via backend
                    startActivity(new Intent(ForgotPasswordEmailActivity.this, OTPVerificationActivity.class));
                }
            }
        });
    }
}

