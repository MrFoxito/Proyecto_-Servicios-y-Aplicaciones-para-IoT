package com.example.proyecto_iot.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.admin.model.AdminProjectVisualItem;
import com.example.proyecto_iot.databinding.ItemAdminProjectVisualEditorBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminProjectVisualEditorAdapter extends RecyclerView.Adapter<AdminProjectVisualEditorAdapter.VisualViewHolder> {

    public interface Listener {
        void onActionClick(AdminProjectVisualItem item);
    }

    private final List<AdminProjectVisualItem> items = new ArrayList<>();
    private final Listener listener;

    public AdminProjectVisualEditorAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<AdminProjectVisualItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VisualViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminProjectVisualEditorBinding binding = ItemAdminProjectVisualEditorBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new VisualViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VisualViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VisualViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminProjectVisualEditorBinding binding;

        VisualViewHolder(ItemAdminProjectVisualEditorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AdminProjectVisualItem item, Listener listener) {
            binding.tvVisualTitle.setText(item.getTitle());
            binding.btnVisualAction.setText(item.getActionLabel());

            if (item.hasImage()) {
                binding.ivVisual.setVisibility(View.VISIBLE);
                binding.ivVisual.setImageResource(item.getImageRes());
                binding.placeholderContainer.setVisibility(View.GONE);
            } else {
                binding.ivVisual.setVisibility(View.GONE);
                binding.placeholderContainer.setVisibility(View.VISIBLE);
            }

            binding.btnVisualAction.setOnClickListener(v -> listener.onActionClick(item));
            binding.getRoot().setOnClickListener(v -> listener.onActionClick(item));
        }
    }
}
