package com.example.habittracker.ui;

import com.example.habittracker.db.entity.HabitModel;

public interface OnClickItemInterface {
    /**
     * Handles event and edits or deletes habit
     * @param habitModel model of habit
     * @param toEdit true - edit habit, false - delete habit
     */
    void onClickItem(HabitModel habitModel, boolean toEdit);
    void onCompleteItem(HabitModel habitModel);
}
