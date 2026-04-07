package com.example.habittracker.db.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class HabitWithCategory {
    @Embedded
    public HabitModel habit;
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    public Category category;
}
