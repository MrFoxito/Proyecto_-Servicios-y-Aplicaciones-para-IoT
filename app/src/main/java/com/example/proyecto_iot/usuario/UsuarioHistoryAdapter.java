package com.example.proyecto_iot.usuario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;

import java.util.List;

public class UsuarioHistoryAdapter extends RecyclerView.Adapter<UsuarioHistoryAdapter.HistoryViewHolder> {

    public interface OnHistoryClickListener {
        void onHistoryClick(UsuarioHistoryItem item);
    }

    private final List<UsuarioHistoryItem> items;
    private final OnHistoryClickListener clickListener;

    public UsuarioHistoryAdapter(List<UsuarioHistoryItem> items, OnHistoryClickListener clickListener) {
        this.items = items;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario_actividad_historial, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        UsuarioHistoryItem item = items.get(position);
        holder.badge.setText(item.getBadge());
        holder.title.setText(item.getTitle());
        holder.date.setText(item.getDate());
        holder.summary.setText(item.getSummary());
        holder.itemView.setOnClickListener(v -> clickListener.onHistoryClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView badge;
        private final TextView title;
        private final TextView date;
        private final TextView summary;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            badge = itemView.findViewById(R.id.tvHistoryItemBadge);
            title = itemView.findViewById(R.id.tvHistoryItemTitle);
            date = itemView.findViewById(R.id.tvHistoryItemDate);
            summary = itemView.findViewById(R.id.tvHistoryItemSummary);
        }
    }
}
