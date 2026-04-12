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
import com.example.habittracker.db.entity.HabitWithDetails;
import com.example.habittracker.db.entity.MarkDoneResult;
import com.example.habittracker.db.entity.Reminder;
import com.example.habittracker.ui.SortType;

import java.time.LocalDate;
import java.time.LocalTime;
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

    public AppRepo(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        habitDao = db.habitDao();
        completionDao = db.habitCompletionDao();
        categoryDao = db.categoryDao();
        reminderDao = db.reminderDao();
    }

    public LiveData<Boolean> getDuplicateEntryError() {
        return isDuplicateEntry;
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

    public LiveData<HabitModel> getByIdLive(int habitId) {
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
        switch (sortType) {
            case NAME:
                return categoryId == null ? habitDao.getAllSortedByName() : habitDao.getFilteredSortedByName(categoryId);
            case DATE:
                return categoryId == null ? habitDao.getAllSortedByDate() : habitDao.getFilteredSortedByDate(categoryId);
            default:
                return habitDao.getAllSortedByStreak();
        }
    }

    public LiveData<List<HabitModel>> getFilteredSorted(Long categoryId, String sortType, int asc) {
        return habitDao.getFilteredSorted(categoryId, sortType, asc);
    }

    public void updateReminderTime(int id, LocalTime time) {

    }

    public void setReminder(int habitId, LocalTime time) {

        executor.execute(() -> {
            Reminder reminder = new Reminder();
            reminder.habitId = habitId;
            reminder.time = time;
            reminder.enabled = true;
            // This will insert a new one or replace the existing one for this habit
            reminderDao.insertOrUpdate(reminder);
        });
    }

    public LiveData<Reminder> getReminderForHabit(int habitId) {
        return reminderDao.getReminderByHabitId(habitId);
    }

    public void updateReminderStatus(int habitId, boolean isChecked) {
        executor.execute(() -> {
            reminderDao.updateStatusByHabitId(habitId, isChecked);
        });
    }

    public LiveData<HabitWithDetails> getHabitWithDetails(int habitId) {
        return habitDao.getHabitWithDetails(habitId);
    }

    public HabitWithDetails getHabitWithDetailsSync(int habitId) {
        return habitDao.getHabitWithDetailsSync(habitId);
    }

    public LiveData<MarkDoneResult> markHabitAsDone(int habitId) {
/*//        MutableLiveData<Boolean> result = new MutableLiveData<>();
        MutableLiveData<MarkDoneResult> resultLiveData = new MutableLiveData<>();
        executor.execute(() -> {
//            boolean wasSuccessful = habitDao.markAsDoneAndCalculateStreak(habitId, LocalDate.now());
            MarkDoneResult result = habitDao.markAsDoneAndCalculateStreak(habitId, LocalDate.now());
            // postValue safely handles the thread switch for you!
//            result.postValue(wasSuccessful);
            resultLiveData.postValue(result);
        });
        return resultLiveData;*/
        MutableLiveData<MarkDoneResult> resultLiveData = new MutableLiveData<>();
        executor.execute(() -> {
            MarkDoneResult result = habitDao.markAsDoneAndCalculateStreak(habitId, LocalDate.now());
            resultLiveData.postValue(result);
        });
        return resultLiveData;
    }
}
