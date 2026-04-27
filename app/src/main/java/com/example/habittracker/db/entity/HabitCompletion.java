package com.example.habittracker.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(tableName = "habit_completions",
        primaryKeys = {"habitId", "completionDate"},
        foreignKeys = @ForeignKey(entity = HabitModel.class,
                parentColumns = "id",
                childColumns = "habitId",
                onDelete = ForeignKey.CASCADE))
public class HabitCompletion {
    @ColumnInfo(index = true)
    private int habitId;

    @NonNull
    private LocalDate completionDate;

    @NonNull
    private CompletionStatus status;

    private LocalDateTime completedAt;

    public int getHabitId() {
        return habitId;
    }

    public void setHabitId(int habitId) {
        this.habitId = habitId;
    }

    @NonNull
    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(@NonNull LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    @NonNull
    public CompletionStatus getStatus() {
        return status;
    }

    public void setStatus(@NonNull CompletionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
