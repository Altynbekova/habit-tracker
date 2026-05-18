package com.example.habittracker.db.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class HabitWithCategory {
    @Embedded
    private HabitModel habit;
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    private Category category;

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
}
