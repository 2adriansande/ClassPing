package com.example.classping;

import android.app.Dialog;
import android.content.Context;
import android.view.WindowManager;
import android.widget.*;
import androidx.annotation.NonNull;

public class AddScheduleDialog extends Dialog {

    public AddScheduleDialog(@NonNull Context context, ScheduleManager manager, Runnable refreshCallback) {
        super(context);
        setContentView(R.layout.dialog_add_schedule);
        setTitle("Add Schedule");

        if (getWindow() != null) {
            getWindow().setLayout((int)(context.getResources().getDisplayMetrics().widthPixels * 0.95),
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }

        Spinner spinnerDay = findViewById(R.id.spinnerDay);
        EditText etSubject = findViewById(R.id.etSubject);
        EditText etProgram = findViewById(R.id.etProgram);
        EditText etRoom = findViewById(R.id.etRoom);
        EditText etNotes = findViewById(R.id.etNotes);
        TextView tvStart = findViewById(R.id.tvStartTime);
        TextView tvEnd = findViewById(R.id.tvEndTime);
        Spinner spinnerReminder = findViewById(R.id.spinnerReminder);
        Button btnSave = findViewById(R.id.btnSaveSchedule);

        String[] days = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
        spinnerDay.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, days));

        // Reminder options (stored in model, although manager sets default reminder behavior)
        String[] reminders = {"None","5 minutes before","10 minutes before","30 minutes before","1 hour before"};
        spinnerReminder.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, reminders));

        tvStart.setOnClickListener(v -> TimePickerHelper.showTimePicker(context, tvStart));
        tvEnd.setOnClickListener(v -> TimePickerHelper.showTimePicker(context, tvEnd));

        btnSave.setOnClickListener(v -> {
            if (etSubject.getText().toString().trim().isEmpty() ||
                    etRoom.getText().toString().trim().isEmpty() ||
                    tvStart.getText().toString().trim().isEmpty() ||
                    tvEnd.getText().toString().trim().isEmpty()) {
                Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String reminder = spinnerReminder.getSelectedItem() != null ?
                    spinnerReminder.getSelectedItem().toString() : "None";

            Schedule s = new Schedule(
                    spinnerDay.getSelectedItem().toString(),
                    etSubject.getText().toString().trim(),
                    etProgram.getText().toString().trim(),
                    etRoom.getText().toString().trim(),
                    tvStart.getText().toString(),
                    tvEnd.getText().toString(),
                    etNotes.getText().toString().trim(),
                    reminder
            );

            // Manager will add to DB and schedule reminder
            manager.addSchedule(s);
            // Optional: also sync to firebase if desired
            manager.syncToFirebase();

            refreshCallback.run();
            dismiss();
        });
    }
}
