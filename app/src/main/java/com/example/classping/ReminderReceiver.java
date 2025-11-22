package com.example.classping;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "classping_reminder_channel";
    private static final String CHANNEL_NAME = "ClassPing Reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        String subject = intent.getStringExtra("subject");
        String startTime = intent.getStringExtra("startTime");
        String day = intent.getStringExtra("day");

        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE
                        : 0
        );

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            // Create notification channel only on API 26+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Notifications for upcoming classes");
                channel.enableLights(true);
                channel.setLightColor(Color.BLUE);
                notificationManager.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                    context,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? CHANNEL_ID : ""
            )
                    .setSmallIcon(R.drawable.ic_notification) // Replace with your icon
                    .setContentTitle("Upcoming Class: " + (subject == null ? "Class" : subject))
                    .setContentText("Starts at " + (startTime == null ? "soon" : startTime)
                            + " on " + (day == null ? "" : day))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            int notifId = (int) System.currentTimeMillis();
            notificationManager.notify(notifId, builder.build());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for upcoming classes");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
