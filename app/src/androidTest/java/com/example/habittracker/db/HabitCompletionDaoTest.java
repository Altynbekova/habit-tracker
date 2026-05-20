package com.example.habittracker.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.habittracker.Util;
import com.example.habittracker.db.dao.HabitCompletionDao;
import com.example.habittracker.db.entity.CompletionStatus;
import com.example.habittracker.db.entity.HabitCompletion;
import com.example.habittracker.db.entity.HabitModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class HabitCompletionDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase db;
    private HabitCompletionDao completionDao;

    @Before
    public void createDb() {
        db = Room.inMemoryDatabaseBuilder(
                        ApplicationProvider.getApplicationContext(),
                        AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        completionDao = db.habitCompletionDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertAndGetCompletions_returnsOrderedByDateDesc() throws InterruptedException {
        int habitId = createAndInsertHabit("Read Books");
        LocalDate today = LocalDate.now();
        HabitCompletion olderCompletion = new HabitCompletion();
        olderCompletion.setHabitId(habitId);
        olderCompletion.setStatus(CompletionStatus.PARTIAL);
        olderCompletion.setCompletionDate(today.minusDays(1)); // Вчера

        HabitCompletion newerCompletion = new HabitCompletion();
        newerCompletion.setHabitId(habitId);
        newerCompletion.setStatus(CompletionStatus.PARTIAL);
        newerCompletion.setCompletionDate(today);

        completionDao.insert(olderCompletion);
        completionDao.insert(newerCompletion);

        LiveData<List<HabitCompletion>> liveData = completionDao.getCompletionsForHabit(habitId);
        List<HabitCompletion> history = Util.getValueOrAwait(liveData);

        assertNotNull(history);
        assertEquals(2, history.size());
        assertEquals(today, history.get(0).getCompletionDate());
        assertEquals(today.minusDays(1), history.get(1).getCompletionDate());
    }

    @Test
    public void insertDuplicate_ignoresConflict() {
        int habitId = createAndInsertHabit("Drink Water");

        HabitCompletion completion = new HabitCompletion();
        completion.setHabitId(habitId);
        completion.setStatus(CompletionStatus.PARTIAL);
        completion.setCompletionDate(LocalDate.now());

        long firstInsertId = completionDao.insert(completion);
        long secondInsertId = completionDao.insert(completion);

        assertTrue(firstInsertId != -1);
        assertEquals(-1, secondInsertId);
    }

    private int createAndInsertHabit(String name) {
        HabitModel habit = new HabitModel();
        habit.setName(name);
        habit.setArchived(false);
        db.habitDao().insert(habit);

        return db.habitDao().getAllActiveHabitsSync().stream()
                .filter(h -> name.equals(h.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Не удалось создать привычку для теста: " + name))
                .getId();
    }
}
