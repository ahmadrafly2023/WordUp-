package com.example.project_pendidikan.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_words")
public class FavoriteWord {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String word;
    private String definition;
    private String phonetic;
    private long timestamp;

    public FavoriteWord(String word, String definition, String phonetic) {
        this.word = word;
        this.definition = definition;
        this.phonetic = phonetic;
        this.timestamp = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getPhonetic() {
        return phonetic;
    }

    public void setPhonetic(String phonetic) {
        this.phonetic = phonetic;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
