package com.example.project_pendidikan.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_pendidikan.R;
import com.example.project_pendidikan.model.Definition;

import java.util.List;

public class DefinitionAdapter extends RecyclerView.Adapter<DefinitionAdapter.DefinitionViewHolder> {
    private List<Definition> definitions;

    public DefinitionAdapter(List<Definition> definitions) {
        this.definitions = definitions;
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
        
        if (definition.getExample() != null && !definition.getExample().isEmpty()) {
            holder.textViewExample.setVisibility(View.VISIBLE);
            holder.textViewExample.setText("Example: " + definition.getExample());
        } else {
            holder.textViewExample.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return definitions != null ? definitions.size() : 0;
    }

    public void setDefinitions(List<Definition> definitions) {
        this.definitions = definitions;
        notifyDataSetChanged();
    }

    public List<Definition> getDefinitions() {
        return definitions;
    }

    static class DefinitionViewHolder extends RecyclerView.ViewHolder {
        TextView textViewPartOfSpeech;
        TextView textViewDefinition;
        TextView textViewExample;

        public DefinitionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPartOfSpeech = itemView.findViewById(R.id.textViewPartOfSpeech);
            textViewDefinition = itemView.findViewById(R.id.textViewDefinition);
            textViewExample = itemView.findViewById(R.id.textViewExample);
        }
    }
}
