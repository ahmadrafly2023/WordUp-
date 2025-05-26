package com.example.project_pendidikan;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class HomeFragment extends Fragment {

    private TextInputEditText editTextSearch;
    private MaterialCardView cardViewResult;
    private TextView textViewWord;
    private TextView textViewPhonetic;
    private RecyclerView recyclerViewDefinitions;
    private ChipGroup chipGroupSynonyms;
    private DefinitionAdapter definitionAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        editTextSearch = view.findViewById(R.id.editTextSearch);
        cardViewResult = view.findViewById(R.id.cardViewResult);
        textViewWord = view.findViewById(R.id.textViewWord);
        textViewPhonetic = view.findViewById(R.id.textViewPhonetic);
        recyclerViewDefinitions = view.findViewById(R.id.recyclerViewDefinitions);
        chipGroupSynonyms = view.findViewById(R.id.chipGroupSynonyms);

        // Setup RecyclerView
        recyclerViewDefinitions.setLayoutManager(new LinearLayoutManager(requireContext()));
        definitionAdapter = new DefinitionAdapter();
        recyclerViewDefinitions.setAdapter(definitionAdapter);

        // Setup search functionality
        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch(editTextSearch.getText().toString());
                return true;
            }
            return false;
        });
    }

    private void performSearch(String word) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }

        // TODO: Implement dictionary API call
        // For now, just show some dummy data
        showDummyResults(word);
    }

    private void showDummyResults(String word) {
        cardViewResult.setVisibility(View.VISIBLE);
        textViewWord.setText(word);
        textViewPhonetic.setText("/ˈdʌmi/");

        // Add dummy definitions
        List<Definition> definitions = List.of(
            new Definition("noun", "A placeholder or mock-up used for testing"),
            new Definition("adjective", "Serving as a temporary substitute")
        );
        definitionAdapter.setDefinitions(definitions);

        // Add dummy synonyms
        chipGroupSynonyms.removeAllViews();
        String[] synonyms = {"mock", "placeholder", "temporary"};
        for (String synonym : synonyms) {
            Chip chip = new Chip(requireContext());
            chip.setText(synonym);
            chip.setClickable(true);
            chip.setCheckable(false);
            chipGroupSynonyms.addView(chip);
        }
    }
}
