package com.example.habittracker.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.habittracker.db.dao.CategoryDao;
import com.example.habittracker.db.dao.HabitDao;
import com.example.habittracker.db.entity.Category;
import com.example.habittracker.db.entity.HabitModel;
import com.example.habittracker.db.entity.MarkDoneResult;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class HabitDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase db;
    private HabitDao habitDao;
    private CategoryDao categoryDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();

        habitDao = db.habitDao();
        categoryDao = db.categoryDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertAndGetHabit() {
        HabitModel habit = new HabitModel();
        habit.setId(1);
        habit.setName("Пить воду");
        habit.setArchived(false);

        habitDao.insert(habit);
        HabitModel loaded = habitDao.getHabit(1);

        assertNotNull(loaded);
        assertEquals(habit.getName(), loaded.getName());
    }

    @Test
    public void getFilteredSorted_filtersByCategoryCorrectly() throws Exception {
        Category sports = new Category("Спорт", "");
        Category health = new Category("Здоровье", "");
        categoryDao.insert(sports);
        categoryDao.insert(health);

        HabitModel habit1 = new HabitModel();
        habit1.setId(1);
        habit1.setName("Бег");
        habit1.setCategoryId(1L); // Спорт

        HabitModel habit2 = new HabitModel();
        habit2.setId(2);
        habit2.setName("Медитация");
        habit2.setCategoryId(2L); // Здоровье

        habitDao.insert(habit1);
        habitDao.insert(habit2);

        LiveData<List<HabitModel>> liveData = habitDao.getFilteredSorted(1L, "NAME", 1);
        List<HabitModel> filteredList = getValueOrAwait(liveData);

        assertEquals(1, filteredList.size());
        assertEquals("Бег", filteredList.get(0).getName());
    }

    @Test
    public void markAsDoneAndCalculateStreak_returnsValidResult() {
        HabitModel habit = new HabitModel();
        habit.setId(5);
        habit.setName("Чтение книги");
        habitDao.insert(habit);

        MarkDoneResult result = habitDao.markAsDoneAndCalculateStreak(5, LocalDateTime.now());

        assertNotNull(result);
        assertEquals(MarkDoneResult.GOAL_REACHED, result);
    }

    /**
     * Вспомогательный метод для получения данных из LiveData внутри тестов.
     * Так как LiveData является асинхронным компонентом,
     * CountDownLatch блокирует поток выполнения теста до тех пор,
     * пока LiveData не вернет значение.
     */
    private <T> T getValueOrAwait(final LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);

        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(T o) {
                data[0] = o;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };

        Runnable runnable = () -> liveData.observeForever(observer);
        runnable.run();
        if (!latch.await(2, TimeUnit.SECONDS)) {
            throw new RuntimeException("LiveData value was never set.");
        }

        return (T) data[0];
    }
}
