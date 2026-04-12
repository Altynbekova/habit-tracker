package com.example.habittracker.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;

import com.example.habittracker.db.AppDatabase;
import com.example.habittracker.db.entity.Reminder;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            /*SharedPreferences prefs = context.getSharedPreferences("HabitPrefs", Context.MODE_PRIVATE);

            // Note: In a real app, you'd loop through a list of Habit IDs from a Database.
            // Here is the logic for a single habit ID "101"
            int habitId = 101;
            boolean isEnabled = prefs.getBoolean("enabled_" + habitId, false);

            if (isEnabled) {
                int hour = prefs.getInt("hour_" + habitId, 9);
                int minute = prefs.getInt("minute_" + habitId, 0);
                String name = prefs.getString("name_" + habitId, "Habit");

                scheduleAlarm(context, habitId, name, hour, minute);
            }*/

            // We must use a background thread or a WorkManager
            // because Room/Database operations cannot run on the Main Thread
            new Thread(() -> {
                // Access your database (replace with your actual Database/DAO access)
                AppDatabase db = AppDatabase.getInstance(context);
                List<Reminder> activeReminders = db.reminderDao().getAllEnabledRemindersSync();

                for (Reminder reminder : activeReminders) {
                    // Re-schedule each one using the logic from the previous step
                    NotificationHelper.scheduleAlarm(context, reminder.habitId, db.habitDao().getHabit(reminder.habitId).getName(), reminder.time);
                }
            }).start();
        }
    }

    /*private void scheduleAlarm(Context context, int id, String name, int h, int m) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, HabitReminderReceiver.class);
        intent.putExtra("HABIT_NAME", name);
        intent.putExtra("HABIT_ID", id);

        PendingIntent pi = PendingIntent.getBroadcast(context, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, h);
        cal.set(Calendar.MINUTE, m);
        cal.set(Calendar.SECOND, 0);

        if (Calendar.getInstance().after(cal)) cal.add(Calendar.DAY_OF_MONTH, 1);

        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
    }*/
}
