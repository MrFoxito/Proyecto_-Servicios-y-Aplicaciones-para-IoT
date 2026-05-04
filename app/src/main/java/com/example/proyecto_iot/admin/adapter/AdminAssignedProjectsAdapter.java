package com.example.proyecto_iot.admin.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.admin.model.AdminAssignedProjectItem;
import com.example.proyecto_iot.databinding.ItemAdminAssignedProjectBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminAssignedProjectsAdapter extends RecyclerView.Adapter<AdminAssignedProjectsAdapter.AssignedProjectViewHolder> {

    public interface Listener {
        void onProjectClick(AdminAssignedProjectItem item);
    }

    private final List<AdminAssignedProjectItem> items = new ArrayList<>();
    private final Listener listener;

    public AdminAssignedProjectsAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<AdminAssignedProjectItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AssignedProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminAssignedProjectBinding binding = ItemAdminAssignedProjectBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AssignedProjectViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignedProjectViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AssignedProjectViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminAssignedProjectBinding binding;

        AssignedProjectViewHolder(ItemAdminAssignedProjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AdminAssignedProjectItem item, Listener listener) {
            binding.ivProject.setImageResource(item.getImageRes());
            binding.tvStatus.setText(item.getStatus());
            binding.tvTitle.setText(item.getTitle());
            binding.tvLocation.setText(item.getLocation());

            binding.tvStatus.setBackgroundTintList(ColorStateList.valueOf(
                    "ACTIVO".equals(item.getStatus()) ? 0xFFFFFFFF : 0xFFF5EFE1
            ));
            binding.tvStatus.setTextColor("ACTIVO".equals(item.getStatus()) ? 0xFF0B1A24 : 0xFF8A6D3B);

            binding.getRoot().setOnClickListener(v -> listener.onProjectClick(item));
        }
    }
}
