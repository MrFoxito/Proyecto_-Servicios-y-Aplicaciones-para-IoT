package com.example.proyecto_iot.admin.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.model.AdminProjectItem;
import com.example.proyecto_iot.databinding.ItemAdminProyectoBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminProjectsAdapter extends RecyclerView.Adapter<AdminProjectsAdapter.ProjectViewHolder> {

    public interface Listener {
        void onProjectClick(AdminProjectItem item);
    }

    private final List<AdminProjectItem> items = new ArrayList<>();
    private final Listener listener;

    public AdminProjectsAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<AdminProjectItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminProyectoBinding binding = ItemAdminProyectoBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ProjectViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ProjectViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminProyectoBinding binding;

        ProjectViewHolder(ItemAdminProyectoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AdminProjectItem item) {
            binding.tvTitle.setText(item.getTitle());
            binding.tvLocation.setText(" " + item.getLocation());
            binding.tvPrice.setText(item.getPriceFrom());
            if (!item.getImageUrl().isEmpty()) {
                Glide.with(binding.ivProject)
                        .load(item.getImageUrl())
                        .centerCrop()
                        .into(binding.ivProject);
            } else {
                binding.ivProject.setImageResource(item.getImageRes());
            }
            binding.btnVerProyecto.setOnClickListener(v -> listener.onProjectClick(item));
            binding.getRoot().setOnClickListener(v -> listener.onProjectClick(item));

            binding.tvStatus.setText(item.getStatus());
            if ("EN PREVENTA".equals(item.getStatus())) {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_tag_preventa);
                binding.tvStatus.setTextColor(0xFF8A6D3B);
            } else if ("EN VENTA".equals(item.getStatus())) {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_tag_venta);
                binding.tvStatus.setTextColor(0xFF1E7B54);
            } else {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_tag_planos);
                binding.tvStatus.setTextColor(0xFF3B6EA8);
            }
        }
    }
}
