package com.example.classping.announcements;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.*;
import com.example.classping.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddAnnouncementDialog extends Dialog {
    public AddAnnouncementDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_announcement);
        EditText titleField = findViewById(R.id.titleField);
        EditText messageField = findViewById(R.id.messageField);
        EditText departmentField = findViewById(R.id.departmentField);
        EditText authorField = findViewById(R.id.authorField);
        Button postButton = findViewById(R.id.postButton);

        postButton.setOnClickListener(v -> {
            String title = titleField.getText().toString().trim();
            String message = messageField.getText().toString().trim();
            String department = departmentField.getText().toString().trim();
            String author = authorField.getText().toString().trim();

            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String id = db.collection("announcements").document().getId();

            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("title", title);
            data.put("message", message);
            data.put("department", department);
            data.put("author", author);
            data.put("timestamp", System.currentTimeMillis());

            db.collection("announcements").document(id).set(data)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(context, "Announcement posted!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to post announcement", Toast.LENGTH_SHORT).show());
        });
    }
}
