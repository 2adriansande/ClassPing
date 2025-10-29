package com.example.classping.announcements;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.classping.R;
import com.example.classping.ToastHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminAnnouncementActivity extends AppCompatActivity {
    private EditText titleField, messageField, departmentField;
    private Button postButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_announcement);

        titleField = findViewById(R.id.adminTitle);
        messageField = findViewById(R.id.adminMessage);
        departmentField = findViewById(R.id.adminDepartment);
        postButton = findViewById(R.id.adminPostButton);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        postButton.setOnClickListener(v -> postAnnouncement());
    }

    private void postAnnouncement() {
        String title = titleField.getText().toString().trim();
        String message = messageField.getText().toString().trim();
        String department = departmentField.getText().toString().trim();

        if (title.isEmpty() || message.isEmpty()) {
            ToastHelper.show(this, "Please fill in all fields");
            return;
        }

        String docId = db.collection("announcements").document().getId();
        String author = auth.getCurrentUser() != null
                ? auth.getCurrentUser().getEmail()
                : "Admin";

        Map<String, Object> data = new HashMap<>();
        data.put("id", docId);
        data.put("title", title);
        data.put("message", message);
        data.put("department", department);
        data.put("author", author);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("announcements").document(docId)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    ToastHelper.show(this, "Announcement posted!");
                    titleField.setText("");
                    messageField.setText("");
                    departmentField.setText("");
                })
                .addOnFailureListener(e -> ToastHelper.show(this, "Failed to post: " + e.getMessage()));
    }
}
