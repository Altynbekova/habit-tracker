package com.example.habittracker;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

// ReminderBroadcastReceiver.java
public class ReminderBroadcastReceiver extends BroadcastReceiver {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @Override
    public void onReceive(Context context, Intent intent) {
        // Retrieve task data from the intent
        String taskTitle = intent.getStringExtra("TASK_TITLE");

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MyApplication.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Set a small icon
                .setContentTitle("Habit Reminder")
                .setContentText(taskTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true); // Dismiss on tap

        // Add action to open app when tapped
        Intent tapIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(intent.getIntExtra("NOTIFICATION_ID", -1), builder.build()); // Use a unique ID for the notification
    }
}

