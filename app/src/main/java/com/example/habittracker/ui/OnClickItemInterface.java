package com.example.habittracker.ui;

import com.example.habittracker.db.HabitModel;

public interface OnClickItemInterface {
    /**
     *
     * @param habitModel model of habit
     * @param toEdit true - to edit habit, false - to delete habit
     */
    void onClickItem(HabitModel habitModel, boolean toEdit);
    void onCompleteItem(HabitModel habitModel);
}
