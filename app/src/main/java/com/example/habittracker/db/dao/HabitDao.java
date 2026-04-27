package com.example.habittracker.db.dao;

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
import com.example.habittracker.db.entity.HabitWithCompletion;
import com.example.habittracker.db.entity.HabitWithDetails;
import com.example.habittracker.db.entity.MarkDoneResult;

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

    /**
     * Finds habit with last completion
     *
     * @param habitId id of habit
     * @return habit with completion info
     */
    @Transaction
    @Query("select * from habits, habit_completions where id=:habitId order by completionDate desc limit 1")
    public abstract LiveData<HabitWithCompletion> getHabitWithCompletion(int habitId);

    /**
     * Finds habit with last completion
     *
     * @param habitId id of habit
     * @return habit with completion info
     */
    @Transaction
    @Query("select * from habits, habit_completions where id=:habitId order by completionDate desc limit 1")
    public abstract HabitWithCompletion getHabitWithCompletionSync(int habitId);

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
        long id = insertCompletion(completion);

        HabitModel habit = getHabitById(completion.getHabitId());
        if (habit != null) {
            int currentCompletions = getCompletionCount(habit.getId());
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
    public MarkDoneResult markAsDoneAndCalculateStreak(int habitId, LocalDateTime dateTime)/* throws HabitNotFoundException, InvalidAction */ {
        HabitWithCompletion habitWithCompletion = getHabitWithCompletionSync(habitId);
        HabitModel habit = habitWithCompletion.habit;
        LocalDate date = dateTime.toLocalDate();

//        if (habit == null) throw new HabitNotFoundException("Habit not found. Id="+ habitId);
        if (habit == null || habit.isCompleted || habitWithCompletion.completion != null &&
                date.isEqual(habitWithCompletion.completion.getCompletionDate())) {
            return MarkDoneResult.ALREADY_DONE;
        }

        HabitCompletion completion = new HabitCompletion();
        completion.setHabitId(habitId);
        completion.setCompletionDate(date);
        completion.setCompletedAt(dateTime);

        String yesterday = date.minusDays(1).toString();
        boolean wasCompletedYesterday = isHabitCompletedOnDate(habitId, yesterday) > 0;
        if(wasCompletedYesterday){
            habit.setCurrentStreak(habit.getCurrentStreak() + 1);
            completion.setStatus(CompletionStatus.PARTIAL);
        } else {
            habit.setCurrentStreak(1);
            completion.setStatus(CompletionStatus.SKIPPED);
        }

        MarkDoneResult result = MarkDoneResult.SUCCESS;
        if (habit.getCurrentStreak() >= habit.getTargetDays()) {
            habit.isCompleted = true;
            completion.setStatus(CompletionStatus.COMPLETED);
            result = MarkDoneResult.GOAL_REACHED;
        }

        insertCompletion(completion);
        updateHabit(habit);

        return result;
    }
}
