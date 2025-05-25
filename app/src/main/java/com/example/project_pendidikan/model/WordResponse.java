package com.example.project_pendidikan.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WordResponse {
    @SerializedName("word")
    private String word;

    @SerializedName("pronunciation")
    private Pronunciation pronunciation;

    @SerializedName("results")
    private List<Result> results;

    public static class Pronunciation {
        @SerializedName("all")
        private String all;

        public String getAll() {
            return all;
        }
    }

    public static class Result {
        @SerializedName("definition")
        private String definition;

        @SerializedName("partOfSpeech")
        private String partOfSpeech;

        @SerializedName("synonyms")
        private List<String> synonyms;

        @SerializedName("examples")
        private List<String> examples;

        public String getDefinition() {
            return definition;
        }

        public String getPartOfSpeech() {
            return partOfSpeech;
        }

        public List<String> getSynonyms() {
            return synonyms;
        }

        public List<String> getExamples() {
            return examples;
        }
    }

    public String getWord() {
        return word;
    }

    public Pronunciation getPronunciation() {
        return pronunciation;
    }

    public List<Result> getResults() {
        return results;
    }
}
