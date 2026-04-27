package com.example.habittracker.util;

import com.example.habittracker.R;
import com.example.habittracker.db.entity.Category;

import java.util.List;
import java.util.Map;

public class Utils {
    public static final String TAG = "HabitTrackerApplication";
    public static final Map<String, Integer> drawableMap = Map.of(
            "ic_health_metrics", R.drawable.ic_health_metrics,
            "ic_work_outline", R.drawable.ic_work_outline,
            "ic_person", R.drawable.ic_person,
            "ic_category", R.drawable.ic_category
    );

    public static List<Category> categories() {
        return List.of(
//                new Category("Health", Integer.parseInt("89F336", 16)),
//                new Category("Work", Integer.parseInt("FF9C27", 16)),
//                new Category("Personal", Integer.parseInt("FF4CAF", 16)),
//                new Category("Other", Integer.parseInt("B39DDB", 16))
                new Category("Здоровье", "ic_health_metrics"),
                new Category("Работа", "ic_work_outline"),
                new Category("Личное", "ic_person"),
                new Category("Другое", "ic_category")
        );
    }
}
