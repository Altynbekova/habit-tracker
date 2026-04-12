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
    /*@PrimaryKey(autoGenerate = true)
    private Long id;*/
    @ColumnInfo(index = true)
    public int habitId;

    @NonNull
//    @ColumnInfo(index = true)
    public LocalDate completionDate;

    @NonNull
    public CompletionStatus status; // COMPLETED, PARTIAL, SKIPPED

    public float partialValue; // 0–100%

    public LocalDateTime completedAt;
    private String note;

    /*public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }*/

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
