package com.example.proyecto_iot.admin.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.admin.model.AdminProjectGalleryItem;
import com.example.proyecto_iot.data.ProjectImageLoader;
import com.example.proyecto_iot.databinding.ItemAdminProjectGalleryImageBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminProjectGalleryAdapter extends RecyclerView.Adapter<AdminProjectGalleryAdapter.GalleryViewHolder> {

    private final List<AdminProjectGalleryItem> items = new ArrayList<>();

    public void setItems(List<AdminProjectGalleryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminProjectGalleryImageBinding binding = ItemAdminProjectGalleryImageBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new GalleryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class GalleryViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminProjectGalleryImageBinding binding;

        GalleryViewHolder(ItemAdminProjectGalleryImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AdminProjectGalleryItem item) {
            ProjectImageLoader.load(binding.ivProjectGallery, item.getImageUrl(), item.getImageRes());
        }
    }
}
