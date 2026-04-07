package com.example.habittracker.viewmodel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.habittracker.db.entity.Category;
import com.example.habittracker.db.entity.CompletionStatus;
import com.example.habittracker.db.entity.HabitCompletion;
import com.example.habittracker.db.entity.HabitModel;
import com.example.habittracker.repository.AppRepo;
import com.example.habittracker.ui.SortType;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public LiveData<HabitModel> getLiveHabitById(int habitId) throws ExecutionException, InterruptedException {
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

    public void restoreHabit(long habitId) {
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
}
