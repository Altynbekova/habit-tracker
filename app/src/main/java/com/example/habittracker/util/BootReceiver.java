package com.example.habittracker.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.habittracker.db.AppDatabase;
import com.example.habittracker.db.entity.Reminder;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            new Thread(() -> {
                AppDatabase db = AppDatabase.getInstance(context);
                List<Reminder> activeReminders = db.reminderDao().getAllEnabledRemindersSync();

                for (Reminder reminder : activeReminders) {
                    // Re-schedule each one
                    NotificationHelper.scheduleAlarm(context, reminder.getHabitId(),
                            db.habitDao().getHabit(reminder.getHabitId()).getName(),
                            reminder.getTime());
                }
            }).start();
        }
    }
}
