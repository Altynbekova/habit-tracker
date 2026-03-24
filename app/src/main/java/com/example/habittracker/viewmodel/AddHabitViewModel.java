package com.example.habittracker.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;

import com.example.habittracker.repository.AppRepo;

import java.time.LocalTime;
import java.util.Locale;

public class AddHabitViewModel extends AndroidViewModel {
    private final AppRepo appRepo;

    public AddHabitViewModel(Application application) {
        super(application);
        this.appRepo = new AppRepo(application);
    }

    public final ObservableField<String> notificationTime = new ObservableField<>("--:--");

    public void updateTime(int habitId, int hour, int minute) {
//        String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        String time = LocalTime.of(hour, minute).toString();
        notificationTime.set(time);
    }

}
