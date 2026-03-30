package com.example.habittracker.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.habittracker.repository.AppRepo;

import java.time.LocalTime;

public class AddHabitViewModel extends AndroidViewModel {
    public final MutableLiveData<String> notificationTime = new MutableLiveData<>("--:--");
    private final AppRepo appRepo;

    public AddHabitViewModel(Application application) {
        super(application);
        this.appRepo = new AppRepo(application);
    }

    public void updateNotificationTime(int habitId, int hour, int minute) {
//        String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        String time = LocalTime.of(hour, minute).toString();
        notificationTime.setValue(time);
    }

}
