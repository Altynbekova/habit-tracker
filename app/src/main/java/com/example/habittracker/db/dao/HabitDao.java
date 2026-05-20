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

import java.time.LocalDateTime;
import java.util.List;

@Dao
public abstract class HabitDao {
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

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY name ASC")
    public abstract List<HabitModel> getAllActiveHabitsSync();

    @Query("UPDATE habits SET isArchived = 0 WHERE id = :id")
    public abstract void restoreHabit(long id);

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
    @Query("select * from habits left join habit_completions on habits.id=habitId where habits.id=:habitId " +
            "order by completionDate desc limit 1")
    public abstract HabitWithCompletion getHabitWithCompletionSync(int habitId);

    @Query("SELECT * FROM habits WHERE isArchived = 0 " +
            "AND (:catId IS NULL OR categoryId = :catId) " +
            "ORDER BY " +
            "CASE WHEN :sortType = 'NAME' AND :isAsc = 1 THEN lower(name) END ASC, " +
            "CASE WHEN :sortType = 'NAME' AND :isAsc = 0 THEN lower(name) END DESC, " +
            "CASE WHEN :sortType = 'DATE' AND :isAsc = 1 THEN createdAt END ASC, " +
            "CASE WHEN :sortType = 'DATE' AND :isAsc = 0 THEN createdAt END DESC")
    public abstract LiveData<List<HabitModel>> getFilteredSorted(Long catId, String sortType, int isAsc);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insertCompletion(HabitCompletion completion);

    @Update
    public abstract void updateHabit(HabitModel habit);

    @Transaction
    @Query("SELECT * FROM habits WHERE id = :habitId")
    public abstract LiveData<HabitWithDetails> getHabitWithDetails(int habitId);

    @Transaction
    @Query("SELECT * FROM habits WHERE id = :habitId")
    public abstract HabitWithDetails getHabitWithDetailsSync(int habitId);


    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId AND completionDate = :date")
    public abstract int isHabitDoneOnDate(int habitId, String date);

    @Transaction
    public MarkDoneResult markAsDoneAndCalculateStreak(int habitId, LocalDateTime dateTime) {
        HabitWithCompletion habitWithCompletion = getHabitWithCompletionSync(habitId);
        HabitModel habit = habitWithCompletion.getHabit();

        if (habit.isCompleted()) {
            return MarkDoneResult.GOAL_REACHED;
        }
        if (habitWithCompletion.getCompletion() != null &&
                dateTime.toLocalDate().isEqual(habitWithCompletion.getCompletion().getCompletionDate())) {
            return MarkDoneResult.ALREADY_DONE;
        }

        HabitCompletion completion = new HabitCompletion();
        completion.setHabitId(habitId);
        completion.setCompletionDate(dateTime.toLocalDate());
        completion.setCompletedAt(dateTime);

        MarkDoneResult result;
        String yesterday = dateTime.toLocalDate().minusDays(1).toString();
        boolean doneYesterday = isHabitDoneOnDate(habitId, yesterday) > 0;
        if (doneYesterday) {
            habit.setCurrentStreak(habit.getCurrentStreak() + 1);
        } else {
            habit.setCurrentStreak(1);
        }

        if (habit.getCurrentStreak() < habit.getTargetDays()) {
            completion.setStatus(CompletionStatus.PARTIAL);
            result = MarkDoneResult.SUCCESS;
        } else {
            habit.setCompleted(true);
            completion.setStatus(CompletionStatus.COMPLETED);
            result = MarkDoneResult.GOAL_REACHED;
        }

        insertCompletion(completion);
        updateHabit(habit);

        return result;
    }
}
