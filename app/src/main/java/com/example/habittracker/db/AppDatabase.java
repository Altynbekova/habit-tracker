package com.example.habittracker.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {HabitModel.class}, exportSchema = false, version = 1)
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
                HabitDao dao = instance.habitDao();

                HabitModel habitModel1 = new HabitModel();
                habitModel1.setName("Зарядка");
                habitModel1.setDescription("Утренняя зарядка 15 минут");
                habitModel1.setColor("#B5EAD7");
                habitModel1.setTargetDays(21);
                habitModel1.setCompletedDays(new boolean[7]);

                HabitModel habitModel2 = new HabitModel();
                habitModel2.setName("Пить воду");
                habitModel2.setDescription("Выпивать 2 литра воды в день");
                habitModel2.setColor("#AEC6CF");
                habitModel2.setTargetDays(30);
                habitModel2.setCompletedDays(new boolean[7]);

                dao.insertHabit(habitModel1);
                dao.insertHabit(habitModel2);
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
                            .build();
                }
            }
        }
        return instance;
    }

    public abstract HabitDao habitDao();
}
