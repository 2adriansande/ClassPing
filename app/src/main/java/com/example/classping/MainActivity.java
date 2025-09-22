package com.example.classping;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ListView scheduleList;
    Button btnAdd, btnUploadImage;
    ArrayAdapter<String> adapter;
    ArrayList<String> allSchedules;
    ArrayList<String> displayedSchedules;
    String selectedDay = "UNKNOWN_DAY";

    Map<String, Button> dayButtons = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scheduleList = findViewById(R.id.scheduleList);
        btnAdd = findViewById(R.id.btnAdd);
        btnUploadImage = findViewById(R.id.btnUploadImage);

        allSchedules = new ArrayList<>();
        displayedSchedules = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayedSchedules);
        scheduleList.setAdapter(adapter);

        // Map day names to button IDs
        dayButtons.put("SUNDAY", findViewById(R.id.btnSun));
        dayButtons.put("MONDAY", findViewById(R.id.btnMon));
        dayButtons.put("TUESDAY", findViewById(R.id.btnTue));
        dayButtons.put("WEDNESDAY", findViewById(R.id.btnWed));
        dayButtons.put("THURSDAY", findViewById(R.id.btnThu));
        dayButtons.put("FRIDAY", findViewById(R.id.btnFri));
        dayButtons.put("SATURDAY", findViewById(R.id.btnSat));

        // Day button click
        for (Map.Entry<String, Button> entry : dayButtons.entrySet()) {
            String day = entry.getKey();
            Button button = entry.getValue();
            button.setOnClickListener(v -> {
                selectedDay = day;
                updateScheduleList();
                highlightSelectedDay();
            });
        }

        // Default day
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case Calendar.SUNDAY: selectedDay = "SUNDAY"; break;
            case Calendar.MONDAY: selectedDay = "MONDAY"; break;
            case Calendar.TUESDAY: selectedDay = "TUESDAY"; break;
            case Calendar.WEDNESDAY: selectedDay = "WEDNESDAY"; break;
            case Calendar.THURSDAY: selectedDay = "THURSDAY"; break;
            case Calendar.FRIDAY: selectedDay = "FRIDAY"; break;
            case Calendar.SATURDAY: selectedDay = "SATURDAY"; break;
            default: selectedDay = "UNKNOWN_DAY"; break;
        }
        highlightSelectedDay();

        btnAdd.setOnClickListener(v -> showAddDialog());
        btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"), 100);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) processImage(imageUri);
        }
    }

    private void processImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            if (bitmap == null) {
                Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                return;
            }

            InputImage image = InputImage.fromBitmap(bitmap, 0);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String recognizedText = visionText.getText();
                        android.util.Log.d("OCR_OUTPUT", "Recognized Text: '" + recognizedText + "'");

                        if (recognizedText.trim().isEmpty()) {
                            Toast.makeText(this, "No text found in the image.", Toast.LENGTH_SHORT).show();
                        } else {
                            parseScheduleText(recognizedText);
                            updateScheduleList();
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("OCR_ERROR", "OCR Failed: ", e);
                        Toast.makeText(this, "OCR Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });

        } catch (Exception e) {
            android.util.Log.e("PROCESS_IMAGE_ERROR", "Error processing image: ", e);
            Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void parseScheduleText(String text) {
        allSchedules.clear();

        String currentDay = selectedDay;
        String[] lines = text.split("\\n");

        Pattern dayPattern = Pattern.compile("^(MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY)", Pattern.CASE_INSENSITIVE);
        Pattern coursePattern = Pattern.compile("[A-Z]{3,5}\\s?\\d{3,4}[A-Z]?", Pattern.CASE_INSENSITIVE);
        Pattern roomPattern = Pattern.compile("[AC]-\\d{3}");

        String currentCourse = "";
        String currentProgram = "";
        String currentRoom = "";

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            Matcher dayMatcher = dayPattern.matcher(line);
            if (dayMatcher.find()) {
                currentDay = dayMatcher.group(1).toUpperCase();
                continue;
            }

            Matcher courseMatcher = coursePattern.matcher(line);
            Matcher roomMatcher = roomPattern.matcher(line);

            if (courseMatcher.find()) {
                currentCourse = courseMatcher.group(0);
            } else if (roomMatcher.find()) {
                currentRoom = roomMatcher.group(0);
                // Add entry once we have course and room
                if (!currentCourse.isEmpty()) {
                    String entry = String.format("%s | %s [%s] (%s)", currentDay, currentCourse, currentProgram, currentRoom);
                    allSchedules.add(entry);
                    android.util.Log.d("SCHEDULE_PARSED", "Added: " + entry);
                    currentCourse = "";
                    currentRoom = "";
                    currentProgram = "";
                }
            } else {
                currentProgram = line; // Program/description
            }
        }

        // Sort alphabetically
        Collections.sort(allSchedules, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        });

        if (allSchedules.isEmpty()) {
            Toast.makeText(this, "Could not parse schedule from text.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Schedule successfully parsed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateScheduleList() {
        displayedSchedules.clear();
        for (String entry : allSchedules) {
            if (entry.toUpperCase().startsWith(selectedDay.toUpperCase())) {
                displayedSchedules.add(entry);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_schedule, null);
        builder.setView(dialogView);

        Spinner spinnerDay = dialogView.findViewById(R.id.spinnerDay);
        EditText etSubject = dialogView.findViewById(R.id.etSubject);
        TimePicker timeStart = dialogView.findViewById(R.id.timeStart);
        TimePicker timeEnd = dialogView.findViewById(R.id.timeEnd);

        builder.setPositiveButton("Add", (dialog, id) -> {
            String day = spinnerDay.getSelectedItem().toString().trim().toUpperCase(Locale.US);
            String subject = etSubject.getText().toString().trim();
            String startTime = String.format("%02d:%02d", timeStart.getCurrentHour(), timeStart.getCurrentMinute());
            String endTime = String.format("%02d:%02d", timeEnd.getCurrentHour(), timeEnd.getCurrentMinute());

            if (!day.isEmpty() && !subject.isEmpty()) {
                String entry = String.format("%s | %s (%s - %s)", day, subject, startTime, endTime);
                allSchedules.add(entry);
                Collections.sort(allSchedules, new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        return s1.compareTo(s2);
                    }
                });
                updateScheduleList();
            } else {
                Toast.makeText(MainActivity.this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void highlightSelectedDay() {
        for (Map.Entry<String, Button> entry : dayButtons.entrySet()) {
            Button button = entry.getValue();
            if (entry.getKey().equalsIgnoreCase(selectedDay)) {
                button.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                button.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                button.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                button.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
    }
}
