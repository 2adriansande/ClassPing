package com.example.classping;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.classping.R;

public class ScheduleActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        TextView text = findViewById(R.id.tvSchedulePlaceholder);
        text.setText("Schedule Management will be added soon.");
    }
}
