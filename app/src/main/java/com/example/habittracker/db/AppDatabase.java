package com.example.habittracker.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.habittracker.db.dao.CategoryDao;
import com.example.habittracker.db.dao.HabitCompletionDao;
import com.example.habittracker.db.dao.HabitDao;
import com.example.habittracker.db.dao.ReminderDao;
import com.example.habittracker.db.entity.Category;
import com.example.habittracker.db.entity.Converters;
import com.example.habittracker.db.entity.FrequencyType;
import com.example.habittracker.db.entity.HabitCompletion;
import com.example.habittracker.db.entity.HabitModel;
import com.example.habittracker.db.entity.Reminder;
import com.example.habittracker.util.Utils;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {HabitModel.class, HabitCompletion.class, Category.class, Reminder.class},
        exportSchema = true, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public static final String DB_NAME = "habit_database.db";
    public static final Object LOCK = new Object();
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    public static AppDatabase instance;
    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
                /*db.execSQL("insert into categories (name, color) values ('Health', 16731311)");
                db.execSQL("insert into categories (name, color) values ('Work', 16751655)");
                db.execSQL("insert into categories (name, color) values ('Personal', 16777195)");*/

                CategoryDao categoryDao = instance.categoryDao();
                Utils.categories().forEach(c -> categoryDao.insert(c));

                HabitDao dao = instance.habitDao();

                HabitModel habitModel1 = new HabitModel();
                habitModel1.setName("Зарядка");
                habitModel1.setDescription("Утренняя зарядка 15 минут");
                habitModel1.setColor("#B5EAD7");
                habitModel1.setTargetDays(21);
                habitModel1.setCreatedAt(LocalDateTime.now());
                habitModel1.setFrequencyType(FrequencyType.DAILY);
                habitModel1.categoryId = 1L;

                HabitModel habitModel2 = new HabitModel();
                habitModel2.setName("Пить воду");
                habitModel2.setDescription("Выпивать 2 литра воды в день");
                habitModel2.setColor("#AEC6CF");
                habitModel2.setTargetDays(30);
                habitModel2.setCreatedAt(LocalDateTime.now());
                habitModel2.setFrequencyType(FrequencyType.DAILY);
                habitModel2.categoryId = 3L;

                dao.insert(habitModel1);
                dao.insert(habitModel2);
            });
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME)
                            .addCallback(sRoomDatabaseCallback)
//                            .fallbackToDestructiveMigration(true)
                            .build();
                }
            }
        }
        return instance;
    }

    public abstract HabitDao habitDao();

    public abstract HabitCompletionDao habitCompletionDao();

    public abstract CategoryDao categoryDao();

    public abstract ReminderDao reminderDao();
}
