package com.example.habittracker.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.habittracker.db.AppDatabase;
import com.example.habittracker.db.HabitModel;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AppRepo {
    private final AppDatabase appDatabase;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public AppRepo(Context context) {
        appDatabase = AppDatabase.getInstance(context);
    }

    public List<HabitModel> getAllHabitsFuture() throws ExecutionException, InterruptedException {

        Callable<List<HabitModel>> callable = new Callable<List<HabitModel>>() {
            @Override
            public List<HabitModel> call() throws Exception {
                return appDatabase.habitDao().getAllHabitsFuture();
            }
        };

        Future<List<HabitModel>> future = Executors.newSingleThreadExecutor().submit(callable);
        return future.get();
    }

    public LiveData<List<HabitModel>> getAllHabitsLive() {
        return appDatabase.habitDao().getAllHabitsLive();
    }

    public void insertHabit(HabitModel habitModel) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                appDatabase.habitDao().insertHabit(habitModel);
            }
        });
    }

    public void updateHabit(HabitModel habitModel) {
        executor.execute(() -> appDatabase.habitDao().updateHabit(habitModel));
    }

    public void deleteHabit(HabitModel habitModel) {
        executor.execute(() -> appDatabase.habitDao().deleteHabit(habitModel));
    }

    public void deleteById(int id) {
        executor.execute(() -> appDatabase.habitDao().deleteHabitById(id));
    }

    public HabitModel getById(int habitId) throws ExecutionException, InterruptedException {
        Callable<HabitModel> callable = new Callable<HabitModel>() {
            @Override
            public HabitModel call() throws Exception {
                return appDatabase.habitDao().getHabit(habitId);
            }
        };

        Future<HabitModel> future = Executors.newSingleThreadExecutor().submit(callable);
        return future.get();
    }

    public void updateTime(int id, String time) {
        executor.execute(() -> appDatabase.habitDao().updateTime(id, time));
    }
}
