package com.example.habittracker.ui;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.habittracker.R;
import com.example.habittracker.db.entity.HabitCompletion;
import com.example.habittracker.util.NotificationHelper;
import com.example.habittracker.util.Utils;
import com.example.habittracker.viewmodel.HabitViewModel;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Angle;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.Spread;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class HabitDetailFragment extends Fragment {

    private HabitViewModel habitViewModel;
    private int habitId;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Check if view still exists (fragment might have been closed)
                    if (getView() != null) {
                        TextView textNotificationTime = getView().findViewById(R.id.textNotificationTime);
                        MaterialSwitch switchNotification = getView().findViewById(R.id.switchNotification);

                        // Call your specific signature
                        showTimePicker(textNotificationTime, switchNotification);
                    }
                } else {
                    Snackbar.make(requireView(), "Уведомления отключены", Snackbar.LENGTH_SHORT).show();
                }
            });


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve ID passed from HabitListFragment
        if (getArguments() != null) {
            habitId = getArguments().getInt("habitId");
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Уведомления отключены")
                .setMessage("Без этого разрешения приложение не сможет напоминать вам о привычке. Вы можете включить его в настройках приложения.")
                .setPositiveButton("В настройки", (d, w) -> {
                    // Open App Info settings
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Отмена", null)
                .show();
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
        habitViewModel.getLiveHabitById(habitId).observe(getViewLifecycleOwner(), habit -> {
            if (habit != null) {
                nameText.setText(habit.getName());
                descText.setText(habit.getDescription());
                if (habit.isCompleted) {
                    view.findViewById(R.id.btnComplete).setClickable(false);
                    view.findViewById(R.id.btnEdit).setClickable(false);
                    view.findViewById(R.id.layoutNotification).setClickable(false);
                    ((MaterialSwitch) view.findViewById(R.id.switchNotification)).setChecked(false);
                    view.findViewById(R.id.switchNotification).setClickable(false);
                }
//                    streakText.setText("Прогресс: "+ habit.getTargetDays()+ "дн.");
                    /*if(!habit.isCompleted) {
                        textViewProgress.setText(habit.getTargetDays());
                    }*/
            }
        });


        /*view.findViewById(R.id.btnComplete).setOnClickListener(v -> {
            try {
                long habitCompletionInsertId = habitViewModel.markAsCompleted(habitId);
                if (habitCompletionInsertId != -1) {
                    Snackbar.make(view, "Привычка выполнена!", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(view, "Сегодня уже выполнялась!", Snackbar.LENGTH_SHORT).show();
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.e(Utils.TAG, "Cannot mark habit as completed", e);
                throw new RuntimeException(e);
            }
        });*/

        /*habitViewModel.getCompletionStatus().observe(getViewLifecycleOwner(), message -> {
//            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
        });*/
        habitViewModel.getMarkDoneEvent().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return; // Safety check

            switch (result) {
                case SUCCESS:
//                    Toast.makeText(requireContext(), "Streak updated!", Toast.LENGTH_SHORT).show();
                    Snackbar.make(view, "Привычка выполнена!", Snackbar.LENGTH_SHORT).show();
                    break;
                case GOAL_REACHED:
                    showConfettiAnimation();
                    NotificationHelper.cancelAlarm(view.getContext(), habitId);
//                    Toast.makeText(requireContext(), "Congratulations! Goal reached!", Toast.LENGTH_LONG).show();
                    Snackbar.make(view, "Цель достигнута!", Snackbar.LENGTH_SHORT).show();
                    break;
                case ALREADY_DONE:
//                    Toast.makeText(requireContext(), "Already done for today.", Toast.LENGTH_SHORT).show();
                    Snackbar.make(view, "Сегодня уже выполнялась!", Snackbar.LENGTH_SHORT).show();
                    break;
            }
        });

// Trigger the action
//        view.findViewById(R.id.btnComplete).setOnClickListener(v -> habitViewModel.completeHabit(habitId));
        view.findViewById(R.id.btnComplete).setOnClickListener(v -> habitViewModel.completeHabit(habitId));

        view.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Удалить?")
                    .setMessage("Это приведет к архивированию привычки.")
                    .setPositiveButton("Удалить", (dialog, which) -> {
                        // Cancel notification before deleting/archiving
                        NotificationHelper.cancelAlarm(requireContext(), habitId);

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


        TextView textNotificationTime = view.findViewById(R.id.textNotificationTime);
        MaterialSwitch switchNotification = view.findViewById(R.id.switchNotification);

        /*// 1. Observe the Habit to set the initial UI state
        try {
            habitViewModel.getLiveHabitById(habitId).observe(getViewLifecycleOwner(), habit -> {
                if (habit != null && habit.reminderTime != null) {
                    String time = habit.reminderTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                    textNotificationTime.setText("Ежедневно в " + time);
                    // switchNotification.setChecked(habit.isNotificationEnabled); // If you have this boolean
                }
            });
        } catch (Exception e) {
            Log.e("HabitDetail", "Error loading habit", e);
        }*/
        habitViewModel.getReminderForHabit(habitId).observe(getViewLifecycleOwner(), reminder -> {
            if (reminder != null) {
                String formattedTime = reminder.time.format(DateTimeFormatter.ofPattern("HH:mm"));
                textNotificationTime.setText("Ежедневно в " + formattedTime);
                switchNotification.setChecked(reminder.enabled);
            } else {
                textNotificationTime.setText("Уведомление не установлено");
                switchNotification.setChecked(false);
            }
        });

        // 2. Setup the Click Listener for the Time Picker
        view.findViewById(R.id.layoutNotification).setOnClickListener(v -> {
            /*// 2. Check Notification Permission (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Trigger the launcher we created earlier
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
                    return; // Stop here until permission is granted
                }
            }

            // 3. Permission is OK, show the Time Picker
            showTimePicker(textNotificationTime, switchNotification);*/

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED) {
                    showTimePicker(textNotificationTime, switchNotification);
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            } else {
                showTimePicker(textNotificationTime, switchNotification);
            }

            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED) {
                    // Already have permission
                    showTimePicker(textNotificationTime, switchNotification);
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    // Explain why you need it
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Разрешение на уведомления")
                            .setMessage("Чтобы вы не забывали о привычке, приложению нужно разрешение на отправку уведомлений.")
                            .setPositiveButton("OK", (d, w) -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS))
                            .show();
                } else {
                    // Directly ask
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            } else {
                // Android 12 and below don't need runtime permission for notifications
                showTimePicker(textNotificationTime, switchNotification);
            }*/
        });

        // 3. Handle Switch toggle
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            /*// You might want to enable/disable the actual AlarmManager here

            // Only update if the user manually clicked it (prevents infinite loops from LiveData)
            if (buttonView.isPressed()) {
                habitViewModel.updateReminderStatus(habitId, isChecked);
            }*/
            habitViewModel.updateReminderStatus(habitId, isChecked);
            /*try {
                NotificationHelper.scheduleAlarm(requireContext(), habitId, habitViewModel.getHabitById(habitId).getName(), habitViewModel.getReminderForHabit(habitId).getValue().time);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(Utils.TAG, "onViewCreated: cannot find habit by id=" + habitId, e);
                throw new RuntimeException(e);
            }*/

            /*// Use buttonView.isPressed() to ensure this was a USER click,
            // not a programmatic change from a LiveData observer
            if (buttonView.isPressed()) {
                habitViewModel.toggleReminder(habitId, isChecked);
            }*/
        });
    }

    private void showConfettiAnimation() {
        final KonfettiView konfettiView = getView().findViewById(R.id.konfettiView);

        EmitterConfig emitterConfig = new Emitter(3L, TimeUnit.SECONDS).perSecond(50);
        konfettiView.start(
                new PartyFactory(emitterConfig)
                        .angle(Angle.TOP)
                        .spread(Spread.WIDE)
                        .setSpeedBetween(10f, 30f)
                        .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                        .position(new Position.Relative(0.5, 0.3)) // Top center
                        .build()
        );
    }


    private void showTimePicker(TextView timeTextView, MaterialSwitch notificationSwitch) {
        LocalTime now = LocalTime.now();

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> {
                    LocalTime selectedTime = LocalTime.of(hourOfDay, minute);
                    String formattedTime = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"));

                    // Update UI
                    timeTextView.setText("Ежедневно в " + formattedTime);
                    notificationSwitch.setChecked(true);

                    // Update Database via ViewModel
                    // You'll need to create this method in your ViewModel
                    /*habitViewModel.updateReminderTime(habitId, selectedTime);*/
                    // Save to Reminder entity, NOT HabitModel
                    habitViewModel.setReminder(habitId, selectedTime);

//                    NotificationHelper.scheduleAlarm(requireContext(), habitId, selectedTime);
                    // 5. Schedule the actual system alarm
                    // Assuming you have a Habit object or at least the name
                    habitViewModel.getLiveHabitById(habitId).observe(getViewLifecycleOwner(), habit -> {
                        if (habit != null) {
                            NotificationHelper.scheduleAlarm(requireContext(), habitId, habit.getName(), selectedTime);
                        }
                    });

                    Snackbar.make(requireView(), "Время уведомления обновлено", Snackbar.LENGTH_SHORT).show();
                },
                now.getHour(),
                now.getMinute(),
                true);

        timePickerDialog.show();
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

