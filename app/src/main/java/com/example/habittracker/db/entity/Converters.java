package com.example.habittracker.db.entity;

import androidx.room.TypeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    @TypeConverter
    public static LocalDate fromDateString(String value) {
        return value == null ? null : LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @TypeConverter
    public static String toDateString(LocalDate date) {
        return date == null ? null : date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @TypeConverter
    public static LocalTime fromTimeString(String value) {
        return value == null ? null : LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME);
    }

    @TypeConverter
    public static String toTimeString(LocalTime time) {
        return time == null ? null : time.format(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    @TypeConverter
    public static LocalDateTime fromDateTimeString(String value) {
        return value == null ? null : LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @TypeConverter
    public static String toDateTimeString(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    // --- Lists of Integers (e.g., selectedDays) ---
    @TypeConverter
    public static List<Integer> fromIntegerList(String value) {
        if (value == null || value.isEmpty()) return null;
        return Arrays.stream(value.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    @TypeConverter
    public static String toIntegerList(List<Integer> list) {
        if (list == null || list.isEmpty()) return null;
        return list.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    // --- Lists of LocalTime (e.g., reminderTimes) ---
    @TypeConverter
    public static List<LocalTime> fromLocalTimeList(String value) {
        if (value == null || value.isEmpty()) return null;
        return Arrays.stream(value.split(","))
                .map(LocalTime::parse)
                .collect(Collectors.toList());
    }

    @TypeConverter
    public static String toLocalTimeList(List<LocalTime> list) {
        if (list == null || list.isEmpty()) return null;
        return list.stream()
                .map(LocalTime::toString)
                .collect(Collectors.joining(","));
    }

    @TypeConverter
    public static String fromEnum(Enum<?> status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static FrequencyType toFrequencyType(String value) {
        return value == null ? null : FrequencyType.valueOf(value);
    }

    @TypeConverter
    public static CompletionStatus toCompletionStatus(String value) {
        return value == null ? null : CompletionStatus.valueOf(value);
    }
}
