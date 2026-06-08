package com.example.proyecto_iot.admin.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.model.AdminProjectTypologyItem;
import com.example.proyecto_iot.databinding.ItemAdminTipologiaBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminProjectTypologiesAdapter extends RecyclerView.Adapter<AdminProjectTypologiesAdapter.TypologyViewHolder> {

    private final List<AdminProjectTypologyItem> items = new ArrayList<>();

    public void setItems(List<AdminProjectTypologyItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TypologyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminTipologiaBinding binding = ItemAdminTipologiaBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TypologyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TypologyViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TypologyViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminTipologiaBinding binding;

        TypologyViewHolder(ItemAdminTipologiaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AdminProjectTypologyItem item) {
            binding.tvTypologyTitle.setText(item.getTitle());
            binding.tvTypologyStatus.setText(item.getStatus());
            binding.tvArea.setText(item.getArea());
            binding.tvBedrooms.setText(item.getBedrooms());
            binding.tvBathrooms.setText(item.getBathrooms());
            binding.tvTotalAmount.setText(item.getTotalAmount());
            binding.tvSeparationAmount.setText(item.getSeparationAmount());

            binding.tvTypologyStatus.setBackgroundResource(
                    item.isAvailable() ? R.drawable.admin_detail_status_green : R.drawable.admin_detail_status_red
            );
            binding.tvTypologyStatus.setTextColor(item.isAvailable() ? 0xFF0B6B0E : 0xFFB31515);
        }
    }
}
