package com.example.habittracker.db.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class HabitWithCompletion {
    @Embedded
    private HabitModel habit;

    @Relation(parentColumn = "id", entityColumn = "habitId")
    private HabitCompletion completion;

    public HabitModel getHabit() {
        return habit;
    }

    public void setHabit(HabitModel habit) {
        this.habit = habit;
    }

    public HabitCompletion getCompletion() {
        return completion;
    }

    public void setCompletion(HabitCompletion completion) {
        this.completion = completion;
    }
}
