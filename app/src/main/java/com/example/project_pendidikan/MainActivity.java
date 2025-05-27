package com.example.project_pendidikan;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.SharedPreferences;

import com.example.project_pendidikan.db.DatabaseHelper;

public class MainActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private DatabaseHelper databaseHelper;
    private static final String THEME_PREFS = "ThemePrefs";
    private static final String KEY_NIGHT_MODE = "night_mode";
    private static final String PREF_NAME = "UserPref";
    private static final String KEY_EMAIL = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate
        SharedPreferences themePreferences = getSharedPreferences(THEME_PREFS, MODE_PRIVATE);
        boolean isNightMode = themePreferences.getBoolean(KEY_NIGHT_MODE, false);
        AppCompatDelegate.setDefaultNightMode(isNightMode ? 
            AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize DatabaseHelper first
        databaseHelper = new DatabaseHelper(this);

        // Check if user is already logged in
        SharedPreferences userPrefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String savedEmail = userPrefs.getString(KEY_EMAIL, "");
        if (!savedEmail.isEmpty() && databaseHelper.isEmailExists(savedEmail)) {
            // User is already logged in, go to dashboard
            String userName = databaseHelper.getUserName(savedEmail);
            startDashboard(userName, savedEmail);
            finish();
            return;
        }

        // Initialize views only if not auto-logging in
        initializeViews();
    }

    private void initializeViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        Button buttonViewUsers = findViewById(R.id.buttonViewUsers);
        buttonViewUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UserListActivity.class));
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Please enter email");
            editTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Please enter password");
            editTextPassword.requestFocus();
            return;
        }

        if (databaseHelper.checkUser(email, password)) {
            // Save user email in SharedPreferences
            SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
            editor.putString(KEY_EMAIL, email);
            editor.apply();

            // Get user name and start dashboard
            String userName = databaseHelper.getUserName(email);
            startDashboard(userName, email);
            finish();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void startDashboard(String userName, String email) {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        intent.putExtra("USER_NAME", userName);
        intent.putExtra("USER_EMAIL", email);
        startActivity(intent);
    }
}