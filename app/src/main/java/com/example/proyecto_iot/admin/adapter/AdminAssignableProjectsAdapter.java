package com.example.proyecto_iot.admin.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.model.AdminAssignableProjectItem;
import com.example.proyecto_iot.data.ProjectImageLoader;
import com.example.proyecto_iot.databinding.ItemAdminAsignableProjectBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminAssignableProjectsAdapter extends RecyclerView.Adapter<AdminAssignableProjectsAdapter.AssignableProjectViewHolder> {

    public interface Listener {
        void onDetailsClick(AdminAssignableProjectItem item);
        void onAssignClick(AdminAssignableProjectItem item);
    }

    private final List<AdminAssignableProjectItem> items = new ArrayList<>();
    private final Listener listener;

    public AdminAssignableProjectsAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<AdminAssignableProjectItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AssignableProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminAsignableProjectBinding binding = ItemAdminAsignableProjectBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AssignableProjectViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignableProjectViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AssignableProjectViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminAsignableProjectBinding binding;

        AssignableProjectViewHolder(ItemAdminAsignableProjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AdminAssignableProjectItem item, Listener listener) {
            ProjectImageLoader.load(binding.ivProject, item.getImageUrl(), item.getImageRes());
            binding.tvTitle.setText(item.getTitle());
            binding.tvLocation.setText(item.getLocation());
            binding.tvNeighborhood.setText(item.getNeighborhood());
            binding.tvStatus.setText(item.getStatus());

            if ("EN VENTA".equals(item.getStatus())) {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_pill_active);
                binding.tvStatus.setBackgroundTintList(ColorStateList.valueOf(0xFFE7FBF2));
                binding.tvStatus.setTextColor(0xFF1E7B54);
            } else if ("EN PREVENTA".equals(item.getStatus())) {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_pill_inactive);
                binding.tvStatus.setBackgroundTintList(ColorStateList.valueOf(0xFFF5EFE1));
                binding.tvStatus.setTextColor(0xFF8A6D3B);
            } else {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_pill_inactive);
                binding.tvStatus.setBackgroundTintList(ColorStateList.valueOf(0xFFE8EEF5));
                binding.tvStatus.setTextColor(0xFF3B6EA8);
            }

            binding.tvVerDetalles.setOnClickListener(v -> listener.onDetailsClick(item));
            binding.btnAsignar.setOnClickListener(v -> listener.onAssignClick(item));
            binding.getRoot().setOnClickListener(v -> listener.onDetailsClick(item));
        }
    }
}
