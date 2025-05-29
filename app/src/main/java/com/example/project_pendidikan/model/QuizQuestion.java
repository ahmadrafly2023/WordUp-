package com.example.project_pendidikan.model;

import java.util.List;

public class QuizQuestion {
    private String word;
    private String correctDefinition;
    private List<String> options;
    
    public QuizQuestion(String word, String correctDefinition, List<String> options) {
        this.word = word;
        this.correctDefinition = correctDefinition;
        this.options = options;
    }
    
    public String getWord() {
        return word;
    }
    
    public void setWord(String word) {
        this.word = word;
    }
    
    public String getCorrectDefinition() {
        return correctDefinition;
    }
    
    public void setCorrectDefinition(String correctDefinition) {
        this.correctDefinition = correctDefinition;
    }
    
    public List<String> getOptions() {
        return options;
    }
    
    public void setOptions(List<String> options) {
        this.options = options;
    }
    
    public boolean isCorrectAnswer(String selectedAnswer) {
        return correctDefinition.equals(selectedAnswer);
    }
}
