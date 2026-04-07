package com.example.habittracker.ui;

import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.habittracker.R;
import com.example.habittracker.db.entity.Category;
import com.example.habittracker.db.entity.FrequencyType;
import com.example.habittracker.db.entity.HabitModel;
import com.example.habittracker.util.Utils;
import com.example.habittracker.viewmodel.HabitViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AddHabitSheet extends BottomSheetDialogFragment {

    private HabitViewModel viewModel;
    private int habitId = -1; // -1 means "New Mode"
    private HabitModel existingHabit;

    public static AddHabitSheet newInstance(int id) {
        AddHabitSheet fragment = new AddHabitSheet();
        Bundle args = new Bundle();
        args.putInt("habitId", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_habit, container, false);

        // Connect to the SAME ViewModel as the Fragment
        viewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);

        /*EditText inputName = view.findViewById(R.id.editTextHabitName);
        EditText description = view.findViewById(R.id.editTextDescription);
        EditText targetDays = view.findViewById(R.id.editTextTargetDays);
        Button saveBtn = view.findViewById(R.id.buttonSave);

        saveBtn.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            if (!name.isEmpty()) {
                HabitModel newHabit = new HabitModel();
                newHabit.setName(name);
                newHabit.setDescription(description.getText().toString().trim());
                newHabit.setTargetDays(Integer.parseInt(targetDays.getText().toString().trim()));
                newHabit.createdAt = LocalDateTime.now();
                newHabit.frequencyType = FrequencyType.DAILY; // Default

                viewModel.addHabit(newHabit);
                dismiss(); // Close dialog
            }
        });*/

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);

        EditText inputName = view.findViewById(R.id.editTextHabitName);
        EditText description = view.findViewById(R.id.editTextDescription);
        EditText targetDays = view.findViewById(R.id.editTextTargetDays);
        AutoCompleteTextView categoryDropdown = view.findViewById(R.id.categoryDropdown);
        Button saveBtn = view.findViewById(R.id.buttonSave);
        TextView dialogTitle = view.findViewById(R.id.textViewDialogAddHabitTitle);

        if (getArguments() != null) {
            habitId = getArguments().getInt("habitId", -1);
        }

        // 1. DATA LOADING (EDIT MODE)
        if (habitId != -1) {
            dialogTitle.setText("Редактировать");
            try {
                viewModel.getLiveHabitById(habitId).observe(getViewLifecycleOwner(), habit -> {
                    if (habit != null) {
                        existingHabit = habit;
                        inputName.setText(habit.getName());
                        description.setText(habit.getDescription());
                        targetDays.setText(String.valueOf(habit.getTargetDays()));
                    }
                });
            } catch (Exception e) {
                Log.e("HabitTracker", "Error loading habit", e);
            }
        } else {
            existingHabit = new HabitModel(); // Prepare new habit object
        }

        // 2. CATEGORY SETUP
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            List<String> names = new ArrayList<>();
            String categoryToPreFill = "";

            for (Category c : categories) {
                names.add(c.name);
                if (habitId != -1 && existingHabit != null && c.id == existingHabit.categoryId) {
//                    categoryDropdown.setText(c.name, false);
                    categoryToPreFill = c.name;
                }
            }
            categoryDropdown.setAdapter(new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, names));
            if (!categoryToPreFill.isEmpty()) {
                categoryDropdown.setText(categoryToPreFill, false);
            }

            categoryDropdown.setOnItemClickListener((p, v1, pos, id) ->
                    existingHabit.categoryId = categories.get(pos).id);
        });

        // 3. SAVE LOGIC
        saveBtn.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String daysStr = targetDays.getText().toString().trim();

            if (name.isEmpty() || daysStr.isEmpty()) return;

            existingHabit.setName(name);
            existingHabit.setDescription(description.getText().toString().trim());
            existingHabit.setTargetDays(Integer.parseInt(daysStr));

            if (habitId == -1) {
                existingHabit.createdAt = LocalDateTime.now();
                existingHabit.frequencyType = FrequencyType.DAILY;
                viewModel.addHabit(existingHabit);
            } else {
                viewModel.updateHabit(existingHabit);
            }
            dismiss();
        });
    }
}
