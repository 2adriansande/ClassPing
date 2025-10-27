package com.example.classping;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private TextView headerTitle;
    private Spinner spinnerDays;
    private RecyclerView recyclerView;
    private ScheduleManager manager;
    private ScheduleAdapter adapter;
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        headerTitle = findViewById(R.id.header_title);
        spinnerDays = findViewById(R.id.spinnerDays);
        recyclerView = findViewById(R.id.scheduleRecycler);
        ImageButton btnAdd = findViewById(R.id.btnAdd);
        ImageButton btnUpload = findViewById(R.id.btnUploadImage);

        manager = new ScheduleManager(this);

        adapter = new ScheduleAdapter(manager.getAllSchedules(), manager, this::refreshScheduleList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        headerTitle.setText("ClassPing");

        btnAdd.setOnClickListener(v ->
                new AddScheduleDialog(MainActivity.this, manager, this::refreshScheduleList).show()
        );

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), (Uri uri) -> {
            if (uri != null) {
                String defaultDay = spinnerDays.getSelectedItem() != null
                        ? spinnerDays.getSelectedItem().toString()
                        : "Monday";

                // 🔹 Use the improved OCR flow with automatic parsing + saving + loading dialog
                OCRProcessor processor = new OCRProcessor(manager, this::refreshScheduleList, this);
                processor.processAndSaveImage(uri, defaultDay);
            }
        });

        btnUpload.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        spinnerDays.setAdapter(new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, days));

        spinnerDays.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedDay = days[position];
                adapter.updateSchedules(manager.getSchedulesForDay(selectedDay));
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void refreshScheduleList() {
        String selectedDay = spinnerDays.getSelectedItem() != null
                ? spinnerDays.getSelectedItem().toString()
                : "Monday";
        adapter.updateSchedules(manager.getSchedulesForDay(selectedDay));
    }
}
