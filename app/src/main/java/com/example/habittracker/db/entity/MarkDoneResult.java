package com.example.habittracker.db.entity;

public enum MarkDoneResult {
    SUCCESS,        // количество выполненных дней увеличилось на 1
    ALREADY_DONE,   // сегодня уже выполнялась
    GOAL_REACHED    // цель достигнута. Цепочка выполнений завершена
}
