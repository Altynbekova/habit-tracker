package com.example.habittracker.ui;

import com.example.habittracker.db.entity.HabitModel;

public interface AddHabitActivityHandler {
    void onIncrease();
    void onDecrease();
    void onSave(HabitModel habitModel);
}
