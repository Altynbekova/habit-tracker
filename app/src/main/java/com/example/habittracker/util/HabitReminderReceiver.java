package com.example.habittracker.util;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.habittracker.MainActivity;
import com.example.habittracker.R;
import com.example.habittracker.db.AppDatabase;
import com.example.habittracker.db.entity.HabitWithDetails;
import com.example.habittracker.db.entity.Reminder;
import com.example.habittracker.repository.AppRepo;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;

public class HabitReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "habit_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Ensure you have POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // We cannot ask for permission here!
                // Just log it and stop so the app doesn't crash.
                Log.e("HabitReminder", "Notification permission not granted. Skipping.");
                return;
            }
        }

        // 1. Get data passed from the Intent
        String habitName = intent.getStringExtra("HABIT_NAME");
        int habitId = intent.getIntExtra("HABIT_ID", 0);
        int hour = intent.getIntExtra("hour", 0);
        int minute = intent.getIntExtra("minute", 0);
        AsyncTask.execute(() -> {
            HabitWithDetails details = new AppRepo(context).getHabitWithDetailsSync(habitId);
            if (details != null && details.reminder != null && details.reminder.enabled && !details.habit.isArchived) {
                // 2. Create the Notification Channel (Required for Android 8.0+)
                createNotificationChannel(context);

                // 3. Set what happens when the user taps the notification
                Intent activityIntent = new Intent(context, MainActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(
                        context, habitId, activityIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                // 4. Build the notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification) // Use your icon
                        .setContentTitle("Habit Reminder")
                        .setContentText("Time for: " + habitName)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent);

                // 5. Show it
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(habitId, builder.build());

            }
        });


        // --- CALCULATION LOGIC ---
        Calendar calendar = Calendar.getInstance();

        // Use this for an instant 2-minute test:
        calendar.add(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

    /*
    // Regular Daily Logic (Commented out for 2-minute test)
    calendar.set(Calendar.HOUR_OF_DAY, time.getHour());
    calendar.set(Calendar.MINUTE, time.getMinute());
    if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
        calendar.add(Calendar.DAY_OF_MONTH, 1);
    }
    */
        // -------------------------

        // 2. RESCHEDULE
//        LocalTime nextTime = LocalTime.of(hour, minute);// This makes it "Daily"
        LocalTime nextTime = LocalDateTime.ofInstant(
                        calendar.toInstant(),
                        calendar.getTimeZone().toZoneId())
                .toLocalTime();

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            Reminder reminder = db.reminderDao().getReminderForHabit(habitId);

            // Only reschedule if the user hasn't turned off the switch in the meantime
//            if (reminder != null && reminder.enabled) {
            if (reminder != null) {
                NotificationHelper.scheduleAlarm(context, habitId, habitName, nextTime);
            }
        }).start();


//        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Habit Reminders", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification) // Ensure this exists
                .setContentTitle("Habit Reminder")
                .setContentText("Time for: " + habitName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Using habitId ensures multiple habits show separate notifications
        manager.notify(habitId, builder.build());*/
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Habit Reminders", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for scheduled habits");

            /*NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);*/
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
