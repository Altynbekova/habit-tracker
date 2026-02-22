package com.example.habittracker.db;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

@Entity(tableName = "habits")
public class HabitModel implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String description;
    private String color;
    private int targetDays;
    private int currentStreak;
    private boolean[] completedDays = new boolean[7];
    private String lastCompletedDate = "";

    public HabitModel() {
    }

    public HabitModel(Parcel in) {
        id = in.readInt();
        name = in.readString();
        description = in.readString();
        color = in.readString();
        targetDays = in.readInt();
        currentStreak = in.readInt();
        completedDays = in.createBooleanArray();
        lastCompletedDate = in.readString();
    }

    public HabitModel(String name, String description, String color, int targetDays) {
        this.name = name;
        this.description = description;
        this.color = color;
        this.targetDays = targetDays;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(color);
        parcel.writeInt(targetDays);
        parcel.writeInt(currentStreak);
        parcel.writeBooleanArray(completedDays);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getTargetDays() {
        return targetDays;
    }

    public void setTargetDays(int targetDays) {
        this.targetDays = targetDays;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public boolean[] getCompletedDays() {
        return completedDays;
    }

    public void setCompletedDays(boolean[] completedDays) {
        this.completedDays = completedDays;
    }

    public void setDayCompleted(int dayIndex, boolean completed) {
        if (dayIndex >= 0 && dayIndex < completedDays.length) {
            completedDays[dayIndex] = completed;
        }
    }

    public String getLastCompletedDate() {
        return lastCompletedDate;
    }

    public void setLastCompletedDate(String lastCompletedDate) {
        this.lastCompletedDate = lastCompletedDate;
    }

    public boolean isDayCompleted(int dayIndex) {
        if (dayIndex >= 0 && dayIndex < completedDays.length) {
            return completedDays[dayIndex];
        }
        return false;
    }

    public boolean isTodayCompleted() {
        return !lastCompletedDate.isEmpty() && LocalDate.now().isEqual(LocalDate.parse(lastCompletedDate));
    }


    public static final Creator<HabitModel> CREATOR = new Creator<>() {
        @Override
        public HabitModel createFromParcel(Parcel parcel) {
            return new HabitModel(parcel);
        }

        @Override
        public HabitModel[] newArray(int size) {
            return new HabitModel[size];
        }
    };
}
