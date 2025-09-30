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

import java.text.SimpleDateFormat;
import java.util.*;
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

        dayButtons.put("SUNDAY", findViewById(R.id.btnSun));
        dayButtons.put("MONDAY", findViewById(R.id.btnMon));
        dayButtons.put("TUESDAY", findViewById(R.id.btnTue));
        dayButtons.put("WEDNESDAY", findViewById(R.id.btnWed));
        dayButtons.put("THURSDAY", findViewById(R.id.btnThu));
        dayButtons.put("FRIDAY", findViewById(R.id.btnFri));
        dayButtons.put("SATURDAY", findViewById(R.id.btnSat));

        for (Map.Entry<String, Button> entry : dayButtons.entrySet()) {
            String day = entry.getKey();
            Button button = entry.getValue();
            button.setOnClickListener(v -> {
                selectedDay = day;
                updateScheduleList();
                highlightSelectedDay();
            });
        }

        // Set default day
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

        scheduleList.setOnItemClickListener((parent, view, position, id) -> showAddDialog(position));

        scheduleList.setOnItemLongClickListener((parent, view, position, id) -> {
            String entry = displayedSchedules.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Delete Schedule")
                    .setMessage("Are you sure you want to delete this schedule?\n\n" + entry)
                    .setPositiveButton("Delete", (dialog, which) -> {
                        allSchedules.remove(entry);
                        updateScheduleList();
                        Toast.makeText(this, "Schedule deleted.", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

        // 🔥 Check if launched via Manual button
        String openMode = getIntent().getStringExtra("openMode");
        if ("manual".equals(openMode)) {
            new android.os.Handler().post(() -> showAddDialog());
        }
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
                        if (recognizedText.trim().isEmpty()) {
                            Toast.makeText(this, "No text found in the image.", Toast.LENGTH_SHORT).show();
                        } else {
                            parseScheduleText(recognizedText);
                            updateScheduleList();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "OCR Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
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
                if (!currentCourse.isEmpty()) {
                    String entry = String.format("%s | %s [%s] (%s)", currentDay, currentCourse, currentProgram, currentRoom);
                    allSchedules.add(entry);
                    currentCourse = "";
                    currentRoom = "";
                    currentProgram = "";
                }
            } else {
                currentProgram = line;
            }
        }

        sortSchedulesByTime();

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

    private void showAddDialog(int editPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_schedule, null);
        builder.setView(dialogView);

        Spinner spinnerDay = dialogView.findViewById(R.id.spinnerDay);
        EditText etSubject = dialogView.findViewById(R.id.etSubject);
        EditText etProgram = dialogView.findViewById(R.id.etProgram);
        EditText etRoom = dialogView.findViewById(R.id.etRoom);
        TextView tvStartTime = dialogView.findViewById(R.id.tvStartTime);
        TextView tvEndTime = dialogView.findViewById(R.id.tvEndTime);
        Spinner spinnerReminder = dialogView.findViewById(R.id.spinnerReminder);

        ArrayAdapter<CharSequence> reminderAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.reminder_options,
                android.R.layout.simple_spinner_item
        );
        reminderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReminder.setAdapter(reminderAdapter);

        String[] days = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(dayAdapter);

        // Pre-fill fields if editing
        if (editPosition >= 0) {
            String entry = displayedSchedules.get(editPosition);
            String[] parts = entry.split("\\|");
            if (parts.length >= 2) {
                String dayPart = parts[0].trim();
                spinnerDay.setSelection(Arrays.asList(days).indexOf(dayPart.substring(0,1).toUpperCase() + dayPart.substring(1).toLowerCase()));
            }
        }

        tvStartTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new android.app.TimePickerDialog(this, (view, hour, minute) -> tvStartTime.setText(formatTime(hour, minute)), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
        });
        tvEndTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new android.app.TimePickerDialog(this, (view, hour, minute) -> tvEndTime.setText(formatTime(hour, minute)), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
        });

        builder.setPositiveButton(editPosition >= 0 ? "Update" : "Add", (dialog, id) -> {
            String day = spinnerDay.getSelectedItem().toString().trim().toUpperCase(Locale.US);
            String subject = etSubject.getText().toString().trim();
            String program = etProgram.getText().toString().trim();
            String room = etRoom.getText().toString().trim();
            String startTime = tvStartTime.getText().toString().trim();
            String endTime = tvEndTime.getText().toString().trim();

            if (!day.isEmpty() && !subject.isEmpty()) {
                String entry;
                if (!program.isEmpty() && !room.isEmpty()) {
                    entry = String.format("%s | %s [%s] (%s)", day, subject, program, room);
                } else if (!startTime.isEmpty() && !endTime.isEmpty()) {
                    entry = String.format("%s | %s (%s - %s)", day, subject, startTime, endTime);
                } else {
                    entry = String.format("%s | %s", day, subject);
                }

                if (editPosition >= 0) {
                    String original = displayedSchedules.get(editPosition);
                    int indexInAll = allSchedules.indexOf(original);
                    if (indexInAll >= 0) allSchedules.set(indexInAll, entry);
                } else {
                    allSchedules.add(entry);
                }

                sortSchedulesByTime();

                // 🔥 Auto-switch to chosen day
                selectedDay = day;
                updateScheduleList();
                highlightSelectedDay();
            } else {
                Toast.makeText(MainActivity.this, "Please fill the subject and day.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());
        builder.create().show();
    }

    private void showAddDialog() {
        showAddDialog(-1);
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

    private void sortSchedulesByTime() {
        Collections.sort(allSchedules, (s1, s2) -> compareTimes(extractStartTime(s1), extractStartTime(s2)));
    }

    private String extractStartTime(String scheduleEntry) {
        Pattern pattern = Pattern.compile("\\((\\d{1,2}:\\d{2}\\s?[AP]M)");
        Matcher matcher = pattern.matcher(scheduleEntry);
        if (matcher.find()) return matcher.group(1);
        return "12:00 AM";
    }

    private int compareTimes(String t1, String t2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            return sdf.parse(t1).compareTo(sdf.parse(t2));
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatTime(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.US);
        return sdf.format(cal.getTime());
    }
}
