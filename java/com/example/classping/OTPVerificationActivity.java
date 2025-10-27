package com.example.classping;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class OTPVerificationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        EditText etOTP = findViewById(R.id.etOTP);
        Button btnVerify = findViewById(R.id.btnVerify);

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = etOTP.getText().toString().trim();
                if(otp.isEmpty()){
                    Toast.makeText(OTPVerificationActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                } else {
                    // TODO: Verify OTP with backend
                    startActivity(new Intent(OTPVerificationActivity.this, ResetPasswordInfoActivity.class));
                }
            }
        });
    }
}
