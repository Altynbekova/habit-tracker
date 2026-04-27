package com.example.habittracker.db.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class HabitWithDetails {

    // @Embedded flattens the fields of HabitModel into this object
    @Embedded
    public HabitModel habit;

    @Relation(
            parentColumn = "categoryId",
            entityColumn = "id"
    )
    public Category category;

    @Relation(
            parentColumn = "id",
            entityColumn = "habitId"
    )
    public Reminder reminder;
}
