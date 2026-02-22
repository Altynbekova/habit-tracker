package com.example.habittracker.ui.adapter;

import android.graphics.Color;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

import com.example.habittracker.R;

public class DataBindingAdapter {
    @BindingAdapter("android:background")
    public static void setBackgroundColor(View view, String color) {
        if (color != null) {
            try {
                int parsedColor = Color.parseColor(color);
                view.setBackgroundColor(parsedColor);
            } catch (IllegalArgumentException e) {
                view.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.pastel_blue));
            }
        }
    }
}
