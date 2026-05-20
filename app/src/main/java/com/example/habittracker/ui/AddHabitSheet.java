package com.example.habittracker.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.habittracker.R;
import com.example.habittracker.databinding.DialogAddHabitBinding;
import com.example.habittracker.db.entity.Category;
import com.example.habittracker.db.entity.FrequencyType;
import com.example.habittracker.db.entity.HabitModel;
import com.example.habittracker.viewmodel.HabitViewModel;
import com.example.habittracker.viewmodel.HabitViewModelFactory;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AddHabitSheet extends BottomSheetDialogFragment {

    private HabitViewModel viewModel;
    private int habitId = -1; // -1 for new habit
    private HabitModel existingHabit;
    private DialogAddHabitBinding binding;

    public static AddHabitSheet newInstance(int id) {
        AddHabitSheet fragment = new AddHabitSheet();
        Bundle args = new Bundle();
        args.putInt("habitId", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogAddHabitBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        HabitViewModelFactory factory = new HabitViewModelFactory(requireActivity().getApplication());
        viewModel = new ViewModelProvider(this, factory).get(HabitViewModel.class);
//        viewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);

        if (getArguments() != null) {
            habitId = getArguments().getInt("habitId", -1);
        }

        if (habitId == -1) {
            existingHabit = new HabitModel();
        } else {
            existingHabit = viewModel.getLiveHabitById(habitId).getValue();
        }

        // observe all categories. runs first, prepares the dropdown
        viewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories == null || categories.isEmpty()) return;

            List<String> names = new ArrayList<>();
            for (Category c : categories) {
                names.add(c.getName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, names);
            binding.categoryDropdown.setAdapter(adapter);

            if (habitId == -1 && existingHabit != null && existingHabit.categoryId == null) {
                Category defaultCategory = categories.get(0);
                existingHabit.categoryId = defaultCategory.getId();
                binding.categoryDropdown.setText(defaultCategory.getName(), false);
            }

            binding.categoryDropdown.setThreshold(1);
            binding.categoryDropdown.setOnItemClickListener(
                    (parent, itemView, position, id) -> {
                        if (existingHabit != null) {
                            existingHabit.categoryId = categories.get(position).getId();

                            // clear the validation error state as soon as any category has been chosen
                            View parentLayout = (View) binding.categoryDropdown.getParent().getParent();
                            if (parentLayout instanceof TextInputLayout) {
                                ((TextInputLayout) parentLayout).setError(null);
                            }
                        }
                    });

            if (habitId != -1 && existingHabit != null) {
                prefillCategoryName(categories, existingHabit.categoryId);
            }
        });

        // observe habit data (Edit Mode)
        if (habitId != -1) {
            binding.textViewDialogAddHabitTitle.setText("Редактировать");

            viewModel.getHabitWithCategory(habitId).observe(getViewLifecycleOwner(), habitWithCategory -> {
                if (habitWithCategory == null) return;

                existingHabit = habitWithCategory.getHabit();
                binding.editTextHabitName.setText(existingHabit.getName());
                binding.editTextDescription.setText(existingHabit.getDescription());
                binding.editTextTargetDays.setText(String.valueOf(existingHabit.getTargetDays()));

                // pre-fill if categories observer have already finished loading
                List<Category> currentCategories = viewModel.getAllCategories().getValue();
                if (currentCategories != null) {
                    prefillCategoryName(currentCategories, existingHabit.categoryId);
                } else if (habitWithCategory.getCategory() != null) {
                    // fallback. use the joined category object directly
                    binding.categoryDropdown.setText(habitWithCategory.getCategory().getName(), false);
                }
            });
        }

        // save
        binding.buttonSave.setOnClickListener(v -> {
            if (!isValid(binding.editTextHabitName) || !isValid(binding.editTextTargetDays)) return;

            existingHabit.setName(binding.editTextHabitName.getText().toString().trim());
            existingHabit.setDescription(binding.editTextDescription.getText().toString().trim());
            existingHabit.setTargetDays(Integer.parseInt(binding.editTextTargetDays.getText().toString().trim()));

            if (habitId == -1) {
                existingHabit.setCreatedAt(LocalDateTime.now());
                viewModel.addHabit(existingHabit);
            } else {
                if (existingHabit.getCurrentStreak() >= existingHabit.getTargetDays()) {
                    existingHabit.setCompleted(true);
                }
                viewModel.updateHabit(existingHabit);
            }
            dismiss();
        });
    }

    private boolean isValid(TextInputEditText editText) {
        boolean isValid = true;
        if (editText.getText().toString().isBlank()) {
            View parentLayout = (View) editText.getParent().getParent();
            if (parentLayout instanceof TextInputLayout) {
                ((TextInputLayout) parentLayout)
                        .setError("Необходимо заполнить");
            }
            isValid = false;
        } else if (editText.getId() == R.id.editTextTargetDays &&
                Integer.parseInt(editText.getText().toString().trim()) < 1) {
            ((TextInputLayout) editText.getParent().getParent())
                    .setError("Должно быть > 0");

            isValid = false;
        }
        return isValid;
    }

    private void prefillCategoryName(List<Category> categories, long categoryId) {
        for (Category c : categories) {
            if (c.getId() == categoryId) {
                binding.categoryDropdown.setText(c.getName(), false);
                break;
            }
        }
    }

}
