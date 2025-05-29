package com.example.project_pendidikan.db;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migration_2_3 extends Migration {
    public Migration_2_3() {
        super(2, 3);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        // Add userEmail column to favorite_words table
        database.execSQL("ALTER TABLE favorite_words ADD COLUMN userEmail TEXT DEFAULT ''");
    }
}
