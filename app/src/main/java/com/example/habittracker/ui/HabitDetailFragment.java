package com.example.habittracker.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.habittracker.R;
import com.example.habittracker.db.entity.HabitCompletion;
import com.example.habittracker.util.Utils;
import com.example.habittracker.viewmodel.HabitViewModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class HabitDetailFragment extends Fragment {

    private HabitViewModel habitViewModel;
    private int habitId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve ID passed from HabitListFragment
        if (getArguments() != null) {
            habitId = getArguments().getInt("habitId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_habit_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        /*long habitId = getArguments() != null ? getArguments().getLong("habitId") : -1;

        HabitDetailViewModel viewModel = new ViewModelProvider(this).get(HabitDetailViewModel.class);
        // Теперь можно подписаться на детали этой привычки*/

        super.onViewCreated(view, savedInstanceState);

        TextView nameText = view.findViewById(R.id.textDetailName);
        TextView descText = view.findViewById(R.id.textLongDescription);
        TextView streakText = view.findViewById(R.id.textStreakCount);
//        TextView textViewProgress = view.findViewById(R.id.textViewProgress);

        habitViewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);

        // Observe specific habit details
        // Note: You might need to add getHabitById(id) to your ViewModel/Repo
        try {
            habitViewModel.getLiveHabitById(habitId).observe(getViewLifecycleOwner(), habit -> {
                if (habit != null) {
                    nameText.setText(habit.getName());
                    descText.setText(habit.getDescription());
//                    streakText.setText("Прогресс: "+ habit.getTargetDays()+ "дн.");
                    /*if(!habit.isCompleted) {
                        textViewProgress.setText(habit.getTargetDays());
                    }*/
                }
            });
        } catch (ExecutionException | InterruptedException e) {
            Log.e(Utils.TAG, "onViewCreated: cannot observe live habit", e);
//            throw new RuntimeException(e);
        }

        view.findViewById(R.id.btnComplete).setOnClickListener(v -> {
            try {
                long habitCompletionInsertId = habitViewModel.markAsCompleted(habitId);
                if (habitCompletionInsertId != -1){
                    Snackbar.make(view, "Привычка выполнена!", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(view, "Сегодня уже выполнялась!", Snackbar.LENGTH_SHORT).show();
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.e(Utils.TAG, "Cannot mark habit as completed", e);
                throw new RuntimeException(e);
            }
        });

        view.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Удалить?")
                    .setMessage("Это приведет к архивированию привычки.")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        habitViewModel.archiveHabit(habitId);
                        Navigation.findNavController(view).navigateUp();
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });

        // Edit Button
        view.findViewById(R.id.btnEdit).setOnClickListener(v -> {
            // Future: Open a pre-filled AddHabitSheet for editing
            AddHabitSheet editSheet = AddHabitSheet.newInstance(habitId);
            editSheet.show(getChildFragmentManager(), "EditHabitTag");
        });

        // Update Streak Progress
        /*habitViewModel.getHistoryForHabit(habitId).observe(getViewLifecycleOwner(), history -> {
            int currentStreak = calculateStreak(history);
            TextView textStreakCount = view.findViewById(R.id.textStreakCount);
            LinearProgressIndicator streakProgress = view.findViewById(R.id.streakProgress);

            textStreakCount.setText(currentStreak + " Day Streak!");
            streakProgress.setProgress((currentStreak % 20) * 5); // Example: 20-day milestones
        });*/

        try {
            habitViewModel.getLiveHabitById(habitId).observe(getViewLifecycleOwner(), habit -> {
                habitViewModel.getHistoryForHabit(habitId).observe(getViewLifecycleOwner(), history -> {
                    int currentStreak = calculateStreak(history);
                    int target = habit.getTargetDays();

                    float progressPercent = (target > 0) ? ((float) currentStreak / target) * 100 : 0;
                    int finalProgress = Math.min(100, (int) progressPercent);

                    // 2. Update UI
                    LinearProgressIndicator streakProgress = view.findViewById(R.id.streakProgress);
//                    textStreakCount.setText(currentStreak + " / " + target + " Days");
                    streakProgress.setProgress(finalProgress, true);

                    // Optional: Change color if goal is reached
                    if (finalProgress >= 100) {
                        streakProgress.setIndicatorColor(Color.GREEN);
                    }
                });
            });
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private int calculateStreak(List<HabitCompletion> history) {
        if (history == null || history.isEmpty()) return 0;

        int streak = 0;
        LocalDate today = LocalDate.now();

        // Simple logic: count consecutive days backwards from today
        for (int i = 0; i < history.size(); i++) {
            if (history.get(i).completionDate.equals(today.minusDays(i))) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }
}

