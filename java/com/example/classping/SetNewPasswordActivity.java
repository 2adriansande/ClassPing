package com.example.classping;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SetNewPasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_new_password);

        EditText etNewPassword = findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnUpdate = findViewById(R.id.btnUpdatePassword);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newPass = etNewPassword.getText().toString().trim();
                String confirmPass = etConfirmPassword.getText().toString().trim();

                if(newPass.isEmpty() || confirmPass.isEmpty()){
                    Toast.makeText(SetNewPasswordActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else if(!newPass.equals(confirmPass)){
                    Toast.makeText(SetNewPasswordActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    // TODO: Update password in backend
                    Toast.makeText(SetNewPasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SetNewPasswordActivity.this, LoginActivity.class));
                    finish();
                }
            }
        });
    }
}

