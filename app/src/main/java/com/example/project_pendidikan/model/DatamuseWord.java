package com.example.project_pendidikan.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DatamuseWord {
    @SerializedName("word")
    private String word;

    @SerializedName("score")
    private int score;

    @SerializedName("tags")
    private List<String> tags;

    @SerializedName("defs")
    private List<String> definitions;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<String> definitions) {
        this.definitions = definitions;
    }
}
