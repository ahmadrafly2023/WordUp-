package com.example.project_pendidikan.db;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migration_1_2 extends Migration {
    public Migration_1_2() {
        super(1, 2);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        // Add synonyms column to favorite_words table
        database.execSQL("ALTER TABLE favorite_words ADD COLUMN synonyms TEXT DEFAULT ''");
    }
} 