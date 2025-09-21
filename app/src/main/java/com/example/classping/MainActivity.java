package com.example.classping;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import android.provider.MediaStore;
import android.widget.Toast;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class MainActivity extends AppCompatActivity {

    ListView scheduleList;
    Button btnAdd, btnUploadImage;
    ArrayAdapter<String> adapter;
    ArrayList<String> allSchedules; // A list to hold all parsed schedules
    ArrayList<String> displayedSchedules; // A list to hold schedules for the selected day
    String selectedDay = "UNKNOWN_DAY";

    // Map to hold day buttons
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

        // Set click listeners for all day buttons
        for (Map.Entry<String, Button> entry : dayButtons.entrySet()) {
            String day = entry.getKey();
            Button button = entry.getValue();
            button.setOnClickListener(v -> {
                selectedDay = day;
                updateScheduleList();
            });
        }

        // Set the default day to the current day
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

        btnAdd.setOnClickListener(v -> showAddDialog());

        btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select PNG"), 100);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if(imageUri != null) processImage(imageUri);
        }
    }

    // From your MainActivity.java
    @OptIn(markerClass = UnstableApi.class)
    private void processImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            if (bitmap == null) { // Good to have a null check
                Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                return;
            }

            InputImage image = InputImage.fromBitmap(bitmap, 0);
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

            recognizer.process(image)
                    .addOnSuccessListener(visionText -> {
                        String recognizedText = visionText.getText();
                        // **** DEBUG POINT 1: What is the recognizedText? ****
                        androidx.media3.common.util.Log.d("OCR_OUTPUT", "Recognized Text: '" + recognizedText + "'"); // Log the output

                        if (recognizedText.trim().isEmpty()) { // Use trim() to check for empty or whitespace-only strings
                            Toast.makeText(this, "No text found in the image.", Toast.LENGTH_SHORT).show();
                        } else {
                            parseScheduleText(recognizedText);
                            // After parsing, update the list to reflect the current selected day
                            updateScheduleList();
                        }
                    })
                    .addOnFailureListener(e -> {
                        androidx.media3.common.util.Log.e("OCR_ERROR", "OCR Failed in listener: ", e);
                        Toast.makeText(this, "OCR Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });

        } catch (Exception e) {
            androidx.media3.common.util.Log.e("PROCESS_IMAGE_ERROR", "Error in processImage method: ", e);
            Toast.makeText(this, "Error processing image: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void parseScheduleText(String text) {
        allSchedules.clear();

        String currentDay = "UNKNOWN_DAY";
        String[] lines = text.split("\\n");
        String currentSubject = "";
        String currentTime = "";

        // Define patterns for days and times
        Pattern dayPattern = Pattern.compile("^(MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY)", Pattern.CASE_INSENSITIVE);
        Pattern timePattern = Pattern.compile("(\\d{2}:\\d{2})\\s*(AM|PM)?\\s*to\\s*(\\d{2}:\\d{2})\\s*(AM|PM)?", Pattern.CASE_INSENSITIVE);

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            Matcher dayMatcher = dayPattern.matcher(line);
            Matcher timeMatcher = timePattern.matcher(line);

            if (dayMatcher.find()) {
                // If a new day is found, save the previous entry if it exists
                if (!currentSubject.isEmpty()) {
                    addScheduleEntry(currentDay, currentSubject.trim(), currentTime);
                }
                // Update the current day and reset subject/time
                currentDay = dayMatcher.group(1).toUpperCase();
                currentSubject = "";
                currentTime = "";
            } else if (timeMatcher.find()) {
                // If a new time is found, save the previous entry if it exists
                if (!currentSubject.isEmpty()) {
                    addScheduleEntry(currentDay, currentSubject.trim(), currentTime);
                }
                // Update the current time and capture the rest of the line as the subject
                currentTime = timeMatcher.group(0);
                String remaining = line.substring(timeMatcher.end()).trim();
                currentSubject = remaining;
            } else {
                // Otherwise, this line is part of the current subject
                currentSubject += " " + line;
            }
        }

        // Add the last entry after the loop
        if (!currentSubject.isEmpty()) {
            addScheduleEntry(currentDay, currentSubject.trim(), currentTime);
        }

        if (allSchedules.isEmpty()) {
            Toast.makeText(this, "Could not parse schedule from text.", Toast.LENGTH_LONG).show();
            androidx.media3.common.util.Log.w("SCHEDULE_PARSED", "No schedule entries found on text: <<<" + text + ">>>");
        } else {
            Toast.makeText(this, "Schedule successfully parsed!", Toast.LENGTH_SHORT).show();
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private void addScheduleEntry(String day, String title, String timeRange) {
        String scheduleEntry = String.format("%s | %s (%s)", day, title, timeRange);
        allSchedules.add(scheduleEntry);
        androidx.media3.common.util.Log.d("SCHEDULE_PARSED", "Added: " + scheduleEntry);
    }

    // The previous addScheduleEntry method is no longer needed
    // private void addScheduleEntry(String day, String title, String startTime, String endTime) { ... }


    private void updateScheduleList() {
        displayedSchedules.clear();
        for (String scheduleEntry : allSchedules) {
            if (scheduleEntry.toUpperCase().startsWith(selectedDay.toUpperCase())) {
                displayedSchedules.add(scheduleEntry);
            }
        }
        adapter.notifyDataSetChanged();
    }


    private void showAddDialog() {
        // Your existing add-dialog code
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_schedule, null);
        builder.setView(dialogView);

        // Corrected findViewById calls to match your new XML layout
        Spinner spinnerDay = dialogView.findViewById(R.id.spinnerDay);
        EditText etSubject = dialogView.findViewById(R.id.etSubject);
        TimePicker timeStart = dialogView.findViewById(R.id.timeStart);
        TimePicker timeEnd = dialogView.findViewById(R.id.timeEnd);

        builder.setPositiveButton("Add", (dialog, id) -> {
            // Get selected day from the spinner
            String day = spinnerDay.getSelectedItem().toString().trim().toUpperCase(Locale.US);
            String subject = etSubject.getText().toString().trim();

            // Handle TimePicker values using deprecated methods for API 21 compatibility
            String startTime = String.format("%02d:%02d", timeStart.getCurrentHour(), timeStart.getCurrentMinute());
            String endTime = String.format("%02d:%02d", timeEnd.getCurrentHour(), timeEnd.getCurrentMinute());

            if (!day.isEmpty() && !subject.isEmpty()) {
                String scheduleEntry = day + " | " + subject + " (" + startTime + " - " + endTime + ")";
                allSchedules.add(scheduleEntry);
                updateScheduleList();
            } else {
                Toast.makeText(MainActivity.this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
