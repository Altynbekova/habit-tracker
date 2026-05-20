package com.example.habittracker.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.habittracker.Util;
import com.example.habittracker.db.dao.CategoryDao;
import com.example.habittracker.db.entity.Category;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class CategoryDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private AppDatabase db;
    private CategoryDao categoryDao;

    @Before
    public void createDb() {
        db = Room.inMemoryDatabaseBuilder(
                        ApplicationProvider.getApplicationContext(),
                        AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        categoryDao = db.categoryDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void insertAndGetAllCategories_returnsOrderedList() throws InterruptedException {
        Category sports = new Category("Спорт", "ic_sport");
        Category health = new Category("Здоровье", "ic_health");

        long id1 = categoryDao.insert(sports);
        long id2 = categoryDao.insert(health);

        LiveData<List<Category>> liveData = categoryDao.getAllCategories();
        List<Category> categories = Util.getValueOrAwait(liveData);

        assertEquals(2, categories.size());
        assertEquals(id1, categories.get(0).getId());
        assertEquals("Спорт", categories.get(0).getName());
        assertEquals(id2, categories.get(1).getId());
        assertEquals("Здоровье", categories.get(1).getName());
    }

    @Test
    public void deleteCategory_removesFromDatabase() throws InterruptedException {
        Category category = new Category("Образование", "ic_edu");
        long categoryId = categoryDao.insert(category);
        category.setId(categoryId);

        List<Category> beforeDelete = Util.getValueOrAwait(categoryDao.getAllCategories());
        assertEquals(1, beforeDelete.size());

        categoryDao.delete(category);

        List<Category> afterDelete = Util.getValueOrAwait(categoryDao.getAllCategories());
        assertTrue(afterDelete.isEmpty());
    }
}
