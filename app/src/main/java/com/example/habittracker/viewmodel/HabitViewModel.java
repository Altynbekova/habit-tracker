package com.example.habittracker.viewmodel;


import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import com.example.habittracker.db.entity.Category;
import com.example.habittracker.db.entity.CompletionStatus;
import com.example.habittracker.db.entity.HabitCompletion;
import com.example.habittracker.db.entity.HabitModel;
import com.example.habittracker.db.entity.HabitWithDetails;
import com.example.habittracker.db.entity.MarkDoneResult;
import com.example.habittracker.db.entity.Reminder;
import com.example.habittracker.repository.AppRepo;
import com.example.habittracker.ui.SortType;
import com.example.habittracker.util.NotificationHelper;
import com.example.habittracker.util.SingleLiveEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

import kotlin.Triple;

public class HabitViewModel extends AndroidViewModel {
    public final LiveData<List<HabitModel>> filteredHabits;
    private final AppRepo repository;
    private final LiveData<List<HabitModel>> allActiveHabits;
    private final MutableLiveData<SortType> sortType = new MutableLiveData<>(SortType.NAME);
    private final MutableLiveData<Long> filterCategoryId = new MutableLiveData<>(null); // null = All
    private final MutableLiveData<Boolean> isAscending = new MutableLiveData<>(true);
    private final MutableLiveData<Long> selectedCategoryId = new MutableLiveData<>(null);
    private final MutableLiveData<String> selectedSortType = new MutableLiveData<>("NAME");
    //    public final LiveData<List<HabitModel>> habits;
    private final MutableLiveData<String> completionStatus = new MutableLiveData<>();
    private final SingleLiveEvent<MarkDoneResult> markDoneEvent = new SingleLiveEvent<>();


    public HabitViewModel(@NonNull Application application) {
        super(application);
        repository = new AppRepo(application);
        allActiveHabits = repository.getAllActiveHabits();
        /*filteredHabits = Transformations.switchMap(filterCategoryId, categoryId -> {
            if (categoryId == null) {
                return repository.getAllActiveHabits();
            } else {
                return repository.getHabitsByCategory(categoryId);
            }
        });*/

        /*// Combine all filters: Category, Search, and Sort
        MediatorLiveData<Pair<Long, SortType>> filterMerger = new MediatorLiveData<>();
        // ... add sources for filterCategoryId and searchQuery ...
        filterMerger.addSource(sortType, sort ->
                filterMerger.setValue(new Pair<>(filterCategoryId.getValue(), sort)));

        filteredHabits = Transformations.switchMap(filterMerger, filters -> {
            // Logic to choose the right repository method based on SortType
            return repository.getFilteredAndSortedHabits(filters.getFirst(), filters.getSecond());
        });*/

        // Merge both chip selections into one trigger
        MediatorLiveData<Triple<Long, String, Boolean>> filterMerger = new MediatorLiveData<>();
        filterMerger.addSource(selectedCategoryId, id ->
                filterMerger.setValue(new Triple<>(id, selectedSortType.getValue(), isAscending.getValue())));

        filterMerger.addSource(selectedSortType, type ->
                filterMerger.setValue(new Triple<>(selectedCategoryId.getValue(), type, isAscending.getValue())));

        filterMerger.addSource(isAscending, asc ->
                filterMerger.setValue(new Triple<>(selectedCategoryId.getValue(), selectedSortType.getValue(), asc)));

        filteredHabits = Transformations.switchMap(filterMerger, params ->
                repository.getFilteredSorted(params.getFirst(), params.getSecond(), params.getThird() ? 1 : 0));
    }

    public LiveData<List<HabitModel>> getAllActiveHabits() {
        return allActiveHabits;
    }

    public List<HabitModel> getAllHabitsFuture() throws ExecutionException, InterruptedException {
        return repository.getAllHabitsFuture();
    }

    public LiveData<List<HabitModel>> getAllHabitsLive() {
        return repository.getAllHabitsLive();
    }

    public void addHabit(HabitModel habitModel) {
        repository.insertHabit(habitModel);
    }

    public void updateHabit(HabitModel habitModel) {
        repository.updateHabit(habitModel);
    }

    public void deleteHabit(HabitModel habitModel) {
        repository.deleteHabit(habitModel);
    }

    public void deleteById(int id) {
        repository.deleteById(id);
    }

    public HabitModel getHabitById(int habitId) throws ExecutionException, InterruptedException {
        return repository.getById(habitId);
    }

    public LiveData<HabitModel> getLiveHabitById(int habitId) {
        return repository.getByIdLive(habitId);
    }

