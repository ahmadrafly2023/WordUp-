package com.example.project_pendidikan.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_pendidikan.R;
import com.example.project_pendidikan.model.FavoriteWord;

import java.util.ArrayList;
import java.util.List;

public class FavoriteWordAdapter extends RecyclerView.Adapter<FavoriteWordAdapter.FavoriteWordViewHolder> {
    private List<FavoriteWord> favoriteWords = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FavoriteWord word);
        void onDeleteClick(FavoriteWord word);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteWordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_word, parent, false);
        return new FavoriteWordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteWordViewHolder holder, int position) {
        FavoriteWord favoriteWord = favoriteWords.get(position);
        holder.textViewWord.setText(favoriteWord.getWord());
        holder.textViewDefinition.setText(favoriteWord.getDefinition());
        if (favoriteWord.getPhonetic() != null && !favoriteWord.getPhonetic().isEmpty()) {
            holder.textViewPhonetic.setVisibility(View.VISIBLE);
            holder.textViewPhonetic.setText(favoriteWord.getPhonetic());
        } else {
            holder.textViewPhonetic.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(favoriteWord);
            }
        });

        holder.textViewDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(favoriteWord);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoriteWords.size();
    }

    public void setFavoriteWords(List<FavoriteWord> favoriteWords) {
        this.favoriteWords = favoriteWords;
        notifyDataSetChanged();
    }

    static class FavoriteWordViewHolder extends RecyclerView.ViewHolder {
        TextView textViewWord;
        TextView textViewPhonetic;
        TextView textViewDefinition;
        TextView textViewDelete;

        public FavoriteWordViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewWord = itemView.findViewById(R.id.textViewWord);
            textViewPhonetic = itemView.findViewById(R.id.textViewPhonetic);
            textViewDefinition = itemView.findViewById(R.id.textViewDefinition);
            textViewDelete = itemView.findViewById(R.id.textViewDelete);
        }
    }
}
