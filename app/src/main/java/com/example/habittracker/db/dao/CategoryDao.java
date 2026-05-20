package com.example.habittracker.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.habittracker.db.entity.Category;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    long insert(Category category);

    @Query("SELECT * FROM categories ORDER BY id ASC")
    LiveData<List<Category>> getAllCategories();

    @Delete
    void delete(Category category);
}
