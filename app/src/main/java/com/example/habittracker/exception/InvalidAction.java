package com.example.habittracker.exception;

import androidx.annotation.Nullable;

public class InvalidAction extends Throwable{

    public InvalidAction() {
    }

    public InvalidAction(@Nullable String message) {
        super(message);
    }

    public InvalidAction(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public InvalidAction(@Nullable String message, @Nullable Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
