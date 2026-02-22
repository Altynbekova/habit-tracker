package com.example.habittracker.viewmodel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.habittracker.db.HabitModel;
import com.example.habittracker.repository.AppRepo;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class HabitViewModel extends AndroidViewModel {
    private final AppRepo appRepo;

    public HabitViewModel(@NonNull Application application) {
        super(application);
        appRepo = new AppRepo(application);
    }

    public List<HabitModel> getAllHabitsFuture() throws ExecutionException, InterruptedException {
        return appRepo.getAllHabitsFuture();
    }

    public LiveData<List<HabitModel>> getAllHabitsLive() {
        return appRepo.getAllHabitsLive();
    }

    public void insertHabit(HabitModel habitModel) {
        appRepo.insertHabit(habitModel);
    }

    public void updateHabit(HabitModel habitModel) {
        appRepo.updateHabit(habitModel);
    }

    public void deleteHabit(HabitModel habitModel) {
        appRepo.deleteHabit(habitModel);
    }

    public void deleteById(int id) {
        appRepo.deleteById(id);
    }

    public HabitModel getHabitById(int habitId) throws ExecutionException, InterruptedException {
        return appRepo.getById(habitId);
    }

    /*private HabitRepository habitRepository;
    LiveData<List<HabitModel>> allHabits = habitRepository.getAllHabits();*/

}
