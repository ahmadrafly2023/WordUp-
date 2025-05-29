package com.example.project_pendidikan.model;

public class UserProgress {
    private String userEmail;
    private int level;
    private int totalCorrectAnswers;
    private int quizzesTaken;
    private int currentStreak;
    
    public UserProgress() {
        this.userEmail = "";
        this.level = 1;
        this.totalCorrectAnswers = 0;
        this.quizzesTaken = 0;
        this.currentStreak = 0;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public int getTotalCorrectAnswers() {
        return totalCorrectAnswers;
    }
    
    public void setTotalCorrectAnswers(int totalCorrectAnswers) {
        this.totalCorrectAnswers = totalCorrectAnswers;
    }
    
    public int getQuizzesTaken() {
        return quizzesTaken;
    }
    
    public void setQuizzesTaken(int quizzesTaken) {
        this.quizzesTaken = quizzesTaken;
    }
    
    public int getCurrentStreak() {
        return currentStreak;
    }
    
    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }
    
    public void incrementCorrectAnswers() {
        this.totalCorrectAnswers++;
        this.currentStreak++;
        updateLevel();
    }
    
    public void resetStreak() {
        this.currentStreak = 0;
    }
    
    public void incrementQuizzesTaken() {
        this.quizzesTaken++;
    }
    
    private void updateLevel() {
        // Level up every 10 correct answers
        this.level = (this.totalCorrectAnswers / 10) + 1;
    }
}
