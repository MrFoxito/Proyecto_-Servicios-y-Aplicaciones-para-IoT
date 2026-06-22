package com.example.proyecto_iot.admin.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.admin.model.AdminReviewItem;
import com.example.proyecto_iot.databinding.ItemAdminResenaBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminReviewsAdapter extends RecyclerView.Adapter<AdminReviewsAdapter.ReviewViewHolder> {

    private final List<AdminReviewItem> items = new ArrayList<>();

    public void setItems(List<AdminReviewItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminResenaBinding binding = ItemAdminResenaBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ReviewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminResenaBinding binding;

        ReviewViewHolder(ItemAdminResenaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AdminReviewItem item) {
            binding.ivAvatar.setImageResource(item.getAvatarRes());
            binding.tvReviewerName.setText(item.getReviewerName());
            binding.tvReviewDate.setText(item.getDate());
            binding.tvProjectTag.setText("PROYECTO: " + item.getProjectName());
            binding.tvReviewText.setText(item.getReviewText());
            binding.tvRating.setText(item.getRatingLabel());
        }
    }
}
