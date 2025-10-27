package com.example.classping;

import android.app.Dialog;
import android.content.Context;
import android.view.WindowManager;
import android.widget.*;
import androidx.annotation.NonNull;

public class EditScheduleDialog extends Dialog {

    public EditScheduleDialog(@NonNull Context context, Schedule oldSchedule, ScheduleManager manager, Runnable refreshCallback) {
        super(context);
        setContentView(R.layout.dialog_add_schedule);
        setTitle("Edit Schedule");

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
        Button btnSave = findViewById(R.id.btnSaveSchedule);

        String[] days = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
        spinnerDay.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, days));

        int idx = java.util.Arrays.asList(days).indexOf(oldSchedule.getDay());
        spinnerDay.setSelection(idx >= 0 ? idx : 0);
        etSubject.setText(oldSchedule.getSubject());
        etProgram.setText(oldSchedule.getProgram());
        etRoom.setText(oldSchedule.getRoom());
        etNotes.setText(oldSchedule.getNotes());
        tvStart.setText(oldSchedule.getStartTime());
        tvEnd.setText(oldSchedule.getEndTime());

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

            Schedule ns = new Schedule(oldSchedule.getId(),
                    spinnerDay.getSelectedItem().toString(),
                    etSubject.getText().toString().trim(),
                    etProgram.getText().toString().trim(),
                    etRoom.getText().toString().trim(),
                    tvStart.getText().toString(),
                    tvEnd.getText().toString(),
                    etNotes.getText().toString().trim(),
                    "None");

            manager.updateSchedule(oldSchedule.getId(), ns);
            refreshCallback.run();
            dismiss();
        });
    }
}
