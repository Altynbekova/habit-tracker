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
    public long id;

    public int habitId;

    @NonNull
    public LocalTime time;

    public boolean enabled = true;
}
