package com.example.project_pendidikan;

public class Definition {
    private String partOfSpeech;
    private String definition;

    public Definition(String partOfSpeech, String definition) {
        this.partOfSpeech = partOfSpeech;
        this.definition = definition;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public String getDefinition() {
        return definition;
    }
}
