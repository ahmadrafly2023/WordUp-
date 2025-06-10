package com.example.project_pendidikan;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class FavoriteDetailActivity extends AppCompatActivity {
    public static final String EXTRA_WORD = "extra_word";
    public static final String EXTRA_DEFINITION = "extra_definition";
    public static final String EXTRA_PHONETIC = "extra_phonetic";
    public static final String EXTRA_SYNONYMS = "extra_synonyms";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_detail);


        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        TextView textViewWord = findViewById(R.id.textViewWord);
        TextView textViewPhonetic = findViewById(R.id.textViewPhonetic);
        TextView textViewDefinition = findViewById(R.id.textViewDefinition);
        ChipGroup chipGroupSynonyms = findViewById(R.id.chipGroupSynonyms);
        TextView labelSynonyms = findViewById(R.id.labelSynonyms);
        MaterialButton buttonSearch = findViewById(R.id.buttonSearch);


        String word = getIntent().getStringExtra(EXTRA_WORD);
        String definition = getIntent().getStringExtra(EXTRA_DEFINITION);
        String phonetic = getIntent().getStringExtra(EXTRA_PHONETIC);
        String synonyms = getIntent().getStringExtra(EXTRA_SYNONYMS);


        textViewWord.setText(word);
        textViewPhonetic.setText(phonetic);
        textViewDefinition.setText(definition);


        if (synonyms != null && !synonyms.isEmpty()) {
            String[] synonymArray = synonyms.split(",");
            for (String synonym : synonymArray) {
                Chip chip = new Chip(this);
                chip.setText(synonym.trim());
                chip.setClickable(true);
                chip.setCheckable(false);
                chip.setOnClickListener(v -> {
                    if (isNetworkAvailable()) {
                        Intent intent = new Intent(this, DashboardActivity.class);
                        intent.putExtra("WORD_TO_SEARCH", synonym.trim());
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "No internet connection available to search for new words", Toast.LENGTH_SHORT).show();
                    }
                });
                chipGroupSynonyms.addView(chip);
            }
            labelSynonyms.setVisibility(View.VISIBLE);
            chipGroupSynonyms.setVisibility(View.VISIBLE);
        } else {
            labelSynonyms.setVisibility(View.GONE);
            chipGroupSynonyms.setVisibility(View.GONE);
        }


        getSupportActionBar().setTitle(word);


        buttonSearch.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.putExtra("WORD_TO_SEARCH", word);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "No internet connection available to search for new words", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 