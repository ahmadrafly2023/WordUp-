package com.example.project_pendidikan;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_pendidikan.adapter.DefinitionAdapter;
import com.example.project_pendidikan.api.ApiClient;
import com.example.project_pendidikan.api.DatamuseService;
import com.example.project_pendidikan.db.AppDatabase;
import com.example.project_pendidikan.model.DatamuseWord;
import com.example.project_pendidikan.model.Definition;
import com.example.project_pendidikan.model.FavoriteWord;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_FAVORITES = 1;

    private TextInputEditText editTextSearch;
    private CardView cardViewResult;
    private TextView textViewWord;
    private TextView textViewPhonetic;
    private RecyclerView recyclerViewDefinitions;
    private ChipGroup chipGroupSynonyms;
    private DefinitionAdapter definitionAdapter;
    private DatamuseService datamuseService;
    private FloatingActionButton fabFavorite;
    private AppDatabase database;
    private ExecutorService executorService;
    private String currentWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize database and executor
        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // Initialize views
        editTextSearch = findViewById(R.id.editTextSearch);
        cardViewResult = findViewById(R.id.cardViewResult);
        textViewWord = findViewById(R.id.textViewWord);
        textViewPhonetic = findViewById(R.id.textViewPhonetic);
        recyclerViewDefinitions = findViewById(R.id.recyclerViewDefinitions);
        chipGroupSynonyms = findViewById(R.id.chipGroupSynonyms);
        fabFavorite = findViewById(R.id.fabFavorite);

        // Setup FAB
        fabFavorite.setOnClickListener(v -> toggleFavorite());

        // Setup RecyclerView
        recyclerViewDefinitions.setLayoutManager(new LinearLayoutManager(this));
        definitionAdapter = new DefinitionAdapter(new ArrayList<>());
        recyclerViewDefinitions.setAdapter(definitionAdapter);

        // Initialize API service
        datamuseService = ApiClient.getClient().create(DatamuseService.class);

        // Setup search
        editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String word = editTextSearch.getText().toString().trim();
                    if (!word.isEmpty()) {
                        searchWord(word);
                    }
                    return true;
                }
                return false;
            }
        });

        // Setup toolbar menu
        toolbar.setOnMenuItemClickListener(new MaterialToolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_logout) {
            // Handle logout
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_favorites) {
            // Open favorites
            Intent intent = new Intent(this, FavoriteWordsActivity.class);
            startActivityForResult(intent, REQUEST_CODE_FAVORITES);
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FAVORITES && resultCode == RESULT_OK && data != null) {
            String wordToSearch = data.getStringExtra("WORD_TO_SEARCH");
            if (wordToSearch != null) {
                editTextSearch.setText(wordToSearch);
                searchWord(wordToSearch);
            }
        }
    }

    private void toggleFavorite() {
        if (currentWord == null) return;

        executorService.execute(() -> {
            FavoriteWord existingWord = database.favoriteWordDao().findByWord(currentWord);
            if (existingWord != null) {
                // Remove from favorites
                database.favoriteWordDao().delete(existingWord);
                runOnUiThread(() -> {
                    fabFavorite.setImageResource(android.R.drawable.btn_star_big_off);
                    Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                });
            } else {
                // Add to favorites
                List<Definition> definitions = definitionAdapter.getDefinitions();
            String definition = definitions != null && !definitions.isEmpty() ? definitions.get(0).getDefinition() : "";
                String phonetic = textViewPhonetic.getVisibility() == View.VISIBLE ? 
                        textViewPhonetic.getText().toString() : "";
                FavoriteWord newFavorite = new FavoriteWord(currentWord, definition, phonetic);
                database.favoriteWordDao().insert(newFavorite);
                runOnUiThread(() -> {
                    fabFavorite.setImageResource(android.R.drawable.btn_star_big_on);
                    Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void searchWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            showError("Please enter a word to search");
            return;
        }

        if (!isNetworkAvailable()) {
            showError("No internet connection. Please check your network settings.");
            return;
        }

        // Show loading state
        cardViewResult.setVisibility(View.GONE);
        fabFavorite.hide();

        try {
            // Search for word definitions with metadata
            Call<List<DatamuseWord>> relatedCall = datamuseService.getRelatedWords(word.trim(), "d");
            relatedCall.enqueue(new Callback<List<DatamuseWord>>() {
                @Override
                public void onResponse(Call<List<DatamuseWord>> call, Response<List<DatamuseWord>> response) {
                    if (response != null && response.isSuccessful()) {
                        List<DatamuseWord> words = response.body();
                        if (words != null && !words.isEmpty()) {
                            displayWordDetails(word.trim(), words);
                            getSynonyms(word.trim());
                            cardViewResult.setVisibility(View.VISIBLE);
                        } else {
                            showError("No definitions found for: " + word);
                        }
                    } else {
                        showError("Error: " + (response != null ? response.message() : "Unknown error"));
                    }
                }

                @Override
                public void onFailure(Call<List<DatamuseWord>> call, Throwable t) {
                    showError("Network error: Please check your internet connection");
                }
            });
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    private void getSynonyms(String word) {
        if (word == null || word.trim().isEmpty()) return;

        try {
            Call<List<DatamuseWord>> call = datamuseService.getSynonyms(word.trim(), "d");
            call.enqueue(new Callback<List<DatamuseWord>>() {
                @Override
                public void onResponse(Call<List<DatamuseWord>> call, Response<List<DatamuseWord>> response) {
                    if (response != null && response.isSuccessful() && response.body() != null) {
                        displaySynonyms(response.body());
                    } else {
                        chipGroupSynonyms.removeAllViews();
                    }
                }

                @Override
                public void onFailure(Call<List<DatamuseWord>> call, Throwable t) {
                    chipGroupSynonyms.removeAllViews();
                }
            });
        } catch (Exception e) {
            chipGroupSynonyms.removeAllViews();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText(DashboardActivity.this, message, Toast.LENGTH_LONG).show();
            cardViewResult.setVisibility(View.GONE);
            fabFavorite.hide();
        });
    }

    private void displayWordDetails(String searchWord, List<DatamuseWord> words) {
        try {
            if (searchWord == null || words == null) {
                showError("Invalid word data");
                return;
            }

            cardViewResult.setVisibility(View.VISIBLE);
            fabFavorite.show();
            currentWord = searchWord;
            
            // Set word
            textViewWord.setText(searchWord);
            textViewPhonetic.setVisibility(View.GONE);

            // Check if word is favorite
            executorService.execute(() -> {
                try {
                    FavoriteWord favoriteWord = database.favoriteWordDao().findByWord(currentWord);
                    runOnUiThread(() -> {
                        fabFavorite.setImageResource(favoriteWord != null ? 
                            android.R.drawable.btn_star_big_on : 
                            android.R.drawable.btn_star_big_off);
                    });
                } catch (Exception e) {
                    showError("Database error: " + e.getMessage());
                }
            });

            // Set definitions
            List<Definition> definitions = new ArrayList<>();
            for (DatamuseWord word : words) {
                if (word != null && word.getDefinitions() != null) {
                    for (String def : word.getDefinitions()) {
                        if (def != null) {
                            try {
                                // Parse definition string (format: "pos\tdef")
                                String[] parts = def.split("\t");
                                String partOfSpeech = parts.length >= 2 ? parts[0] : "";
                                String definitionText = parts.length >= 2 ? parts[1] : def;
                                Definition definition = new Definition(partOfSpeech, definitionText, "");
                                definitions.add(definition);
                            } catch (Exception e) {
                                // Skip invalid definition
                                continue;
                            }
                        }
                    }
                }
            }
            definitionAdapter.setDefinitions(definitions);
        } catch (Exception e) {
            showError("Error displaying word details: " + e.getMessage());
        }
    }

    private void displaySynonyms(List<DatamuseWord> synonyms) {
        if (synonyms == null) return;

        runOnUiThread(() -> {
            try {
                chipGroupSynonyms.removeAllViews();
                for (DatamuseWord synonym : synonyms) {
                    if (synonym != null && synonym.getWord() != null) {
                        Chip chip = new Chip(DashboardActivity.this);
                        chip.setText(synonym.getWord());
                        chip.setClickable(true);
                        chip.setCheckable(false);
                        chip.setOnClickListener(v -> {
                            editTextSearch.setText(synonym.getWord());
                            searchWord(synonym.getWord());
                        });
                        chipGroupSynonyms.addView(chip);
                    }
                }
            } catch (Exception e) {
                showError("Error displaying synonyms: " + e.getMessage());
            }
        });
    }
}
