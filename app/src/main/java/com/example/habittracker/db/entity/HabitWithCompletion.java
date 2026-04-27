package com.example.habittracker.db.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class HabitWithCompletion {
    @Embedded
    public HabitModel habit;

    @Relation(parentColumn = "id", entityColumn = "habitId")
    public HabitCompletion completion;
}
