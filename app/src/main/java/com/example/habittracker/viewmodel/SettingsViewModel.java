package com.example.habittracker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habittracker.repository.ThemeManager;

public class SettingsViewModel extends ViewModel {
    private final ThemeManager themeManager;
    private final MutableLiveData<Boolean> themeLiveData = new MutableLiveData<>();

    public SettingsViewModel(ThemeManager manager) {
        this.themeManager = manager;
        themeManager.getTheme().subscribe(themeLiveData::postValue);
    }

    public LiveData<Boolean> getTheme() {
        return themeLiveData;
    }

    public void toggleTheme(boolean isDark) {
        themeManager.setTheme(isDark);
    }
}
