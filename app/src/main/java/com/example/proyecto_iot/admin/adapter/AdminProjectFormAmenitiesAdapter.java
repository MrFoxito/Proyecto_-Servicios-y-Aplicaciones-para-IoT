package com.example.proyecto_iot.admin.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.model.AdminProjectFormAmenityItem;
import com.example.proyecto_iot.databinding.ItemAdminAmenidadBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminProjectFormAmenitiesAdapter extends RecyclerView.Adapter<AdminProjectFormAmenitiesAdapter.AmenityViewHolder> {

    private final List<AdminProjectFormAmenityItem> items = new ArrayList<>();

    public void setItems(List<AdminProjectFormAmenityItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void addItem(AdminProjectFormAmenityItem item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    public List<AdminProjectFormAmenityItem> getItems() {
        return new ArrayList<>(items);
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

    class AmenityViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminAmenidadBinding binding;

        AmenityViewHolder(ItemAdminAmenidadBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AdminProjectFormAmenityItem item) {
            binding.ivAmenity.setImageResource(item.getIconRes());
            binding.tvAmenityTitle.setText(item.getTitle());

            int tint = item.isSelected() ? Color.parseColor("#163143") : Color.parseColor("#8E98A3");
            int textColor = item.isSelected() ? Color.parseColor("#163143") : Color.parseColor("#6B7280");

            binding.getRoot().setBackgroundResource(
                    item.isSelected() ? R.drawable.admin_form_amenity_selected_bg : R.drawable.admin_detail_amenity_bg
            );
            binding.getRoot().setAlpha(item.isSelected() ? 1f : 0.82f);
            binding.tvAmenityTitle.setTextColor(textColor);
            ImageViewCompat.setImageTintList(binding.ivAmenity, ColorStateList.valueOf(tint));

            binding.getRoot().setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    AdminProjectFormAmenityItem currentItem = items.get(position);
                    currentItem.setSelected(!currentItem.isSelected());
                    notifyItemChanged(position);
                }
            });
        }
    }
}
