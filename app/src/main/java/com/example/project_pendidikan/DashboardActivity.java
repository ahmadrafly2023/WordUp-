package com.example.project_pendidikan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private ActivityResultLauncher<Intent> favoritesLauncher;

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

        // Initialize views
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // Initialize activity result launcher
        favoritesLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String wordToSearch = result.getData().getStringExtra("WORD_TO_SEARCH");
                    if (wordToSearch != null) {
                        editTextSearch.setText(wordToSearch);
                        searchWord(wordToSearch);
                    }
                }
            });

        editTextSearch = findViewById(R.id.editTextSearch);
        cardViewResult = findViewById(R.id.cardViewResult);
        textViewWord = findViewById(R.id.textViewWord);
        textViewPhonetic = findViewById(R.id.textViewPhonetic);
        chipGroupSynonyms = findViewById(R.id.chipGroupSynonyms);
        recyclerViewDefinitions = findViewById(R.id.recyclerViewDefinitions);
        fabFavorite = findViewById(R.id.fabFavorite);

        // Initialize database
        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        // Initialize adapter
        definitionAdapter = new DefinitionAdapter(new ArrayList<>());
        recyclerViewDefinitions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDefinitions.setAdapter(definitionAdapter);

        // Initialize API client
        datamuseService = ApiClient.getClient().create(DatamuseService.class);

        // Set up search
        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String word = editTextSearch.getText().toString();
                searchWord(word);
                return true;
            }
            return false;
        });

        // Set up favorite button
        fabFavorite.setOnClickListener(v -> {
            if (currentWord != null && !currentWord.isEmpty()) {
                executorService.execute(() -> {
                    try {
                        FavoriteWord existingWord = database.favoriteWordDao().findByWord(currentWord);
                        if (existingWord != null) {
                            // Remove from favorites
                            database.favoriteWordDao().delete(existingWord);
                            runOnUiThread(() -> {
                                fabFavorite.setImageResource(android.R.drawable.btn_star_big_off);
                                Toast.makeText(DashboardActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            // Add to favorites
                            String definition = definitionAdapter.getDefinitions().isEmpty() ? "" : 
                                    definitionAdapter.getDefinitions().get(0).getDefinition();
                            FavoriteWord favoriteWord = new FavoriteWord(
                                    currentWord,
                                    definition,
                                    textViewPhonetic.getText().toString());
                            database.favoriteWordDao().insert(favoriteWord);
                            runOnUiThread(() -> {
                                fabFavorite.setImageResource(android.R.drawable.btn_star_big_on);
                                Toast.makeText(DashboardActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        showError("Error updating favorites: " + e.getMessage());
                    }
                });
            }
        });

        // Set up toolbar menu item click
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_favorites) {
                try {
                    Intent intent = new Intent(DashboardActivity.this, FavoriteWordsActivity.class);
                    favoritesLauncher.launch(intent);
                    return true;
                } catch (Exception e) {
                    showError("Error opening favorites: " + e.getMessage());
                    return false;
                }
            }
            return false;
        });

        // Hide results initially
        cardViewResult.setVisibility(View.GONE);
        fabFavorite.hide();
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
            favoritesLauncher.launch(intent);
            return true;
        }
        return false;
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
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
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
