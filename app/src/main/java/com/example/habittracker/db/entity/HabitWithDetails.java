package com.example.habittracker.db.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class HabitWithDetails {

    // @Embedded flattens the fields of HabitModel into this object
    @Embedded
    public HabitModel habit;

    // 1-to-1: HabitModel.categoryId -> Category.id
    @Relation(
            parentColumn = "categoryId",
            entityColumn = "id"
    )
    public Category category;

    // 1-to-Many: HabitModel.id -> Reminder.habitId
    @Relation(
            parentColumn = "id",
            entityColumn = "habitId"
    )
    public Reminder reminder;
}
