package com.example.habittracker.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

@Entity(tableName = "habits",
        foreignKeys = @ForeignKey(entity = Category.class,
                parentColumns = "id",
                childColumns = "categoryId",
                onDelete = ForeignKey.SET_NULL))
public class HabitModel {
    @ColumnInfo(index = true)
    public Long categoryId;
    private boolean isArchived = false;
    @NonNull
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    private boolean isCompleted = false;
    @PrimaryKey(autoGenerate = true)
    private int id;
    @NonNull
    @ColumnInfo(index = true)
    private String name;
    private String description;
    private String color;//todo delete
    private int targetDays;
    private int currentStreak;

    public HabitModel() {
    }

    public HabitModel(String name, String description, String color, int targetDays) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.targetDays = targetDays;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getTargetDays() {
        return targetDays;
    }

    public void setTargetDays(int targetDays) {
        this.targetDays = targetDays;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    @NonNull
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NonNull LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
