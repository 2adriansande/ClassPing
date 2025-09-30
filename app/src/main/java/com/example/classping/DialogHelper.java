package com.example.classping;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;

public class DialogHelper {

    public interface ScheduleDialogListener {
        void onSave(Schedule schedule);
    }

    public static void showScheduleDialog(Context context, Schedule existing, ScheduleDialogListener listener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_add_schedule, null);

        Spinner spinnerDay = view.findViewById(R.id.spinnerDay);
        EditText etSubject = view.findViewById(R.id.etSubject);
        EditText etRoom = view.findViewById(R.id.etRoom);
        EditText etProgram = view.findViewById(R.id.etProgram);
        EditText etNotes = view.findViewById(R.id.etNotes);
        TextView tvStartTime = view.findViewById(R.id.tvStartTime);
        TextView tvEndTime = view.findViewById(R.id.tvEndTime);
        Spinner spinnerReminder = view.findViewById(R.id.spinnerReminder);

        // Day options
        String[] days = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, days);
        spinnerDay.setAdapter(dayAdapter);

        // Reminder options
        String[] reminders = {"None","5 minutes before","10 minutes before","30 minutes before","1 hour before"};
        ArrayAdapter<String> reminderAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, reminders);
        spinnerReminder.setAdapter(reminderAdapter);

        // Populate fields if editing
        if (existing != null) {
            etSubject.setText(existing.getSubject());
            etRoom.setText(existing.getRoom());
            etProgram.setText(existing.getProgram());
            etNotes.setText(existing.getNotes());
            tvStartTime.setText(existing.getStartTime());
            tvEndTime.setText(existing.getEndTime());

            int dayPos = dayAdapter.getPosition(existing.getDay());
            if (dayPos >= 0) spinnerDay.setSelection(dayPos);

            int reminderPos = reminderAdapter.getPosition(existing.getReminder());
            if (reminderPos >= 0) spinnerReminder.setSelection(reminderPos);
        }

        // Time pickers
        tvStartTime.setOnClickListener(v -> pickTime(context, tvStartTime));
        tvEndTime.setOnClickListener(v -> pickTime(context, tvEndTime));

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(existing == null ? "Add Schedule" : "Edit Schedule")
                .setView(view)
                .setPositiveButton("Save", (d, which) -> {
                    String subject = etSubject.getText().toString().trim();
                    String room = etRoom.getText().toString().trim();
                    String program = etProgram.getText().toString().trim();
                    String notes = etNotes.getText().toString().trim();
                    String day = spinnerDay.getSelectedItem().toString();
                    String startTime = tvStartTime.getText().toString();
                    String endTime = tvEndTime.getText().toString();
                    String reminder = spinnerReminder.getSelectedItem().toString();

                    Schedule schedule = (existing != null) ? existing : new Schedule();
                    schedule.setSubject(subject);
                    schedule.setRoom(room);
                    schedule.setProgram(program);
                    schedule.setNotes(notes);
                    schedule.setDay(day);
                    schedule.setStartTime(startTime);
                    schedule.setEndTime(endTime);
                    schedule.setReminder(reminder);

                    listener.onSave(schedule);
                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.show();
    }

    private static void pickTime(Context context, TextView target) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(context, (view, hourOfDay, min) -> {
            String amPm = (hourOfDay < 12) ? "AM" : "PM";
            int hour12 = (hourOfDay % 12 == 0) ? 12 : hourOfDay % 12;
            String time = String.format("%02d:%02d %s", hour12, min, amPm);
            target.setText(time);
        }, hour, minute, false).show();
    }
}
