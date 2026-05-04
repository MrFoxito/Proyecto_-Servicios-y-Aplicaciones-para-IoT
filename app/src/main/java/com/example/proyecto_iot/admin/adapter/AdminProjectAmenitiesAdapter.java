package com.example.proyecto_iot.admin.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.admin.model.AdminProjectAmenityItem;
import com.example.proyecto_iot.databinding.ItemAdminAmenidadBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminProjectAmenitiesAdapter extends RecyclerView.Adapter<AdminProjectAmenitiesAdapter.AmenityViewHolder> {

    private final List<AdminProjectAmenityItem> items = new ArrayList<>();

    public void setItems(List<AdminProjectAmenityItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AmenityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminAmenidadBinding binding = ItemAdminAmenidadBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AmenityViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AmenityViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AmenityViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminAmenidadBinding binding;

        AmenityViewHolder(ItemAdminAmenidadBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AdminProjectAmenityItem item) {
            binding.ivAmenity.setImageResource(item.getIconRes());
            binding.tvAmenityTitle.setText(item.getTitle());
        }
    }
}
