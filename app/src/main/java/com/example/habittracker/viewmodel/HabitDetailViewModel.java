package com.example.habittracker.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.habittracker.db.entity.CompletionStatus;
import com.example.habittracker.db.entity.HabitCompletion;
import com.example.habittracker.db.entity.HabitModel;
import com.example.habittracker.repository.AppRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class HabitDetailViewModel extends AndroidViewModel {
    private final AppRepo repository;

    public HabitDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new AppRepo(application);
    }

    public LiveData<List<HabitCompletion>> getHistory(long habitId) {
        return repository.getHistoryForHabit(habitId);
    }

    public void toggleReminder(long reminderId, boolean isEnabled) {
        repository.toggleReminder(reminderId, isEnabled);
    }
}

