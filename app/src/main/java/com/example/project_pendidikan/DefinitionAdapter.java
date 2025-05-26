package com.example.project_pendidikan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DefinitionAdapter extends RecyclerView.Adapter<DefinitionAdapter.DefinitionViewHolder> {

    private List<Definition> definitions = new ArrayList<>();

    public void setDefinitions(List<Definition> definitions) {
        this.definitions = definitions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DefinitionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_definition, parent, false);
        return new DefinitionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DefinitionViewHolder holder, int position) {
        Definition definition = definitions.get(position);
        holder.textViewPartOfSpeech.setText(definition.getPartOfSpeech());
        holder.textViewDefinition.setText(definition.getDefinition());
    }

    @Override
    public int getItemCount() {
        return definitions.size();
    }

    static class DefinitionViewHolder extends RecyclerView.ViewHolder {
        TextView textViewPartOfSpeech;
        TextView textViewDefinition;

        DefinitionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPartOfSpeech = itemView.findViewById(R.id.textViewPartOfSpeech);
            textViewDefinition = itemView.findViewById(R.id.textViewDefinition);
        }
    }
}
