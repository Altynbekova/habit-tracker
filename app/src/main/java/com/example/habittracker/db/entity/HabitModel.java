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
    @NonNull
    @ColumnInfo(defaultValue = "'DAILY'")
    public FrequencyType frequencyType;//todo delete
    @ColumnInfo(index = true)
    public Long categoryId;
    public boolean isArchived = false;
    @NonNull
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    public LocalDateTime createdAt;
    public boolean isCompleted = false;
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

    @NonNull
    public FrequencyType getFrequencyType() {
        return frequencyType;
    }

    public void setFrequencyType(@NonNull FrequencyType frequencyType) {
        this.frequencyType = frequencyType;
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
}
