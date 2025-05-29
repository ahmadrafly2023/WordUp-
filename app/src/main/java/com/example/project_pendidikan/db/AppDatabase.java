package com.example.project_pendidikan.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.project_pendidikan.model.FavoriteWord;

@Database(entities = {FavoriteWord.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    
    public abstract FavoriteWordDao favoriteWordDao();
    
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "vocabulary_db")
                    .addMigrations(new Migration_1_2(), new Migration_2_3())
                    .build();
        }
        return instance;
    }
}
