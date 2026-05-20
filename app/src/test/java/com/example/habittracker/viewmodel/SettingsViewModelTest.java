package com.example.habittracker.viewmodel;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.habittracker.repository.ThemeManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.reactivex.rxjava3.core.Flowable;

@RunWith(MockitoJUnitRunner.class)
public class SettingsViewModelTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private ThemeManager themeManager;

    @Mock
    private Observer<Boolean> themeObserver;

    private SettingsViewModel viewModel;

    @Before
    public void setUp() {
        when(themeManager.getTheme()).thenReturn(Flowable.just(true));
        viewModel = new SettingsViewModel(themeManager);
    }

    @Test
    public void constructor_subscribesAndUpdatesLiveData() {
        viewModel.getTheme().observeForever(themeObserver);

        verify(themeObserver).onChanged(true);
        assertNotNull(viewModel.getTheme().getValue());
        assertTrue(viewModel.getTheme().getValue());

        viewModel.getTheme().removeObserver(themeObserver);
    }

    @Test
    public void toggleTheme_callsThemeManagerToSaveState() {
        viewModel.toggleTheme(true);

        verify(themeManager).setTheme(true);
    }
}
