package com.example.habittracker;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.habittracker.databinding.ActivityMainBinding;
import com.example.habittracker.db.entity.HabitModel;
import com.example.habittracker.repository.ThemeManager;
import com.example.habittracker.ui.AddHabitSheet;
import com.example.habittracker.ui.OnClickItemInterface;
import com.example.habittracker.ui.adapter.HabitAdapter;
import com.example.habittracker.viewmodel.HabitViewModel;
import com.example.habittracker.viewmodel.SettingsViewModel;
import com.example.habittracker.viewmodel.SettingsViewModelFactory;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.time.LocalDate;

public class MainActivity extends AppCompatActivity implements OnClickItemInterface {
    private HabitAdapter habitAdapter;
    private ActivityMainBinding binding;
    private HabitViewModel habitViewModel;
    private SettingsViewModel settingsViewModel;
    private MaterialSwitch themeSwitch;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /*habitViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
                .create(HabitViewModel.class);

        binding.recyclerViewHabits.setLayoutManager(new LinearLayoutManager(this));
        habitAdapter = new HabitAdapter(this);
        binding.recyclerViewHabits.setAdapter(habitAdapter);

        habitViewModel.getAllHabitsLive().observe(MainActivity.this, habitModels -> {
            if (habitModels != null) {
                habitAdapter.setHabits(habitModels);
            }
        });

        binding.fabAddHabit.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, AddHabitActivity.class)));*/

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        navController = navHostFragment.getNavController();

        setSupportActionBar(binding.toolbar);
        // Настройка ActionBar для отображения стрелки "Назад"
        NavigationUI.setupActionBarWithNavController(this, navController);

        binding.fabAddHabit.setOnClickListener(v -> {
            AddHabitSheet addHabitSheet = new AddHabitSheet();
            addHabitSheet.show(getSupportFragmentManager(), "AddHabitTag");//todo remove -Tag ending from AddHabitTag
        });

        DynamicColors.applyToActivitiesIfAvailable(getApplication());
        SettingsViewModelFactory settingsViewModelFactory = new SettingsViewModelFactory(new ThemeManager(this));
        settingsViewModel = new ViewModelProvider(this, settingsViewModelFactory).get(SettingsViewModel.class);
        themeSwitch = binding.themeSwitch;
        settingsViewModel.getTheme().observe(this, isDark -> {
            if (isDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            themeSwitch.setChecked(isDark);
        });

        themeSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            settingsViewModel.toggleTheme(isChecked);
        });
    }

    @Override
    public void onClickItem(HabitModel habitModel, boolean toEdit) {
        if (toEdit) {
            Intent intent = new Intent(MainActivity.this, AddHabitActivity.class);
            intent.putExtra("habitId", habitModel.getId());
            startActivity(intent);
        } else {
            habitViewModel.deleteHabit(habitModel);
            Toast.makeText(this, "Привычка удалена", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCompleteItem(HabitModel habitModel) {
        if (!habitModel.isTodayCompleted()) {
            habitModel.setLastCompletedDate(LocalDate.now().toString());
            habitModel.setCurrentStreak(habitModel.getCurrentStreak() + 1);
            habitViewModel.updateHabit(habitModel);

            if (habitModel.getCurrentStreak() >= habitModel.getTargetDays()) {
//            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                NotificationManager notificationManager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(habitModel.getId());
            }
            Toast.makeText(this, "Привычка выполнена!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Привычка уже выполнена сегодня", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}