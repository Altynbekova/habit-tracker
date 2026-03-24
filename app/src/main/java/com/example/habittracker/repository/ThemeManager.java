package com.example.habittracker.repository;

import android.content.Context;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class ThemeManager {
    private final RxDataStore<Preferences> dataStore;
    private final Preferences.Key<Boolean> DARK_MODE_KEY = PreferencesKeys.booleanKey("dark_mode");

    public ThemeManager(Context context) {
        dataStore = new RxPreferenceDataStoreBuilder(context, "settings").build();
    }

    public Flowable<Boolean> getTheme() {
        return dataStore.data().map(prefs ->
                prefs.get(DARK_MODE_KEY) != null ? prefs.get(DARK_MODE_KEY) : false);
    }

    public void setTheme(boolean isDark) {
        dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(DARK_MODE_KEY, isDark);
            return Single.just(mutablePreferences);
        });
    }
}
