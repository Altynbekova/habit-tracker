package com.example.habittracker.ui.adapter;

import android.graphics.Color;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

public class DataBindingAdapter {
    @BindingAdapter("android:background")
    public static void setBackgroundColor(View view, String color) {
        if (color != null) {
            try {
                int parsedColor = Color.parseColor(color);
                view.setBackgroundColor(parsedColor);
            } catch (IllegalArgumentException e) {
                view.setBackgroundColor(ContextCompat.getColor(view.getContext(), com.google.android.material.R.color.material_dynamic_neutral50));
            }
        }
    }
}
