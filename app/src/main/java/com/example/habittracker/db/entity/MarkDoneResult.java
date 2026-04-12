package com.example.habittracker.db.entity;

public enum MarkDoneResult {
    SUCCESS,        // streak incremented
    ALREADY_DONE,   // no change (already done today)
    GOAL_REACHED    // targetDays hit, habit is completed
}
