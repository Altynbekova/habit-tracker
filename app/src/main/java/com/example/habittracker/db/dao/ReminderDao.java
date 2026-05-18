package com.example.habittracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.habittracker.db.entity.Reminder;

import java.util.List;

@Dao
public interface ReminderDao {
    @Insert
    void insert(Reminder reminder);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(Reminder reminder);

    @Query("SELECT * FROM reminders WHERE habitId = :habitId LIMIT 1")
    LiveData<Reminder> getReminderByHabitId(int habitId);

    @Query("SELECT * FROM reminders WHERE habitId = :habitId order by id desc LIMIT 1")
    Reminder getReminderForHabit(int habitId);

    @Query("SELECT * FROM reminders WHERE habitId = :habitId")
    List<Reminder> getRemindersForHabit(int habitId);

    @Query("UPDATE reminders SET enabled = :isEnabled WHERE id = :id")
    void updateReminderStatus(long id, boolean isEnabled);

    @Delete
    void delete(Reminder reminder);

    @Query("UPDATE reminders SET enabled = :isEnabled WHERE habitId = :habitId")
    void updateStatusByHabitId(int habitId, boolean isEnabled);

    // used for the BootReceiver
    @Query("SELECT * FROM reminders WHERE enabled = 1")
    List<Reminder> getAllEnabledRemindersSync();

}

