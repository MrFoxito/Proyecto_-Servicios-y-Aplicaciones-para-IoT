package com.example.proyecto_iot.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.model.AdminAdvisorItem;
import com.example.proyecto_iot.databinding.ItemAdminAsesorBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminAdvisorsAdapter extends RecyclerView.Adapter<AdminAdvisorsAdapter.AdvisorViewHolder> {

    public interface Listener {
        void onAdvisorClick(AdminAdvisorItem item);
        void onAssignProjectClick(AdminAdvisorItem item);
    }

    private final List<AdminAdvisorItem> items = new ArrayList<>();
    private final Listener listener;

    public AdminAdvisorsAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<AdminAdvisorItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public AdvisorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminAsesorBinding binding = ItemAdminAsesorBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AdvisorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AdvisorViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    class AdvisorViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminAsesorBinding binding;

        AdvisorViewHolder(ItemAdminAsesorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AdminAdvisorItem item) {
            binding.tvName.setText(item.getName());
            binding.tvRating.setText(item.getRatingText());
            binding.tvEmail.setText(item.getEmail());
            binding.ivAvatar.setImageResource(item.getAvatarRes());
            binding.btnAsignarProyecto.setOnClickListener(v -> listener.onAssignProjectClick(item));
            binding.getRoot().setOnClickListener(v -> listener.onAdvisorClick(item));

            binding.tvStatus.setText(item.isActive() ? "ACTIVO" : "INACTIVO");
            binding.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    item.isActive() ? 0xFF10B981 : 0xFF9CA3AF
            ));

            binding.projectChipsContainer.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(binding.getRoot().getContext());
            for (String project : item.getProjects()) {
                View chipView = inflater.inflate(R.layout.item_admin_project_chip, binding.projectChipsContainer, false);
                TextView chipText = chipView.findViewById(R.id.tvProjectChip);
                chipText.setText(project);
                binding.projectChipsContainer.addView(chipView);
            }
        }
    }
}
