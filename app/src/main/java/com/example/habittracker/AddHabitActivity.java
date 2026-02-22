package com.example.habittracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.habittracker.db.HabitModel;
import com.example.habittracker.viewmodel.HabitViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.concurrent.ExecutionException;

public class AddHabitActivity extends AppCompatActivity {

    private EditText editTextHabitName, editTextHabitDescription;
    private RadioGroup radioGroupColors;
    private TextView textViewTargetDays;
    private Button buttonIncrease, buttonDecrease, buttonSave;
    private HabitViewModel habitViewModel;
    private int targetDays = 7;
    private int habitId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        habitViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
                .create(HabitViewModel.class);

        initViews();
        setupListeners();

        Intent intent = getIntent();
        if (intent.hasExtra("model")) {
            HabitModel model = intent.getParcelableExtra("model");
            habitId = model != null ? model.getId() : -1;
            try {
                loadHabitData();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("MainActivity", "Cannot loadHabits()");
//                throw new RuntimeException(e);
            }
        }
    }

    private void initViews() {
        editTextHabitName = findViewById(R.id.editTextHabitName);
        editTextHabitDescription = findViewById(R.id.editTextHabitDescription);
        radioGroupColors = findViewById(R.id.radioGroupColors);
        textViewTargetDays = findViewById(R.id.textViewTargetDays);
        buttonIncrease = findViewById(R.id.buttonIncrease);
        buttonDecrease = findViewById(R.id.buttonDecrease);
        buttonSave = findViewById(R.id.buttonSave);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        textViewTargetDays.setText(String.valueOf(targetDays));
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        buttonIncrease.setOnClickListener(v -> {
            targetDays++;
            textViewTargetDays.setText(String.valueOf(targetDays));
        });

        buttonDecrease.setOnClickListener(v -> {
            if (targetDays > 1) {
                targetDays--;
                textViewTargetDays.setText(String.valueOf(targetDays));
            }
        });

        buttonSave.setOnClickListener(v -> {
            try {
                saveHabit();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("MainActivity", "Cannot loadHabits()");
//                throw new RuntimeException(e);
            }
        });
    }

    private void saveHabit() throws ExecutionException, InterruptedException {
        String name = editTextHabitName.getText().toString().trim();
        String description = editTextHabitDescription.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название привычки", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedColor = getSelectedColor();

        if (habitId == -1) {
            HabitModel habit = new HabitModel(name, description, selectedColor, targetDays);
            habitViewModel.insertHabit(habit);
            Toast.makeText(this, "Привычка создана!", Toast.LENGTH_SHORT).show();
        } else {
            HabitModel habit = habitViewModel.getHabitById(habitId);
            if (habit != null) {
                habit.setName(name);
                habit.setDescription(description);
                habit.setColor(selectedColor);
                habit.setTargetDays(targetDays);
                habitViewModel.updateHabit(habit);
                Toast.makeText(this, "Привычка обновлена!", Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    private String getSelectedColor() {
        int selectedId = radioGroupColors.getCheckedRadioButtonId();

        if (selectedId == R.id.radioBlue) return "#AEC6CF";
        else if (selectedId == R.id.radioGreen) return "#B5EAD7";
        else if (selectedId == R.id.radioLavender) return "#E6E6FA";
        else if (selectedId == R.id.radioPeach) return "#FFD8B1";
        else return "#C1E1C1";
    }

    private void loadHabitData() throws ExecutionException, InterruptedException {
        if (habitId != -1) {
            HabitModel habit = habitViewModel.getHabitById(habitId);
            if (habit != null) {
                editTextHabitName.setText(habit.getName());
                editTextHabitDescription.setText(habit.getDescription());
                targetDays = habit.getTargetDays();
                textViewTargetDays.setText(String.valueOf(targetDays));

                String habitColor = habit.getColor();
                switch (habitColor) {
                    case "#AEC6CF":
                        radioGroupColors.check(R.id.radioBlue);
                        break;
                    case "#B5EAD7":
                        radioGroupColors.check(R.id.radioGreen);
                        break;
                    case "#E6E6FA":
                        radioGroupColors.check(R.id.radioLavender);
                        break;
                    case "#FFD8B1":
                        radioGroupColors.check(R.id.radioPeach);
                        break;
                    case "#C1E1C1":
                        radioGroupColors.check(R.id.radioMint);
                        break;
                }

                MaterialToolbar toolbar = findViewById(R.id.toolbar);
                toolbar.setTitle("Редактировать привычку");
            }
        }
    }
}