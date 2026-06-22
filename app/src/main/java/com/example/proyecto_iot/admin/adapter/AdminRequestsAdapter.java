package com.example.proyecto_iot.admin.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.databinding.ItemAdminSolicitudBinding;
import com.example.proyecto_iot.admin.model.AdminRequestItem;

import java.util.ArrayList;
import java.util.List;

public class AdminRequestsAdapter extends RecyclerView.Adapter<AdminRequestsAdapter.RequestViewHolder> {

    public interface Listener {
        void onRequestClick(AdminRequestItem item);
    }

    private final List<AdminRequestItem> items = new ArrayList<>();
    private final Listener listener;

    public AdminRequestsAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<AdminRequestItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminSolicitudBinding binding = ItemAdminSolicitudBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new RequestViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminSolicitudBinding binding;

        RequestViewHolder(ItemAdminSolicitudBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AdminRequestItem item) {
            binding.ivAvatar.setImageResource(item.getAvatarRes());
            binding.tvName.setText(item.getName());
            binding.tvSubtitle.setText(item.getSubtitle());
            binding.tvDescription.setText(item.getDescription());
            binding.btnVerSolicitud.setOnClickListener(v -> listener.onRequestClick(item));
            binding.getRoot().setOnClickListener(v -> listener.onRequestClick(item));

            binding.tvStatus.setText(item.getStatus());
            if ("ACEPTADA".equals(item.getStatus())) {
                binding.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF10B981));
            } else {
                binding.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF2B544));
            }

            boolean accepted = "ACEPTADA".equals(item.getStatus());
            binding.btnVerSolicitud.setBackgroundResource(
                    accepted ? R.drawable.bg_btn_light_grey : R.drawable.bg_btn_dark_blue
            );
            binding.btnVerSolicitud.setTextColor(accepted ? 0xFF163143 : 0xFFFFFFFF);
        }
    }
}
