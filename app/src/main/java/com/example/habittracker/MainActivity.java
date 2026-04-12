package com.example.habittracker;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

        FloatingActionButton fab = binding.fabAddHabit;
        ViewCompat.setOnApplyWindowInsetsListener(fab, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // get the current LayoutParams of the FAB
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) v.getLayoutParams();
            // convert 24dp standard margin to pixels
            int marginPx = (int) (24 * getResources().getDisplayMetrics().density);

            // Set bottom margin: Height of Nav Bar + desired 24dp padding
            params.bottomMargin = systemBars.bottom + marginPx;
            params.rightMargin = marginPx; // Keep the side margin consistent too
            v.setLayoutParams(params);

            return insets;
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.habitListFragment) { // Replace with your actual start destination ID
                fab.show();
            } else {
                fab.hide();
            }
        });

        fab.setOnClickListener(v -> {
            AddHabitSheet addHabitSheet = new AddHabitSheet();
            addHabitSheet.show(getSupportFragmentManager(), "AddHabitTag");//todo replace AddHabitTag with AddHabitSheet.class.getSimpleName()
        });


        DynamicColors.applyToActivitiesIfAvailable(getApplication());
        SettingsViewModelFactory settingsViewModelFactory = new SettingsViewModelFactory(new ThemeManager(this));
        settingsViewModel = new ViewModelProvider(this, settingsViewModelFactory).get(SettingsViewModel.class);
        themeSwitch = binding.themeSwitch;
        settingsViewModel.getTheme().observe(this, isDark -> {
            int targetMode = isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
                AppCompatDelegate.setDefaultNightMode(targetMode);
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