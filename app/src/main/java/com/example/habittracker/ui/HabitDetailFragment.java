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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.example.habittracker.viewmodel.HabitViewModel;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.Angle;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.Spread;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class HabitDetailFragment extends Fragment {
    private static final String TIME_FORMAT = "HH:mm";
    private HabitViewModel habitViewModel;
    private int habitId;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // check if view still exists (fragment might have been closed)
                    if (getView() != null) {
                        TextView textNotificationTime = getView().findViewById(R.id.textNotificationTime);
                        MaterialSwitch switchNotification = getView().findViewById(R.id.switchNotification);

                        // call your specific signature
                        showTimePicker(textNotificationTime, switchNotification);
                    }
                } else {
                    Snackbar.make(requireView(), "Уведомления отключены", Snackbar.LENGTH_SHORT).show();
                }
            });


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve habit ID passed from HabitListFragment
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
        super.onViewCreated(view, savedInstanceState);

        TextView nameText = view.findViewById(R.id.textDetailName);
        TextView descText = view.findViewById(R.id.textLongDescription);
        TextView streakText = view.findViewById(R.id.textStreakCount);

        habitViewModel = new ViewModelProvider(requireActivity()).get(HabitViewModel.class);

        habitViewModel.getLiveHabitById(habitId).observe(getViewLifecycleOwner(), habit -> {
            if (habit != null) {
                nameText.setText(habit.getName());
                descText.setText(habit.getDescription());
                if (habit.isCompleted) {
                    disableButtons(view);
                }
            }
        });

        habitViewModel.getMarkDoneEvent().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;

            switch (result) {
                case SUCCESS:
                    Snackbar.make(view, "Привычка выполнена!", Snackbar.LENGTH_SHORT).show();
                    break;
                case GOAL_REACHED:
                    disableButtons(view);
                    showConfettiAnimation();
                    NotificationHelper.cancelAlarm(view.getContext(), habitId);
                    Snackbar.make(view, "Цель достигнута!", Snackbar.LENGTH_SHORT).show();
                    break;
                case ALREADY_DONE:
                    Snackbar.make(view, "Сегодня уже выполнялась!", Snackbar.LENGTH_SHORT).show();
                    break;
            }
        });

        // Trigger the action
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

        view.findViewById(R.id.btnEdit).setOnClickListener(v -> {
            // for future open a pre-filled AddHabitSheet for editing
            AddHabitSheet editSheet = AddHabitSheet.newInstance(habitId);
            editSheet.show(getChildFragmentManager(), "EditHabitTag");
        });

        habitViewModel.getLiveHabitById(habitId).observe(getViewLifecycleOwner(), habit -> {
            habitViewModel.getHistoryForHabit(habitId).observe(getViewLifecycleOwner(), history -> {
                int currentStreak = calculateStreak(history);
                int target = habit.getTargetDays();

                float progressPercent = (target > 0) ? ((float) currentStreak / target) * 100 : 0;
                int finalProgress = Math.min(100, (int) progressPercent);

                LinearProgressIndicator streakProgress = view.findViewById(R.id.streakProgress);
                streakProgress.setProgress(finalProgress, true);

                if (finalProgress >= 100) {
                    streakProgress.setIndicatorColor(Color.GREEN);
                }
            });
        });


        TextView textNotificationTime = view.findViewById(R.id.textNotificationTime);
        MaterialSwitch switchNotification = view.findViewById(R.id.switchNotification);

        habitViewModel.getReminderForHabit(habitId).observe(getViewLifecycleOwner(), reminder -> {
            if (reminder != null) {
                String formattedTime = reminder.getTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                textNotificationTime.setText("Ежедневно в " + formattedTime);
                switchNotification.setChecked(reminder.isEnabled());
            } else {
                textNotificationTime.setText("Уведомление не установлено");
                switchNotification.setChecked(false);
            }
        });

        // setup the Click Listener for the Time Picker
        view.findViewById(R.id.layoutNotification).setOnClickListener(v -> {
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
        });

        // handle Switch toggle
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            habitViewModel.updateReminderStatus(habitId, isChecked);
        });
    }

    private static void disableButtons(@NonNull View view) {
        view.findViewById(R.id.btnComplete).setEnabled(false);
        view.findViewById(R.id.btnEdit).setEnabled(false);
        view.findViewById(R.id.layoutNotification).setEnabled(false);
        ((MaterialSwitch) view.findViewById(R.id.switchNotification)).setChecked(false);
        view.findViewById(R.id.switchNotification).setEnabled(false);
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
                    String formattedTime = selectedTime.format(DateTimeFormatter.ofPattern(TIME_FORMAT));

                    timeTextView.setText("Ежедневно в " + formattedTime);
                    notificationSwitch.setChecked(true);
                    habitViewModel.setReminder(habitId, selectedTime);

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

        // simple logic: count consecutive days backwards from today
        for (int i = 0; i < history.size(); i++) {
            if (history.get(i).getCompletionDate().equals(today.minusDays(i))) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }
}

