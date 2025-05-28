package com.example.project_pendidikan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.project_pendidikan.db.DatabaseHelper;

public class SplashActivity extends AppCompatActivity {
    private static final String THEME_PREFS = "ThemePrefs";
    private static final String KEY_NIGHT_MODE = "night_mode";
    private static final String PREF_NAME = "UserPref";
    private static final String KEY_EMAIL = "email";
    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate
        SharedPreferences themePreferences = getSharedPreferences(THEME_PREFS, MODE_PRIVATE);
        boolean isNightMode = themePreferences.getBoolean(KEY_NIGHT_MODE, false);
        AppCompatDelegate.setDefaultNightMode(isNightMode ? 
            AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                checkLoginStatus();
            }
        }, SPLASH_DELAY);
    }

    private void checkLoginStatus() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SharedPreferences userPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String savedEmail = userPrefs.getString(KEY_EMAIL, "");

        Intent intent;
        if (!savedEmail.isEmpty() && databaseHelper.isEmailExists(savedEmail)) {
            // User is already logged in
            String userName = databaseHelper.getUserName(savedEmail);
            intent = new Intent(SplashActivity.this, DashboardActivity.class);
            intent.putExtra("USER_NAME", userName);
            intent.putExtra("USER_EMAIL", savedEmail);
        } else {
            // User needs to login
            intent = new Intent(SplashActivity.this, MainActivity.class);
        }

        startActivity(intent);
        finish();
    }
} 