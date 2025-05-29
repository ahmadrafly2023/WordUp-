package com.example.project_pendidikan.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.project_pendidikan.model.FavoriteWord;

import java.util.List;

@Dao
public interface FavoriteWordDao {
    @Insert
    void insert(FavoriteWord word);

    @Delete
    void delete(FavoriteWord word);

    @Query("SELECT * FROM favorite_words ORDER BY timestamp DESC")
    LiveData<List<FavoriteWord>> getAllFavoriteWords();

    @Query("SELECT * FROM favorite_words ORDER BY timestamp DESC")
    List<FavoriteWord> getAllFavorites();
    
    @Query("SELECT * FROM favorite_words WHERE userEmail = :userEmail ORDER BY timestamp DESC")
    List<FavoriteWord> getFavoritesByUser(String userEmail);

    @Query("SELECT * FROM favorite_words WHERE word LIKE :word LIMIT 1")
    FavoriteWord findByWord(String word);

    @Query("DELETE FROM favorite_words WHERE word = :word")
    void deleteByWord(String word);
}
