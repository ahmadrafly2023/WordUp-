package com.example.project_pendidikan;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_pendidikan.adapter.FavoriteWordAdapter;
import com.example.project_pendidikan.db.AppDatabase;
import com.example.project_pendidikan.model.FavoriteWord;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoriteWordsActivity extends AppCompatActivity {
    private RecyclerView recyclerViewFavorites;
    private TextView textViewEmpty;
    private FavoriteWordAdapter adapter;
    private AppDatabase database;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_words);

        // Initialize views
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Favorite Words");
        }

        recyclerViewFavorites = findViewById(R.id.recyclerViewFavorites);
        textViewEmpty = findViewById(R.id.textViewEmpty);

        // Initialize database
        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        // Setup RecyclerView
        adapter = new FavoriteWordAdapter(
            // On item click listener
            word -> {
                Intent intent = new Intent(this, FavoriteDetailActivity.class);
                intent.putExtra(FavoriteDetailActivity.EXTRA_WORD, word.getWord());
                intent.putExtra(FavoriteDetailActivity.EXTRA_DEFINITION, word.getDefinition());
                intent.putExtra(FavoriteDetailActivity.EXTRA_PHONETIC, word.getPhonetic());
                intent.putExtra(FavoriteDetailActivity.EXTRA_SYNONYMS, word.getSynonyms());
                startActivity(intent);
            },
            // On delete click listener
            word -> executorService.execute(() -> {
                database.favoriteWordDao().delete(word);
            })
        );
        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFavorites.setAdapter(adapter);

        // Load favorite words
        loadFavoriteWords();
    }

    private void loadFavoriteWords() {
        database.favoriteWordDao().getAllFavoriteWords().observe(this, favoriteWords -> {
            if (favoriteWords != null && !favoriteWords.isEmpty()) {
                adapter.setFavoriteWords(favoriteWords);
                textViewEmpty.setVisibility(View.GONE);
                recyclerViewFavorites.setVisibility(View.VISIBLE);
            } else {
                textViewEmpty.setVisibility(View.VISIBLE);
                recyclerViewFavorites.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
