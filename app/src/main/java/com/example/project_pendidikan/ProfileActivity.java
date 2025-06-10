package com.example.project_pendidikan;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.project_pendidikan.db.DatabaseHelper;
import com.google.android.material.appbar.MaterialToolbar;

public class ProfileActivity extends AppCompatActivity {
    private TextInputEditText editTextName, editTextEmail, editTextPassword;
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "UserPref";
    private static final String KEY_EMAIL = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        databaseHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        MaterialButton buttonSave = findViewById(R.id.buttonSave);


        loadUserData();

        buttonSave.setOnClickListener(v -> saveChanges());
    }

    private void loadUserData() {
        String userEmail = sharedPreferences.getString(KEY_EMAIL, "");
        if (!userEmail.isEmpty()) {
            editTextEmail.setText(userEmail);
            String userName = databaseHelper.getUserName(userEmail);
            if (!userName.isEmpty()) {
                editTextName.setText(userName);
            }
        }
    }

    private void saveChanges() {
        String currentEmail = sharedPreferences.getString(KEY_EMAIL, "");
        String newName = String.valueOf(editTextName.getText());
        String newEmail = String.valueOf(editTextEmail.getText());
        String newPassword = String.valueOf(editTextPassword.getText());

        if (newName.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(this, "Name and email are required", Toast.LENGTH_SHORT).show();
            return;
        }


        if (!newEmail.equals(currentEmail) && databaseHelper.isEmailExists(newEmail)) {
            Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show();
            return;
        }


        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", newName);
        values.put("email", newEmail);
        if (!newPassword.isEmpty()) {
            values.put("password", newPassword);
        }

        int rowsAffected = db.update("users", values, "email = ?", new String[]{currentEmail});
        db.close();

        if (rowsAffected > 0) {
            // Update shared preferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_EMAIL, newEmail);
            editor.apply();

            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }
}
