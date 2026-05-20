package com.example.habittracker.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.habittracker.repository.AppRepo;

public class HabitViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final AppRepo repository;

    public HabitViewModelFactory(@NonNull Application application) {
        this.application = application;
        this.repository = new AppRepo(application);
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HabitViewModel.class)) {
            return (T) new HabitViewModel(application, repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}

