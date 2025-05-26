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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_pendidikan.adapter.DefinitionAdapter;
import com.example.project_pendidikan.api.ApiClient;
import com.example.project_pendidikan.api.DatamuseService;
import com.example.project_pendidikan.db.AppDatabase;
import com.example.project_pendidikan.model.DatamuseWord;
import com.example.project_pendidikan.model.Definition;
import com.example.project_pendidikan.model.FavoriteWord;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

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
    private MaterialButton btnRefresh;
    private AppDatabase database;
    private ExecutorService executorService;
    private Handler uiHandler;
    
    private static final int MSG_UPDATE_WORD_LIST = 1;
    private static final int MSG_UPDATE_FAVORITE = 2;
    private static final int MSG_SHOW_ERROR = 3;
    private String currentWord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize views
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize UI components
        editTextSearch = findViewById(R.id.editTextSearch);
        cardViewResult = findViewById(R.id.cardViewResult);
        textViewWord = findViewById(R.id.textViewWord);
        textViewPhonetic = findViewById(R.id.textViewPhonetic);
        recyclerViewDefinitions = findViewById(R.id.recyclerViewDefinitions);
        chipGroupSynonyms = findViewById(R.id.chipGroupSynonyms);
        fabFavorite = findViewById(R.id.fabFavorite);
        btnRefresh = findViewById(R.id.btnRefresh);

        // Initialize database and services
        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
        datamuseService = ApiClient.getClient().create(DatamuseService.class);

        // Setup refresh button
        btnRefresh.setOnClickListener(v -> {
            if (currentWord != null) {
                searchWord(currentWord);
            }
        });
        btnRefresh.setVisibility(View.GONE);

        // Initialize RecyclerView
        recyclerViewDefinitions.setLayoutManager(new LinearLayoutManager(this));
        definitionAdapter = new DefinitionAdapter(new ArrayList<>());
        recyclerViewDefinitions.setAdapter(definitionAdapter);

        // Setup search input
        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String searchTerm = editTextSearch.getText().toString().trim();
                if (!searchTerm.isEmpty()) {
                    searchWord(searchTerm);
                }
                return true;
            }
            return false;
        });

        // Setup favorite button
        fabFavorite.setOnClickListener(v -> toggleFavorite());
        fabFavorite.hide();

        // Initialize favorites launcher
        favoritesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Handle any updates needed after returning from favorites
                    }
                });

        // Setup bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                return true;
            } else if (itemId == R.id.navigation_favorite) {
                Intent favoriteIntent = new Intent(DashboardActivity.this, FavoriteWordsActivity.class);
                favoritesLauncher.launch(favoriteIntent);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                Intent profileIntent = new Intent(DashboardActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
                return true;
            }
            return false;
        });

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
        
        // Initialize Handler for UI updates
        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_UPDATE_WORD_LIST:
                        definitionAdapter.notifyDataSetChanged();
                        break;
                    case MSG_UPDATE_FAVORITE:
                        int position = msg.arg1;
                        boolean isFavorite = msg.arg2 == 1;
                        updateFavoriteUI(position, isFavorite);
                        break;
                    case MSG_SHOW_ERROR:
                        String error = (String) msg.obj;
                        Toast.makeText(DashboardActivity.this, error, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_favorites) {
            Intent intent = new Intent(this, FavoriteWordsActivity.class);
            favoritesLauncher.launch(intent);
            return true;
        } else if (item.getItemId() == R.id.action_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleFavorite() {
        if (currentWord == null || currentWord.isEmpty()) {
            return;
        }

        executorService.execute(() -> {
            try {
                FavoriteWord existingFavorite = database.favoriteWordDao().findByWord(currentWord);
                if (existingFavorite != null) {
                    // Remove from favorites
                    database.favoriteWordDao().delete(existingFavorite);
                    runOnUiThread(() -> {
                        fabFavorite.setImageResource(android.R.drawable.btn_star_big_off);
                        Toast.makeText(DashboardActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // Add to favorites
                    String definition = "";
                    if (definitionAdapter != null && !definitionAdapter.getDefinitions().isEmpty()) {
                        definition = definitionAdapter.getDefinitions().get(0).getDefinition();
                    }
                    String phonetic = textViewPhonetic.getVisibility() == View.VISIBLE ? 
                            textViewPhonetic.getText().toString() : "";
                    
                    FavoriteWord newFavorite = new FavoriteWord(currentWord, definition, phonetic);
                    database.favoriteWordDao().insert(newFavorite);
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

    private void searchWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }

        currentWord = word.trim();

        if (!isNetworkAvailable()) {
            showError("No internet connection");
            btnRefresh.setVisibility(View.VISIBLE);
            return;
        }

        btnRefresh.setVisibility(View.GONE);

        // Show loading state
        cardViewResult.setVisibility(View.GONE);
        fabFavorite.hide();

        // Call API
        Call<List<DatamuseWord>> call = datamuseService.getRelatedWords(word, "d");
        call.enqueue(new Callback<List<DatamuseWord>>() {
            @Override
            public void onResponse(Call<List<DatamuseWord>> call, Response<List<DatamuseWord>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayWordDetails(word, response.body());
                    getSynonyms(word);
                } else {
                    showError("No definitions found");
                }
            }

            @Override
            public void onFailure(Call<List<DatamuseWord>> call, Throwable t) {
                showError("Error: " + t.getMessage());
            }
        });

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
            
            if (message.contains("internet") && currentWord != null && !currentWord.isEmpty()) {
                btnRefresh.setVisibility(View.VISIBLE);
            } else {
                btnRefresh.setVisibility(View.GONE);
            }
        });
    }

    private void updateFavoriteUI(int position, boolean isFavorite) {
        definitionAdapter.notifyItemChanged(position);
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
