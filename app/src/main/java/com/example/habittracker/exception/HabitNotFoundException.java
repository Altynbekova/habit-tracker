package com.example.habittracker.exception;

import androidx.annotation.Nullable;

public class HabitNotFoundException extends Throwable{
    public HabitNotFoundException() {
    }

    public HabitNotFoundException(@Nullable String message) {
        super(message);
    }

    public HabitNotFoundException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public HabitNotFoundException(@Nullable String message, @Nullable Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
