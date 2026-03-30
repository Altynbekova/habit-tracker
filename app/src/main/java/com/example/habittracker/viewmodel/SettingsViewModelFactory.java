package com.example.habittracker.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.habittracker.repository.ThemeManager;

public class SettingsViewModelFactory implements ViewModelProvider.Factory {
    private final ThemeManager themeManager;

    public SettingsViewModelFactory(ThemeManager themeManager) {
        this.themeManager = themeManager;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            return (T) new SettingsViewModel(themeManager);
        }
        //return ViewModelProvider.Factory.super.create(modelClass);
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
