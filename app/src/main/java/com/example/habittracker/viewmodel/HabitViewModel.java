package com.example.habittracker.viewmodel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

import com.example.habittracker.db.entity.Category;
import com.example.habittracker.db.entity.HabitCompletion;
import com.example.habittracker.db.entity.HabitModel;
import com.example.habittracker.db.entity.HabitWithCategory;
import com.example.habittracker.db.entity.MarkDoneResult;
import com.example.habittracker.db.entity.Reminder;
import com.example.habittracker.repository.AppRepo;
import com.example.habittracker.ui.SortType;
import com.example.habittracker.util.SingleLiveEvent;

import java.time.LocalTime;
import java.util.List;

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

    private final MediatorLiveData<HabitModel> habitState = new MediatorLiveData<>();


    public HabitViewModel(@NonNull Application application, AppRepo repository) {
        super(application);
        this.repository = repository;
        allActiveHabits = repository.getAllActiveHabits();
        // merging both chip selections into one trigger
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

    public void addHabit(HabitModel habitModel) {
        repository.insertHabit(habitModel);
    }

    public void updateHabit(HabitModel habitModel) {
        repository.updateHabit(habitModel);
    }

    public LiveData<HabitModel> getLiveHabitById(int habitId) {
        return repository.getByIdLive(habitId);
    }

    public LiveData<HabitWithCategory> getHabitWithCategory(int habitId) {
        return repository.getHabitWithCategory(habitId);
    }

    public void archiveHabit(long habitId) {
        repository.archiveHabit(habitId);
    }

    public void restoreHabit(int habitId) {
        repository.restoreHabit(habitId);
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


    public void setCategory(Long id) {
        selectedCategoryId.setValue(id);
    }


    public void setSortType(SortType type) {
        selectedSortType.setValue(type.name());
    }

    public void toggleDirection() {
        isAscending.setValue(Boolean.FALSE.equals(isAscending.getValue()));
    }

    public LiveData<Boolean> getIsAscending() {
        return isAscending;
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

    public void completeHabit(int habitId) {
        LiveData<MarkDoneResult> source = repository.markHabitAsDone(habitId);
        // observing the one-time result from the repository
        source.observeForever(new Observer<MarkDoneResult>() {
            @Override
            public void onChanged(MarkDoneResult result) {
                markDoneEvent.setValue(result);
                // clean up to prevent leaks as this is a one-time event
                source.removeObserver(this);
            }
        });
    }

    public LiveData<MarkDoneResult> getMarkDoneEvent() {
        return markDoneEvent;
    }


}
