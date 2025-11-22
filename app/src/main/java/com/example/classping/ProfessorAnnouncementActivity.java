package com.example.classping;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfessorAnnouncementActivity extends AppCompatActivity {

    private EditText etTitle, etMessage, etDepartment;
    private Button btnPost;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor_announcement);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        etTitle = findViewById(R.id.etTitle);
        etMessage = findViewById(R.id.etMessage);
        etDepartment = findViewById(R.id.etDepartment);
        btnPost = findViewById(R.id.btnPost);

        btnPost.setOnClickListener(v -> postAnnouncement());
    }

    private void postAnnouncement() {
        String title = etTitle.getText().toString().trim();
        String message = etMessage.getText().toString().trim();
        String department = etDepartment.getText().toString().trim();

        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Title and message required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("message", message);
        data.put("department", department);
        data.put("timestamp", System.currentTimeMillis());
        data.put("postedBy", auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "Professor");

        firestore.collection("announcements")
                .add(data)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Announcement posted!", Toast.LENGTH_SHORT).show();
                    etTitle.setText("");
                    etMessage.setText("");
                    etDepartment.setText("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
