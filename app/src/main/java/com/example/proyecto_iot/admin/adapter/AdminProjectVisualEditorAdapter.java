package com.example.proyecto_iot.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;

import com.example.proyecto_iot.admin.model.AdminProjectVisualItem;
import com.example.proyecto_iot.databinding.ItemAdminProjectVisualEditorBinding;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class AdminProjectVisualEditorAdapter extends RecyclerView.Adapter<AdminProjectVisualEditorAdapter.VisualViewHolder> {

    public interface Listener {
        void onActionClick(AdminProjectVisualItem item, int position);
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

    public void setDeviceImage(int position, String imageUri) {
        if (position < 0 || position >= items.size()) {
            return;
        }
        items.get(position).setDeviceImageUri(imageUri);
        notifyItemChanged(position);
    }

    public void clearImage(int position) {
        if (position < 0 || position >= items.size()) {
            return;
        }
        items.get(position).clearDeviceImage();
        notifyItemChanged(position);
    }

    public int getSelectedImageCount() {
        int count = 0;
        for (AdminProjectVisualItem item : items) {
            if (item.hasImage()) {
                count++;
            }
        }
        return count;
    }

    public List<String> getDeviceImageUris() {
        List<String> uris = new ArrayList<>();
        for (AdminProjectVisualItem item : items) {
            String imageUri = item.getImageUri();
            if (!imageUri.isEmpty() && !imageUri.startsWith("http://") && !imageUri.startsWith("https://")) {
                uris.add(imageUri);
            }
        }
        return uris;
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
                if (!item.getImageUri().isEmpty()) {
                    if (item.getImageUri().startsWith("http://") || item.getImageUri().startsWith("https://")) {
                        Glide.with(binding.ivVisual)
                                .load(item.getImageUri())
                                .centerCrop()
                                .into(binding.ivVisual);
                    } else {
                        binding.ivVisual.setImageURI(Uri.parse(item.getImageUri()));
                    }
                } else {
                    binding.ivVisual.setImageResource(item.getImageRes());
                }
                binding.placeholderContainer.setVisibility(View.GONE);
            } else {
                binding.ivVisual.setVisibility(View.GONE);
                binding.placeholderContainer.setVisibility(View.VISIBLE);
            }

            binding.btnVisualAction.setOnClickListener(v -> notifyAction(listener, item));
            binding.getRoot().setOnClickListener(v -> notifyAction(listener, item));
        }

        private void notifyAction(Listener listener, AdminProjectVisualItem item) {
            int position = getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                listener.onActionClick(item, position);
            }
        }
    }
}
