package com.example.habittracker.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity(tableName = "habits",
        foreignKeys = @ForeignKey(entity = Category.class,
                parentColumns = "id",
                childColumns = "categoryId",
                onDelete = ForeignKey.SET_NULL))
public class HabitModel {
    @NonNull
    @ColumnInfo(defaultValue = "'DAILY'")
    public FrequencyType frequencyType;
    @ColumnInfo(index = true)
    public Long categoryId;
    public String icon;
    public boolean isArchived = false;
    @NonNull
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    public LocalDateTime createdAt;
    public LocalTime reminderTime;
    public boolean isCompleted = false;
    @PrimaryKey(autoGenerate = true)
    private int id;
    @NonNull
    @ColumnInfo(index = true)
    private String name;
    private String description;
    private String color;
    private int targetDays;
    private int currentStreak;
    private String lastCompletedDate = "";
    private String notificationTime = "";

    public HabitModel() {
    }

    public HabitModel(String name, String description, String color, int targetDays, String notificationTime) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.targetDays = targetDays;
        this.notificationTime = notificationTime;
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

    public String getLastCompletedDate() {
        return lastCompletedDate;
    }

    public void setLastCompletedDate(String lastCompletedDate) {
        this.lastCompletedDate = lastCompletedDate;
    }

    public String getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(String notificationTime) {
        this.notificationTime = notificationTime;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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

    public LocalTime getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(LocalTime reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isTodayCompleted() {
        if (lastCompletedDate == null || lastCompletedDate.isEmpty())
            return false;
        return LocalDate.now().isEqual(LocalDate.parse(lastCompletedDate));
    }
}
