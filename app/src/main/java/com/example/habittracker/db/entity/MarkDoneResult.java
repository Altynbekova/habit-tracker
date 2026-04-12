package com.example.habittracker.db.entity;

public enum MarkDoneResult {
    SUCCESS,        // Streak incremented
    ALREADY_DONE,   // No change (already done today)
    GOAL_REACHED    // TargetDays hit, habit is now completed
}
