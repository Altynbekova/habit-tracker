package com.example.habittracker.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HabitDao {
    @Query("select * from habits")
    LiveData<List<HabitModel>> getAllHabitsLive();

    @Query("select * from habits")
    List<HabitModel> getAllHabitsFuture();

    @Query("select * from habits where id=:id")
    HabitModel getHabit(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHabit(HabitModel habit);

    @Update
    void updateHabit(HabitModel habit);

    @Delete
    void deleteHabit(HabitModel habit);

    @Query("delete from habits where id = :id")
    void deleteHabitById(int id);
}
