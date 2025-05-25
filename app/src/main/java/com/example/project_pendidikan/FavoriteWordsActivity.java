package com.example.project_pendidikan;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

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
    private FavoriteWordAdapter adapter;
    private AppDatabase database;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_words);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize database and executor
        database = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        // Setup RecyclerView
        recyclerViewFavorites = findViewById(R.id.recyclerViewFavorites);
        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FavoriteWordAdapter();
        recyclerViewFavorites.setAdapter(adapter);

        // Setup click listeners
        adapter.setOnItemClickListener(new FavoriteWordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FavoriteWord word) {
                // Return to dashboard with the word to search
                Intent intent = new Intent();
                intent.putExtra("WORD_TO_SEARCH", word.getWord());
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onDeleteClick(FavoriteWord word) {
                executorService.execute(() -> {
                    database.favoriteWordDao().delete(word);
                });
            }
        });

        // Observe favorite words
        database.favoriteWordDao().getAllFavoriteWords().observe(this, new Observer<List<FavoriteWord>>() {
            @Override
            public void onChanged(List<FavoriteWord> favoriteWords) {
                adapter.setFavoriteWords(favoriteWords);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
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
