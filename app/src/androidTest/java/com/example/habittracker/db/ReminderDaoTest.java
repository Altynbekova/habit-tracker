package com.example.habittracker.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.habittracker.Util;
import com.example.habittracker.db.dao.ReminderDao;
import com.example.habittracker.db.entity.HabitModel;
import com.example.habittracker.db.entity.Reminder;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalTime;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ReminderDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase db;
    private ReminderDao reminderDao;

    @Before
    public void createDb() {
        db = Room.inMemoryDatabaseBuilder(
                        ApplicationProvider.getApplicationContext(),
                        AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        reminderDao = db.reminderDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void insertOrUpdate_savesAndReplacesCorrectly() throws InterruptedException {
        int habitId = createAndInsertHabit("Exercise");

        Reminder reminder = new Reminder();
        reminder.setHabitId(habitId);
        reminder.setTime(LocalTime.of(8, 0));
        reminder.setEnabled(true);

        reminderDao.insertOrUpdate(reminder);

        Reminder loaded = Util.getValueOrAwait(reminderDao.getReminderByHabitId(habitId));
        assertNotNull(loaded);
        assertEquals(LocalTime.of(8, 0), loaded.getTime());

        reminder.setTime(LocalTime.of(9, 30));
        reminderDao.insertOrUpdate(reminder);

        Reminder updated = Util.getValueOrAwait(reminderDao.getReminderByHabitId(habitId));
        assertEquals(LocalTime.of(9, 30), updated.getTime());
    }

    @Test
    public void updateStatusByHabitId_modifiesEnabledFlag() {
        int habitId = createAndInsertHabit("Drink Water");

        Reminder reminder = new Reminder();
        reminder.setHabitId(habitId);
        reminder.setTime(LocalTime.of(12, 0));
        reminder.setEnabled(true);
        reminderDao.insert(reminder);

        reminderDao.updateStatusByHabitId(habitId, false);

        Reminder loaded = reminderDao.getReminderForHabit(habitId);
        assertNotNull(loaded);
        assertFalse(loaded.isEnabled());
    }

    @Test
    public void updateReminderStatus_modifiesEnabledFlagById() {
        int habitId = createAndInsertHabit("Reading");

        Reminder reminder = new Reminder();
        reminder.setHabitId(habitId);
        reminder.setTime(LocalTime.of(15, 0));
        reminder.setEnabled(false);
        long generatedReminderId = reminderDao.insert(reminder);

        reminderDao.updateReminderStatus(generatedReminderId, true);

        Reminder loaded = reminderDao.getReminderForHabit(habitId);
        assertNotNull(loaded);
        assertTrue(loaded.isEnabled());
    }

    @Test
    public void getAllEnabledRemindersSync_returnsOnlyEnabledReminders() {
        int habitId1 = createAndInsertHabit("Gym");
        int habitId2 = createAndInsertHabit("Meditation");
        int habitId3 = createAndInsertHabit("Coding");
        // Arrange
        Reminder r1 = new Reminder();
        r1.setHabitId(habitId1);
        r1.setEnabled(true);
        r1.setTime(LocalTime.of(9, 0));
        Reminder r2 = new Reminder();
        r2.setHabitId(habitId2);
        r2.setEnabled(false);
        r2.setTime(LocalTime.of(10, 0));
        Reminder r3 = new Reminder();
        r3.setHabitId(habitId3);
        r3.setEnabled(true);
        r3.setTime(LocalTime.of(11, 0));

        reminderDao.insert(r1);
        reminderDao.insert(r2);
        reminderDao.insert(r3);

        List<Reminder> enabledReminders = reminderDao.getAllEnabledRemindersSync();

        assertEquals(2, enabledReminders.size());
        for (Reminder r : enabledReminders) {
            assertTrue(r.isEnabled());
        }
    }

    @Test
    public void insertAndGetReminder_savesTimeCorrectly() throws InterruptedException {
        ReminderDao reminderDao = db.reminderDao();
        int habitId = createAndInsertHabit("Morning Hydration");
        LocalTime reminderTime = LocalTime.of(8, 30); // 08:30 утра

        Reminder reminder = new Reminder();
        reminder.setHabitId(habitId);
        reminder.setTime(reminderTime);
        reminder.setEnabled(true);

        reminderDao.insertOrUpdate(reminder);

        LiveData<Reminder> liveData = reminderDao.getReminderByHabitId(habitId);
        Reminder loaded = Util.getValueOrAwait(liveData);

        assertNotNull(loaded);
        assertEquals(habitId, loaded.getHabitId());
        assertEquals(reminderTime, loaded.getTime());
        assertTrue(loaded.isEnabled());
    }

    private int createAndInsertHabit(String name) {
        HabitModel habit = new HabitModel();
        habit.setName(name);
        db.habitDao().insert(habit);

        // Извлекаем сгенерированный базой данных id
        return db.habitDao().getAllActiveHabitsSync().stream()
                .filter(h -> name.equals(h.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Не удалось создать привычку для теста: " + name))
                .getId();
    }
}
