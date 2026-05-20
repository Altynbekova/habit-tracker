package com.example.habittracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.habittracker.db.entity.HabitCompletion;

import java.time.LocalDate;
import java.util.List;

@Dao
public interface HabitCompletionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(HabitCompletion completion);

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY completionDate DESC")
    LiveData<List<HabitCompletion>> getCompletionsForHabit(long habitId);
}
