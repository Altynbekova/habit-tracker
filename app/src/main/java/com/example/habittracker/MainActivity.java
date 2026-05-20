package com.example.habittracker;

import android.os.Bundle;

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
import com.example.habittracker.repository.ThemeManager;
import com.example.habittracker.ui.AddHabitSheet;
import com.example.habittracker.viewmodel.SettingsViewModel;
import com.example.habittracker.viewmodel.SettingsViewModelFactory;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private SettingsViewModel settingsViewModel;
    private MaterialSwitch themeSwitch;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        navController = navHostFragment.getNavController();

        setSupportActionBar(binding.toolbar);
        NavigationUI.setupActionBarWithNavController(this, navController);

        FloatingActionButton fab = binding.fabAddHabit;
        ViewCompat.setOnApplyWindowInsetsListener(fab, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) v.getLayoutParams();
            int marginPx = (int) (24 * getResources().getDisplayMetrics().density);

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
            addHabitSheet.show(getSupportFragmentManager(), "AddHabitTag");
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
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}