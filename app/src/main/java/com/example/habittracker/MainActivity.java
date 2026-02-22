package com.example.habittracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.habittracker.databinding.ActivityMainBinding;
import com.example.habittracker.db.HabitModel;
import com.example.habittracker.ui.OnClickItemInterface;
import com.example.habittracker.ui.adapter.HabitAdapter;
import com.example.habittracker.viewmodel.HabitViewModel;

import java.time.LocalDate;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnClickItemInterface {
    private HabitAdapter habitAdapter;
    private ActivityMainBinding binding;
    private HabitViewModel habitViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        habitViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())
                .create(HabitViewModel.class);

        binding.recyclerViewHabits.setLayoutManager(new LinearLayoutManager(this));
        habitAdapter = new HabitAdapter(this);
        binding.recyclerViewHabits.setAdapter(habitAdapter);

        habitViewModel.getAllHabitsLive().observe(MainActivity.this, new Observer<List<HabitModel>>() {
            @Override
            public void onChanged(List<HabitModel> habitModels) {
                if (habitModels != null) {
                    habitAdapter.setHabits(habitModels);

                }
            }
        });

        binding.fabAddHabit.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, AddHabitActivity.class)));
    }

    @Override
    public void onClickItem(HabitModel habitModel, boolean toEdit) {
        if (toEdit) {
            Intent intent = new Intent(MainActivity.this, AddHabitActivity.class);
            intent.putExtra("model", habitModel);
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
            Toast.makeText(this, "Привычка выполнена!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Привычка уже выполнена сегодня", Toast.LENGTH_SHORT).show();
        }
    }
}