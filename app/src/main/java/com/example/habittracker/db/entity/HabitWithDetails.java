package com.example.habittracker.db.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class HabitWithDetails {
    @Embedded
    private HabitModel habit;

    @Relation(
            parentColumn = "categoryId",
            entityColumn = "id"
    )
    private Category category;

    @Relation(
            parentColumn = "id",
            entityColumn = "habitId"
    )
    private Reminder reminder;

    public HabitModel getHabit() {
        return habit;
    }

    public void setHabit(HabitModel habit) {
        this.habit = habit;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Reminder getReminder() {
        return reminder;
    }

    public void setReminder(Reminder reminder) {
        this.reminder = reminder;
    }
}
