package com.example.habittracker.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.time.LocalTime;
import java.util.Calendar;

public class NotificationHelper {
    public static void scheduleAlarm(Context context, int habitId, String habitName, LocalTime time) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(context, HabitReminderReceiver.class);
        intent.putExtra("HABIT_NAME", habitName);
        intent.putExtra("HABIT_ID", habitId);
        intent.putExtra("hour", time.getHour());
        intent.putExtra("minute", time.getMinute());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, habitId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, time.getHour());
        calendar.set(Calendar.MINUTE, time.getMinute());
        calendar.set(Calendar.SECOND, 0);

        // if the time already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Check if we are allowed to use EXACT alarms
                // Use setExactAndAllowWhileIdle for the first occurrence
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent);
                } else {
                    // Fallback: Use non-exact alarm to avoid crash
                    // This will still fire, but potentially a few minutes late
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent);
                }
            } else {
                // Below Android 12, no permission check is needed
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent);
            }
        } catch (SecurityException e) {
            // Final fallback if system still denies it
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent);
        }


    }

    //only if habit completed or archived
    public static void cancelAlarm(Context context, int habitId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, HabitReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, habitId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
}

