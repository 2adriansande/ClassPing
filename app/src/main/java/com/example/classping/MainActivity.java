package com.example.classping;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView scheduleList;
    Button btnAdd;
    ArrayAdapter<String> adapter;
    ArrayList<String> schedules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scheduleList = findViewById(R.id.scheduleList);
        btnAdd = findViewById(R.id.btnAdd);

        schedules = new ArrayList<>();
        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, schedules);
        scheduleList.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_schedule, null);
        builder.setView(dialogView);

        EditText etSubject = dialogView.findViewById(R.id.etSubject);
        EditText etNotes = dialogView.findViewById(R.id.etNotes);
        TimePicker timeStart = dialogView.findViewById(R.id.timeStart);
        TimePicker timeEnd = dialogView.findViewById(R.id.timeEnd);
        Spinner spinnerDay = dialogView.findViewById(R.id.spinnerDay);

        // Set 12-hour view (AM/PM toggle) instead of 24-hour
        timeStart.setIs24HourView(false);
        timeEnd.setIs24HourView(false);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String subject = etSubject.getText().toString();
            String notes = etNotes.getText().toString();

            int startHour, startMinute, endHour, endMinute;
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                startHour = timeStart.getHour();
                startMinute = timeStart.getMinute();
                endHour = timeEnd.getHour();
                endMinute = timeEnd.getMinute();
            } else {
                startHour = timeStart.getCurrentHour();
                startMinute = timeStart.getCurrentMinute();
                endHour = timeEnd.getCurrentHour();
                endMinute = timeEnd.getCurrentMinute();
            }

            String day = spinnerDay.getSelectedItem().toString();

            String scheduleEntry = day + " | " + subject + " (" +
                    startHour + ":" + startMinute + " - " + endHour + ":" + endMinute + ")\nNotes: " + notes;

            schedules.add(scheduleEntry);
            adapter.notifyDataSetChanged();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
}
