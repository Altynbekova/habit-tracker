package com.example.habittracker;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

public class MyApplication extends Application {
    private static final String TAG = "HabitTrackerApplication";
    public static final String CHANNEL_ID = "habit_tracker_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        // Put the code you want to execute here
        Log.d(TAG, "Application started! Executing initial code.");
        // Example: Initialize a database, a network service, etc.

//        BroadcastReceiver (для запуска после перезагрузки устройства):
//        Требует разрешения RECEIVE_BOOT_COMPLETED в манифесте.

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//todo delete as minSdk=26
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this.
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
