package com.example.habittracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.habittracker.databinding.ActivityAddHabitBinding;
import com.example.habittracker.db.HabitModel;
import com.example.habittracker.ui.AddHabitActivityHandler;
import com.example.habittracker.viewmodel.AddHabitViewModel;
import com.example.habittracker.viewmodel.HabitViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.ExecutionException;

public class AddHabitActivity extends AppCompatActivity implements AddHabitActivityHandler {

    HabitModel model = new HabitModel();
    private HabitViewModel habitViewModel;
    private AddHabitViewModel addHabitViewModel;
    private int habitId;
    private ActivityAddHabitBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.activity_add_habit);

        habitViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
                .create(HabitViewModel.class);*/

        // Inflate layout using data binding
        binding = ActivityAddHabitBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel
        habitViewModel = new ViewModelProvider(this).get(HabitViewModel.class);
        addHabitViewModel = new ViewModelProvider(this).get(AddHabitViewModel.class);
        // Bind the ViewModel to the layout
        binding.setHabitViewModel(habitViewModel);
        binding.setAddHabitViewModel(addHabitViewModel);

        // Set the LiveData owner to enable lifecycle observation
//        binding.setLifecycleOwner(this);

//        initViews();
//        setupListeners();
        model.setTargetDays(7);

        Intent intent = getIntent();
        if (intent.hasExtra("model")) {
            model = intent.getParcelableExtra("model");
            binding.toolbar.setTitle("Редактировать привычку");
            habitId = model.getId();
            /*try {
                loadHabitData();
            } catch (ExecutionException | InterruptedException e) {
                Log.e("MainActivity", "Cannot loadHabits()");
//                throw new RuntimeException(e);
            }*/
        } else {
            habitId = -1;
        }
        binding.setHabitModel(model);
        binding.setEventsHandler(this);

        binding.pickTimeButton.setOnClickListener(v -> showTimePicker(model));
    }

    private void showTimePicker(HabitModel model) {
        LocalTime time;
        if(model.getNotificationTime() == null || model.getNotificationTime().isEmpty()){
            time = LocalTime.now();
        } else {
          time = LocalTime.parse(model.getNotificationTime());
        }
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(time.getHour())
                .setMinute(time.getMinute())
                .setTitleText("Выберите время")
                .build();

        picker.show(getSupportFragmentManager(), "MATERIAL_TIME_PICKER");

        picker.addOnPositiveButtonClickListener(v -> {
            // Передаем результат в ViewModel
            addHabitViewModel.updateTime(model.getId(),picker.getHour(), picker.getMinute());
            model.setNotificationTime(LocalTime.of(picker.getHour(), picker.getMinute()).toString());
        });
    }
    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private String getSelectedColor() {
        int selectedId = binding.radioGroupColors.getCheckedRadioButtonId();

        if (selectedId == R.id.radioBlue) return "#AEC6CF";
        else if (selectedId == R.id.radioGreen) return "#B5EAD7";
        else if (selectedId == R.id.radioLavender) return "#E6E6FA";
        else if (selectedId == R.id.radioPeach) return "#FFD8B1";
        else return "#C1E1C1";
    }

    private void loadHabitData() throws ExecutionException, InterruptedException {
        /*if (habitId != -1) {
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
            }
        }*/
    }

    @Override
    public void onIncrease() {
        model.setTargetDays(model.getTargetDays() + 1);
        binding.textViewTargetDays.setText(String.valueOf(model.getTargetDays()));
    }

    @Override
    public void onDecrease() {
        if (model.getTargetDays() > 1) {
            model.setTargetDays(model.getTargetDays() - 1);
            binding.textViewTargetDays.setText(String.valueOf(model.getTargetDays()));
        }
    }

    @Override
    public void onSave(HabitModel habitModel) {

        if (habitModel.getName()==null || habitModel.getName().trim().isEmpty()) {
            Toast.makeText(this, "Введите название привычки", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedColor = getSelectedColor();
        habitModel.setColor(selectedColor);
        habitModel.setNotificationTime(addHabitViewModel.notificationTime.get());

        if (habitId == -1) {
            habitViewModel.insertHabit(habitModel);

            // Set the desired time (e.g., from user input in ToDo task)
            /*Calendar calendar = Calendar.getInstance();
            LocalTime now = LocalTime.now();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, now.getHour());
            calendar.set(Calendar.MINUTE,  now.plusMinutes(1).getMinute());*/

            LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now(),
                    LocalTime.parse(habitModel.getNotificationTime()));
            // ... add other time logic as needed

            Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
            intent.putExtra("TASK_TITLE", habitModel.getName()); // Pass task data
            intent.putExtra("NOTIFICATION_ID", habitModel.getId()); // Pass notification id
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, habitModel.getId(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE); // Use a unique ID for each task

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            // Use setExact() for accurate timing, setRepeating() for recurring tasks
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    60_000L, pendingIntent);

            Toast.makeText(this, "Привычка создана!", Toast.LENGTH_SHORT).show();
        } else {
            try {
                HabitModel habit = habitViewModel.getHabitById(habitId);
                if (habit != null) {
                    habitModel.setId(habit.getId());
                    habitModel.setCurrentStreak(habit.getCurrentStreak());
                    habitModel.setLastCompletedDate(habit.getLastCompletedDate());
                    habitViewModel.updateHabit(habitModel);
                    Toast.makeText(this, "Привычка обновлена!", Toast.LENGTH_SHORT).show();
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        finish();
    }
}