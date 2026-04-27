package com.example.habittracker.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalTime;

@Entity(tableName = "reminders",
        foreignKeys = @ForeignKey(entity = HabitModel.class,
                parentColumns = "id",
                childColumns = "habitId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = {"habitId"}, unique = true)})
public class Reminder {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private int habitId;

    @NonNull
    private LocalTime time;

    private boolean enabled = true;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getHabitId() {
        return habitId;
    }

    public void setHabitId(int habitId) {
        this.habitId = habitId;
    }

    @NonNull
    public LocalTime getTime() {
        return time;
    }

    public void setTime(@NonNull LocalTime time) {
        this.time = time;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
