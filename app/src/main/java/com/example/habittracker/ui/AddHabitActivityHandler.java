package com.example.habittracker.ui;

import com.example.habittracker.db.HabitModel;

public interface AddHabitActivityHandler {
    void onIncrease();
    void onDecrease();
    void onSave(HabitModel habitModel);
}
