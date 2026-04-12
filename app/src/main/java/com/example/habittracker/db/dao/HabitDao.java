package com.example.habittracker.db.dao;

import android.content.res.Resources;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.habittracker.db.entity.CompletionStatus;
import com.example.habittracker.db.entity.HabitCompletion;
import com.example.habittracker.db.entity.HabitModel;
import com.example.habittracker.db.entity.HabitWithCategory;
import com.example.habittracker.db.entity.HabitWithDetails;
import com.example.habittracker.db.entity.MarkDoneResult;
import com.example.habittracker.exception.HabitNotFoundException;
import com.example.habittracker.exception.InvalidAction;
import com.example.habittracker.ui.SortType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Dao
public abstract class HabitDao {
    @Query("select * from habits")
    public abstract LiveData<List<HabitModel>> getAllHabitsLive();

    @Query("select * from habits")
    public abstract List<HabitModel> getAllHabitsFuture();

    @Query("select * from habits where id=:id")
    public abstract LiveData<HabitModel> getHabitLive(int id);

    @Query("select * from habits where id=:id")
    public abstract HabitModel getHabit(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(HabitModel habit);

    @Update
    public abstract void update(HabitModel habit);

    @Delete
    public abstract void delete(HabitModel habit);

    @Query("delete from habits where id = :id")
    public abstract void deleteById(int id);

    @Query("update habits set notificationTime=:time where id=:id")
    public abstract void updateTime(int id, String time);

    @Query("UPDATE habits SET isArchived = 1 WHERE id = :id")
    public abstract void archiveHabit(long id);

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY name ASC")
    public abstract LiveData<List<HabitModel>> getAllActiveHabits();

    @Query("UPDATE habits SET isArchived = 0 WHERE id = :id")
    public abstract void restoreHabit(long id);

    @Transaction
    @Query("SELECT * FROM habits WHERE isArchived = 0")
    public abstract LiveData<List<HabitWithCategory>> getAllHabitsWithCategories();
    @Transaction
    @Query("SELECT * FROM habits WHERE id=:habitId and isArchived = 0")
    public abstract LiveData<HabitWithCategory> getHabitWithCategory(int habitId);

    @Query("SELECT * FROM habits WHERE categoryId=:categoryId AND isArchived = 0")
    public abstract LiveData<List<HabitModel>> getHabitsWithCategory(long categoryId);

    @Query("SELECT * FROM habits WHERE isArchived = 0 and categoryId=:categoryId ORDER BY name ASC")
    public abstract LiveData<List<HabitModel>> getFilteredSortedByName(long categoryId);

    @Query("SELECT * FROM habits WHERE isArchived = 0 and categoryId=:categoryId ORDER BY createdAt DESC")
    public abstract LiveData<List<HabitModel>> getFilteredSortedByDate(long categoryId);

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY name ASC")
    public abstract LiveData<List<HabitModel>> getAllSortedByName();

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY createdAt DESC")
    public abstract LiveData<List<HabitModel>> getAllSortedByDate();

    // Simplified example for streak sorting (based on total completion count)
    @Query("SELECT h.* FROM habits h LEFT JOIN habit_completions c ON h.id = c.habitId " +
            "WHERE h.isArchived = 0 GROUP BY h.id ORDER BY COUNT(c.completionDate) DESC")
    public abstract LiveData<List<HabitModel>> getAllSortedByStreak();

//    LiveData<List<HabitModel>> getFilteredAndSortedHabits(Long categoryId, SortType sortType);

    @Query("SELECT * FROM habits WHERE isArchived = 0 " +
            "AND (:catId IS NULL OR categoryId = :catId) " +
            "ORDER BY " +
            // Сортировка по имени
            "CASE WHEN :sortType = 'NAME' AND :isAsc = 1 THEN lower(name) END ASC, " +
            "CASE WHEN :sortType = 'NAME' AND :isAsc = 0 THEN lower(name) END DESC, " +
            // Сортировка по дате
            "CASE WHEN :sortType = 'DATE' AND :isAsc = 1 THEN createdAt END ASC, " +
            "CASE WHEN :sortType = 'DATE' AND :isAsc = 0 THEN createdAt END DESC")
    public abstract LiveData<List<HabitModel>> getFilteredSorted(Long catId, String sortType, int isAsc);


    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insertCompletion(HabitCompletion completion);

    @Query("SELECT * FROM habits WHERE id = :id")
    public abstract HabitModel getHabitById(int id);

    @Update
    public abstract void updateHabit(HabitModel habit);

    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId")
    public abstract int getCompletionCount(int habitId);

    @Transaction
    public long insertAndCheckGoal(HabitCompletion completion) {
        // 1. Insert completion
        long id = insertCompletion(completion);

        // 2. Fetch the habit
        HabitModel habit = getHabitById(completion.habitId);

        if (habit != null) {
            // 3. Count total completions for this habit
            int currentCompletions = getCompletionCount(habit.getId());

            // 4. Update 'isCompleted' if target is reached
            if (currentCompletions >= habit.getTargetDays()) {
                habit.isCompleted = true;
                updateHabit(habit);
            }
        }
        return id;
    }

    @Transaction
    @Query("SELECT * FROM habits WHERE id = :habitId")
    public abstract LiveData<HabitWithDetails> getHabitWithDetails(int habitId);

    @Transaction
    @Query("SELECT * FROM habits WHERE id = :habitId")
    public abstract HabitWithDetails getHabitWithDetailsSync(int habitId);



    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId AND completionDate = :date AND status = 'COMPLETED'")
    public abstract int isHabitCompletedOnDate(int habitId, String date);

    @Transaction
    public MarkDoneResult markAsDoneAndCalculateStreak(int habitId, LocalDate date)/* throws HabitNotFoundException, InvalidAction */{
        HabitModel habit = getHabitById(habitId);
//        if (habit == null) throw new HabitNotFoundException("Habit not found. Id="+ habitId);
        if (habit == null || habit.isCompleted || date.toString().equals(habit.getLastCompletedDate())) {
            return MarkDoneResult.ALREADY_DONE;
        }

        // 1. Check if already done today
        /*String todayStr = date.toString();
        if (todayStr.equals(habit.getLastCompletedDate())) {
            return false; // Tells the Repository/VM that nothing changed
//            throw new InvalidAction("Cannot mark habit as done as it has already been done today");
        }*/

        // 2. Insert the completion record
        HabitCompletion completion = new HabitCompletion();
        completion.habitId = habitId;
        completion.completionDate = date;
        completion.status = CompletionStatus.COMPLETED;
        completion.completedAt = LocalDateTime.now();
        insertCompletion(completion);

        // 3. Calculate Streak
        String yesterday = date.minusDays(1).toString();
        boolean wasCompletedYesterday = isHabitCompletedOnDate(habitId, yesterday) > 0;

        habit.setCurrentStreak(wasCompletedYesterday ? habit.getCurrentStreak() + 1 : 1);
        habit.setLastCompletedDate(date.toString());

        // 4. CHECK TARGET DAYS
        MarkDoneResult result = MarkDoneResult.SUCCESS;
        if (habit.getCurrentStreak() >= habit.getTargetDays()) {
            habit.isCompleted = true;
            result = MarkDoneResult.GOAL_REACHED;
        }

        // 5. Finalize update
//        habit.setLastCompletedDate(todayStr);
        updateHabit(habit);

        return result; // Successfully updated
    }

}