    public void updateTime(int id, String time) {
        repository.updateTime(id, time);
    }

    /*private HabitRepository habitRepository;
    LiveData<List<HabitModel>> allHabits = habitRepository.getAllHabits();*/

    public void archiveHabit(long habitId) {
        repository.archiveHabit(habitId);
    }

    public Long markAsCompleted(int habitId) throws ExecutionException, InterruptedException {
        HabitCompletion completion = new HabitCompletion();
        completion.habitId = habitId;
        completion.completionDate = LocalDate.now();
        completion.status = CompletionStatus.COMPLETED;
        completion.completedAt = LocalDateTime.now();

        return repository.markHabitComplete(completion);
    }

    public void restoreHabit(int habitId) {
        repository.restoreHabit(habitId);
        /*HabitWithDetails details = repository.getHabitWithDetailsSync(habitId);
        NotificationHelper.scheduleAlarm(getApplication(), habitId, details.habit.getName(), details.reminder.time);*/
    }

    public LiveData<List<HabitCompletion>> getHistoryForHabit(int habitId) {
        return repository.getHistoryForHabit(habitId);
    }

    public LiveData<List<Category>> getAllCategories() {
        return repository.getAllCategories();
    }

    public void setFilter(Long categoryId) {
        filterCategoryId.setValue(categoryId);
    }

    /*public void setSortType(SortType sortType) {
        this.sortType.setValue(sortType);
    }*/

    public void setCategory(Long id) {
        selectedCategoryId.setValue(id);
    }

    /*public void setCategory(String name) {
        selectedCategoryId.setValue(repository.getCategory(name).getValue().id);
    }*/

    public void setSortType(SortType type) {
        selectedSortType.setValue(type.name());
    }

    public void toggleDirection() {
        isAscending.setValue(Boolean.FALSE.equals(isAscending.getValue()));
    }

    public LiveData<Boolean> getIsAscending() {
        return isAscending;
    }

    public void updateReminderTime(int id, LocalTime time) {

        // You'll need a method in your DAO: @Query("UPDATE habits SET reminderTime = :time WHERE id = :id")
        repository.updateReminderTime(id, time);

    }

    public void setReminder(int habitId, LocalTime time) {
        repository.setReminder(habitId, time);
    }


    public LiveData<Reminder> getReminderForHabit(int habitId) {
        return repository.getReminderForHabit(habitId);
    }

    public void updateReminderStatus(int habitId, boolean isChecked) {
        repository.updateReminderStatus(habitId, isChecked);
    }

    public LiveData<HabitWithDetails> getHabitDetail(int habitId) {
        return repository.getHabitWithDetails(habitId);
    }

    public void toggleReminder(int habitId, boolean isChecked) {
        // 1. Update the DB status
        updateReminderStatus(habitId, isChecked);

        // 2. Schedule or Cancel the Alarm based on the toggle
        // We use a background thread to fetch the latest data before scheduling
        AsyncTask.execute(() -> {
            HabitWithDetails details = repository.getHabitWithDetailsSync(habitId);
            if (details != null && details.reminder != null) {
//                if (isChecked) {
                NotificationHelper.scheduleAlarm(
                        getApplication(),
                        habitId,
                        details.habit.getName(),
                        details.reminder.time
                );
//                } else {
//                    NotificationHelper.cancelAlarm(getApplication(), habitId);
//                }
            }
        });
    }

    public void completeHabit(int habitId) {
        // Observe the one-time result from the repository
        /*repository.markHabitAsDone(habitId).observeForever(new Observer<MarkDoneResult>() {
            @Override
            public void onChanged(MarkDoneResult result) {
                *//*if (wasSuccessful) {
                    completionStatus.setValue("Great job! Streak updated.");
                } else {
                    completionStatus.setValue("Already done today!");
                }*//*
                markDoneEvent.setValue(result);
                // Clean up to prevent leaks (since this is a one-time event)
                repository.markHabitAsDone(habitId).removeObserver(this);
            }
        });*/
        LiveData<MarkDoneResult> source = repository.markHabitAsDone(habitId);
        source.observeForever(new Observer<MarkDoneResult>() {
            @Override
            public void onChanged(MarkDoneResult result) {
                markDoneEvent.setValue(result);
                source.removeObserver(this);
            }
        });
    }

    public LiveData<String> getCompletionStatus() {
        return completionStatus;
    }

    public LiveData<MarkDoneResult> getMarkDoneEvent() { return markDoneEvent; }
}
