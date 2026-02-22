package com.example.habittracker.db;

import androidx.room.TypeConverter;

public class Converters {
    @TypeConverter
    public static boolean[] fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        String[] strings = value.split(",");
        boolean[] booleans = new boolean[strings.length];
        for (int i = 0; i < strings.length; i++) {
            booleans[i] = Boolean.parseBoolean(strings[i]);
        }
        return booleans;
    }

    @TypeConverter
    public static String fromBooleanArray(boolean[] value) {
        if (value == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length; i++) {
            builder.append(value[i]);
            if (i < value.length - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }
}
