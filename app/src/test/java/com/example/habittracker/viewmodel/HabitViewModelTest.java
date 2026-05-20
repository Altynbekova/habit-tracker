package com.example.habittracker.viewmodel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.habittracker.db.entity.HabitModel;
import com.example.habittracker.db.entity.MarkDoneResult;
import com.example.habittracker.repository.AppRepo;
import com.example.habittracker.ui.SortType;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class HabitViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application application;

    @Mock
    private AppRepo repository;

    @Mock
    private Observer<List<HabitModel>> habitsObserver;

    @Mock
    private Observer<MarkDoneResult> markDoneObserver;

    private HabitViewModel viewModel;
    private MutableLiveData<List<HabitModel>> mockActiveHabits;
    private MutableLiveData<List<HabitModel>> mockFilteredHabits;

    @Before
    public void setUp() {
        mockActiveHabits = new MutableLiveData<>();
        mockFilteredHabits = new MutableLiveData<>();

        when(repository.getAllActiveHabits()).thenReturn(mockActiveHabits);
        when(repository.getFilteredSorted(anyLong(), anyString(), anyInt())).thenReturn(mockFilteredHabits);

        viewModel = new HabitViewModel(application, repository);
    }

    @Test
    public void addHabit_callsRepository() {
        HabitModel habit = new HabitModel();
        viewModel.addHabit(habit);
        verify(repository).insertHabit(habit);
    }

    @Test
    public void updateHabit_callsRepository() {
        HabitModel habit = new HabitModel();
        viewModel.updateHabit(habit);
        verify(repository).updateHabit(habit);
    }

    @Test
    public void archiveHabit_callsRepository() {
        viewModel.archiveHabit(42L);
        verify(repository).archiveHabit(42L);
    }

    @Test
    public void toggleDirection_changesIsAscendingValue() {
        assertTrue(viewModel.getIsAscending().getValue());
        viewModel.toggleDirection();
        assertFalse(viewModel.getIsAscending().getValue());
    }

    @Test
    public void filterMerger_triggersRepositoryFilter_whenCategoryChanges() {
        // Arrange
        viewModel.filteredHabits.observeForever(habitsObserver);
        List<HabitModel> expectedHabits = new ArrayList<>();
        mockFilteredHabits.setValue(expectedHabits);

        viewModel.setCategory(5L);

        verify(repository).getFilteredSorted(5L, "NAME", 1);
        verify(habitsObserver).onChanged(expectedHabits);

        viewModel.filteredHabits.removeObserver(habitsObserver);
    }

    @Test
    public void filterMerger_triggersRepositoryFilter_whenSortTypeChanges() {
        viewModel.filteredHabits.observeForever(habitsObserver);
        clearInvocations(repository);

        viewModel.setSortType(SortType.NAME);
        verify(repository, times(1)).getFilteredSorted(null, "NAME", 1);

        viewModel.filteredHabits.removeObserver(habitsObserver);
    }

    @Test
    public void completeHabit_observesRepositoryAndTriggersEvent() {
        int habitId = 10;
        MutableLiveData<MarkDoneResult> repoResultLiveData = new MutableLiveData<>();
        MarkDoneResult mockResult = MarkDoneResult.SUCCESS;

        when(repository.markHabitAsDone(habitId)).thenReturn(repoResultLiveData);
        viewModel.getMarkDoneEvent().observeForever(markDoneObserver);

        viewModel.completeHabit(habitId);
        repoResultLiveData.setValue(mockResult);

        verify(repository, times(1)).markHabitAsDone(habitId);
        verify(markDoneObserver, times(1)).onChanged(mockResult);

        viewModel.getMarkDoneEvent().removeObserver(markDoneObserver);
    }
}
