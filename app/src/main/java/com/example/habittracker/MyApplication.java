package com.example.habittracker;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import com.example.habittracker.util.Utils;

public class MyApplication extends Application {
    public static final String CHANNEL_ID = "habit_tracker_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Utils.TAG, "Application started! Executing initial code.");
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//todo delete as minSdk=26
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
