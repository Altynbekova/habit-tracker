package com.example.habittracker.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    @ColumnInfo(index = true)
    public String name;
    public String icon;

    public Category() {
    }

    public Category(@NonNull String name, String icon) {
        this.name = name;
        this.icon = icon;
    }
}

