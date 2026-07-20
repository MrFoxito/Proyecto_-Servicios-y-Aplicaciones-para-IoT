package com.example.proyecto_iot.usuario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.ProjectImageLoader;

import java.util.ArrayList;
import java.util.List;

public class UsuarioProjectSuggestionAdapter
        extends RecyclerView.Adapter<UsuarioProjectSuggestionAdapter.SuggestionViewHolder> {

    public interface OnSuggestionClickListener {
        void onSuggestionClick(UsuarioPropertyListItem item);
    }

    private final List<UsuarioPropertyListItem> items = new ArrayList<>();
    private final OnSuggestionClickListener listener;

    public UsuarioProjectSuggestionAdapter(OnSuggestionClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<UsuarioPropertyListItem> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SuggestionViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario_project_suggestion, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        UsuarioPropertyListItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.location.setText(item.getLocation());
        ProjectImageLoader.load(holder.image, item.getImageUrl(), item.getImageResId());
        holder.itemView.setOnClickListener(v -> listener.onSuggestionClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView title;
        final TextView location;

        SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ivProjectSuggestionImage);
            title = itemView.findViewById(R.id.tvProjectSuggestionTitle);
            location = itemView.findViewById(R.id.tvProjectSuggestionLocation);
        }
    }
}
