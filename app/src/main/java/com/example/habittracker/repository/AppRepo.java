package com.example.habittracker.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.habittracker.db.AppDatabase;
import com.example.habittracker.db.dao.CategoryDao;
import com.example.habittracker.db.dao.HabitCompletionDao;
import com.example.habittracker.db.dao.HabitDao;
import com.example.habittracker.db.dao.ReminderDao;
import com.example.habittracker.db.entity.Category;
import com.example.habittracker.db.entity.HabitCompletion;
import com.example.habittracker.db.entity.HabitModel;
import com.example.habittracker.ui.SortType;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AppRepo {
    private static final int POOL_SIZE = 4;
    private final HabitDao habitDao;
    private final HabitCompletionDao completionDao;
    private final CategoryDao categoryDao;
    private final ReminderDao reminderDao;
    private final Executor executor = Executors.newFixedThreadPool(POOL_SIZE);
    private final MutableLiveData<Boolean> isDuplicateEntry = new MutableLiveData<>();

    public LiveData<Boolean> getDuplicateEntryError() {
        return isDuplicateEntry;
    }

    public AppRepo(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        habitDao = db.habitDao();
        completionDao = db.habitCompletionDao();
        categoryDao = db.categoryDao();
        reminderDao = db.reminderDao();
    }

    public List<HabitModel> getAllHabitsFuture() throws ExecutionException, InterruptedException {

        Callable<List<HabitModel>> callable = new Callable<List<HabitModel>>() {
            @Override
            public List<HabitModel> call() throws Exception {
                return habitDao.getAllHabitsFuture();
            }
        };

        Future<List<HabitModel>> future = Executors.newSingleThreadExecutor().submit(callable);
        return future.get();
    }

    public LiveData<List<HabitModel>> getAllHabitsLive() {
        return habitDao.getAllHabitsLive();
    }

    public LiveData<List<HabitModel>> getAllActiveHabits() {
        return habitDao.getAllActiveHabits();
    }

    public void insertHabit(HabitModel habitModel) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                habitDao.insert(habitModel);
            }
        });
    }

    public void updateHabit(HabitModel habitModel) {
        executor.execute(() -> habitDao.update(habitModel));
    }

    public void deleteHabit(HabitModel habitModel) {
        executor.execute(() -> habitDao.delete(habitModel));
    }

    public void deleteById(int id) {
        executor.execute(() -> habitDao.deleteById(id));
    }

    public HabitModel getById(int habitId) throws ExecutionException, InterruptedException {
        Callable<HabitModel> callable = new Callable<HabitModel>() {
            @Override
            public HabitModel call() throws Exception {
                return habitDao.getHabit(habitId);
            }
        };

        Future<HabitModel> future = Executors.newSingleThreadExecutor().submit(callable);
        return future.get();
    }

    public LiveData<HabitModel> getByIdLive(int habitId) throws ExecutionException, InterruptedException {
        return habitDao.getHabitLive(habitId);
    }

    public void updateTime(int id, String time) {
        executor.execute(() -> habitDao.updateTime(id, time));
    }

    public void archiveHabit(long habitId) {
        executor.execute(() -> habitDao.archiveHabit(habitId));
    }

    public long markHabitComplete(HabitCompletion completion) throws ExecutionException, InterruptedException {
        /*Callable<Long> callable = new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                return completionDao.insert(completion);
            }
        };

        Future<Long> future = Executors.newSingleThreadExecutor().submit(callable);
        return future.get();*/
        Callable<Long> callable = () -> habitDao.insertAndCheckGoal(completion);

        Future<Long> future = Executors.newSingleThreadExecutor().submit(callable);
        return future.get();
    }

    public LiveData<List<HabitCompletion>> getHistoryForHabit(long habitId) {
        return completionDao.getCompletionsForHabit(habitId);
    }

    public LiveData<List<Category>> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    public LiveData<Category> getCategory(String name) {
        return categoryDao.getCategory(name);
    }

    public void insertCategory(Category category) {
        executor.execute(() -> categoryDao.insert(category));
    }

    public void toggleReminder(long reminderId, boolean isEnabled) {
        executor.execute(() -> reminderDao.updateReminderStatus(reminderId, isEnabled));
    }

    public void restoreHabit(long habitId) {
        executor.execute(() -> habitDao.restoreHabit(habitId));
    }

    public LiveData<List<HabitModel>> getHabitsByCategory(long categoryId) {
        return habitDao.getHabitsWithCategory(categoryId);
    }

    public LiveData<List<HabitModel>> getFilteredAndSortedHabits(Long categoryId, SortType sortType) {
        /*if (categoryId == null) {
            switch (sortType) {
                case NAME_ASC:
                    return habitDao.getAllSortedByName();
                case DATE_NEWEST:
                    return habitDao.getAllSortedByDate();
                default:
                    return habitDao.getAllSortedByStreak();
            }
        }*/

        switch (sortType) {
            case NAME:
                return categoryId == null ? habitDao.getAllSortedByName() : habitDao.getFilteredSortedByName(categoryId);
            case DATE:
                return categoryId == null ? habitDao.getAllSortedByDate() : habitDao.getFilteredSortedByDate(categoryId);
            default:
//                return categoryId == null ? habitDao.getAllSortedByStreak() : habitDao.getFilteredSortedByStreak();
                return habitDao.getAllSortedByStreak();
        }
    }

    public LiveData<List<HabitModel>> getFilteredSorted(Long categoryId, String sortType, int asc) {
        return habitDao.getFilteredSorted(categoryId, sortType, asc);
    }
}
