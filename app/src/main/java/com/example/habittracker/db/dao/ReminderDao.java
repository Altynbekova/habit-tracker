package com.example.habittracker.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.habittracker.db.entity.Reminder;

import java.util.List;

@Dao
public interface ReminderDao {
    @Insert
    void insert(Reminder reminder);

    @Query("SELECT * FROM reminders WHERE habitId = :habitId")
    List<Reminder> getRemindersForHabit(long habitId);

    @Query("UPDATE reminders SET enabled = :isEnabled WHERE id = :id")
    void updateReminderStatus(long id, boolean isEnabled);

    @Delete
    void delete(Reminder reminder);
}

