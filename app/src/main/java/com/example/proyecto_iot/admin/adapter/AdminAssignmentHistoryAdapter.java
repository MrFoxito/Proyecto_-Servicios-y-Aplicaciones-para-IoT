package com.example.proyecto_iot.admin.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.model.AdminAssignmentRecord;
import com.example.proyecto_iot.databinding.ItemAdminAssignmentHistoryBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminAssignmentHistoryAdapter extends RecyclerView.Adapter<AdminAssignmentHistoryAdapter.AssignmentViewHolder> {

    private final List<AdminAssignmentRecord> items = new ArrayList<>();

    public void setItems(List<AdminAssignmentRecord> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public AdminAssignmentRecord getItemAt(int position) {
        if (position < 0 || position >= items.size()) {
            return null;
        }
        return items.get(position);
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminAssignmentHistoryBinding binding = ItemAdminAssignmentHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AssignmentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminAssignmentHistoryBinding binding;

        AssignmentViewHolder(ItemAdminAssignmentHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AdminAssignmentRecord item) {
            binding.tvProjectTitle.setText(item.getProjectTitle());
            binding.tvProjectStatus.setText(item.getProjectStatus());
            binding.tvAdvisorName.setText("Asesor: " + item.getAdvisorName());
            binding.tvProjectLocation.setText(item.getProjectNeighborhood() + " | " + item.getProjectLocation());
            binding.tvAssignedAt.setText("Asignado el " + item.getAssignedAt());

            if ("EN VENTA".equals(item.getProjectStatus())) {
                binding.tvProjectStatus.setBackgroundResource(R.drawable.bg_pill_active);
                binding.tvProjectStatus.setBackgroundTintList(ColorStateList.valueOf(0xFFE7FBF2));
                binding.tvProjectStatus.setTextColor(0xFF1E7B54);
            } else if ("EN PREVENTA".equals(item.getProjectStatus())) {
                binding.tvProjectStatus.setBackgroundResource(R.drawable.bg_pill_inactive);
                binding.tvProjectStatus.setBackgroundTintList(ColorStateList.valueOf(0xFFF5EFE1));
                binding.tvProjectStatus.setTextColor(0xFF8A6D3B);
            } else {
                binding.tvProjectStatus.setBackgroundResource(R.drawable.bg_pill_inactive);
                binding.tvProjectStatus.setBackgroundTintList(ColorStateList.valueOf(0xFFE8EEF5));
                binding.tvProjectStatus.setTextColor(0xFF3B6EA8);
            }
        }
    }
}
