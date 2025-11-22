package com.example.classping;

import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.TextView;
import java.util.Calendar;

public class TimePickerHelper {
    public static void showTimePicker(Context context, TextView target) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        new TimePickerDialog(context, (view, hourOfDay, minute1) -> {
            String time = String.format("%02d:%02d", hourOfDay, minute1);
            target.setText(time);
        }, hour, minute, false).show();
    }
}
