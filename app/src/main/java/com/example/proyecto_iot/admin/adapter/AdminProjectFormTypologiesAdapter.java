package com.example.proyecto_iot.admin.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.model.AdminProjectFormTypologyItem;
import com.example.proyecto_iot.databinding.ItemAdminTipologiaFormBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminProjectFormTypologiesAdapter extends RecyclerView.Adapter<AdminProjectFormTypologiesAdapter.TypologyViewHolder> {

    public interface Listener {
        void onEditRequested(AdminProjectFormTypologyItem item, int position);

        void onDeleteRequested(AdminProjectFormTypologyItem item, int position);
    }

    private final List<AdminProjectFormTypologyItem> items = new ArrayList<>();
    private final Listener listener;

    public AdminProjectFormTypologiesAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<AdminProjectFormTypologyItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void addItem(AdminProjectFormTypologyItem item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    public void updateItem(int position, AdminProjectFormTypologyItem item) {
        if (position < 0 || position >= items.size()) {
            return;
        }
        items.set(position, item);
        notifyItemChanged(position);
    }

    public void removeItem(int position) {
        if (position < 0 || position >= items.size()) {
            return;
        }
        items.remove(position);
        notifyItemRemoved(position);
    }

    public AdminProjectFormTypologyItem getItem(int position) {
        return items.get(position);
    }

    public List<AdminProjectFormTypologyItem> getItems() {
        return new ArrayList<>(items);
    }

    @NonNull
    @Override
    public TypologyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminTipologiaFormBinding binding = ItemAdminTipologiaFormBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TypologyViewHolder(binding, listener);
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
        private final ItemAdminTipologiaFormBinding binding;
        private final Listener listener;

        TypologyViewHolder(ItemAdminTipologiaFormBinding binding, Listener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(AdminProjectFormTypologyItem item) {
            binding.tvTypologyTitle.setText(item.getTitle());
            binding.tvTypologyStatus.setText(item.getStatusLabel());
            binding.tvArea.setText(item.getArea());
            binding.tvBedrooms.setText(item.getBedrooms());
            binding.tvTotalAmount.setText(item.getTotalAmount());
            binding.tvSeparationAmount.setText(item.getSeparationAmount());

            binding.tvTypologyStatus.setBackgroundResource(
                    item.isAvailable() ? R.drawable.admin_detail_status_green : R.drawable.admin_detail_status_red
            );
            binding.tvTypologyStatus.setTextColor(item.isAvailable() ? 0xFF0B6B0E : 0xFFB31515);

            binding.getRoot().setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEditRequested(item, position);
                }
            });

            binding.btnDeleteTypology.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteRequested(item, position);
                }
            });
        }
    }
}
