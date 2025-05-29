package com.example.project_pendidikan;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.project_pendidikan.model.UserProgress;
import com.google.android.material.appbar.MaterialToolbar;

public class AchievementsActivity extends AppCompatActivity {

    private static final String PROGRESS_PREFS = "ProgressPrefs";
    
    private TextView textViewCurrentLevel;
    private ProgressBar progressBarLevel;
    private TextView textViewTotalCorrect;
    private TextView textViewQuizzesTaken;
    private TextView textViewCurrentStreak;
    private ImageView imageAchievement1;
    private ImageView imageAchievement2;
    private ImageView imageAchievement3;
    
    private UserProgress userProgress;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);
        
        // Initialize toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        // Initialize views
        textViewCurrentLevel = findViewById(R.id.textViewCurrentLevel);
        progressBarLevel = findViewById(R.id.progressBarLevel);
        textViewTotalCorrect = findViewById(R.id.textViewTotalCorrect);
        textViewQuizzesTaken = findViewById(R.id.textViewQuizzesTaken);
        textViewCurrentStreak = findViewById(R.id.textViewCurrentStreak);
        imageAchievement1 = findViewById(R.id.imageAchievement1);
        imageAchievement2 = findViewById(R.id.imageAchievement2);
        imageAchievement3 = findViewById(R.id.imageAchievement3);
        
        // Load user progress
        loadUserProgress();
        
        // Update UI
        updateUI();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when activity resumes
        loadUserProgress();
        updateUI();
    }
    
    private void loadUserProgress() {
        // Get user email from shared preferences
        SharedPreferences userPrefs = getSharedPreferences("UserPref", MODE_PRIVATE);
        String userEmail = userPrefs.getString("email", "");
        
        // Load progress using email as part of the key
        SharedPreferences prefs = getSharedPreferences(PROGRESS_PREFS, MODE_PRIVATE);
        userProgress = new UserProgress();
        userProgress.setUserEmail(userEmail);
        userProgress.setLevel(prefs.getInt(userEmail + "_level", 1));
        userProgress.setTotalCorrectAnswers(prefs.getInt(userEmail + "_totalCorrectAnswers", 0));
        userProgress.setQuizzesTaken(prefs.getInt(userEmail + "_quizzesTaken", 0));
        userProgress.setCurrentStreak(prefs.getInt(userEmail + "_currentStreak", 0));
    }
    
    private void updateUI() {
        // Update level information
        textViewCurrentLevel.setText("Level " + userProgress.getLevel());
        
        // Calculate progress to next level (every 10 correct answers is a level)
        int correctAnswersInCurrentLevel = userProgress.getTotalCorrectAnswers() % 10;
        int progressToNextLevel = correctAnswersInCurrentLevel * 10; // 0-100%
        progressBarLevel.setProgress(progressToNextLevel);
        
        // Update stats
        textViewTotalCorrect.setText(String.valueOf(userProgress.getTotalCorrectAnswers()));
        textViewQuizzesTaken.setText(String.valueOf(userProgress.getQuizzesTaken()));
        textViewCurrentStreak.setText(String.valueOf(userProgress.getCurrentStreak()));
        
        // Update achievements
        updateAchievements();
    }
    
    private void updateAchievements() {
        // Achievement 1: Complete first quiz
        if (userProgress.getQuizzesTaken() > 0) {
            imageAchievement1.setImageResource(android.R.drawable.checkbox_on_background);
            imageAchievement1.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        }
        
        // Achievement 2: Get 10 correct answers in a row
        if (userProgress.getCurrentStreak() >= 10) {
            imageAchievement2.setImageResource(android.R.drawable.checkbox_on_background);
            imageAchievement2.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        }
        
        // Achievement 3: Reach Level 5
        if (userProgress.getLevel() >= 5) {
            imageAchievement3.setImageResource(android.R.drawable.checkbox_on_background);
            imageAchievement3.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        }
    }
}
