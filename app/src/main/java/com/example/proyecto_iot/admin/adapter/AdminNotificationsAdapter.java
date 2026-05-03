package com.example.proyecto_iot.admin.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.model.AdminNotificationItem;
import com.example.proyecto_iot.databinding.ItemAdminNotificationCardBinding;
import com.example.proyecto_iot.databinding.ItemAdminNotificationSectionBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminNotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface Listener {
        void onActionClick(AdminNotificationItem item);
    }

    public interface RowItem { }

    public static class SectionRow implements RowItem {
        private final String title;

        public SectionRow(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    public static class NotificationRow implements RowItem {
        private final AdminNotificationItem item;

        public NotificationRow(AdminNotificationItem item) {
            this.item = item;
        }

        public AdminNotificationItem getItem() {
            return item;
        }
    }

    private static final int TYPE_SECTION = 0;
    private static final int TYPE_NOTIFICATION = 1;

    private final List<RowItem> rows = new ArrayList<>();
    private final Listener listener;

    public AdminNotificationsAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setRows(List<RowItem> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    public boolean isNotificationPosition(int position) {
        return position >= 0 && position < rows.size() && rows.get(position) instanceof NotificationRow;
    }

    public AdminNotificationItem getNotificationAt(int position) {
        if (!isNotificationPosition(position)) {
            return null;
        }
        return ((NotificationRow) rows.get(position)).getItem();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position) instanceof SectionRow ? TYPE_SECTION : TYPE_NOTIFICATION;
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SECTION) {
            ItemAdminNotificationSectionBinding binding = ItemAdminNotificationSectionBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new SectionViewHolder(binding);
        }
        ItemAdminNotificationCardBinding binding = ItemAdminNotificationCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SectionViewHolder) {
            ((SectionViewHolder) holder).bind((SectionRow) rows.get(position));
        } else if (holder instanceof NotificationViewHolder) {
            ((NotificationViewHolder) holder).bind(((NotificationRow) rows.get(position)).getItem());
        }
    }

    class SectionViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminNotificationSectionBinding binding;

        SectionViewHolder(ItemAdminNotificationSectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SectionRow row) {
            binding.tvSectionTitle.setText(row.getTitle());
        }
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final ItemAdminNotificationCardBinding binding;

        NotificationViewHolder(ItemAdminNotificationCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(AdminNotificationItem item) {
            binding.tvTitle.setText(item.getTitle());
            binding.tvBadge.setText(item.getBadge());
            binding.tvLine1.setText(item.getLine1());
            binding.tvLine2.setText(item.getLine2());
            binding.btnAction.setText(item.getActionText());
            binding.btnAction.setOnClickListener(v -> listener.onActionClick(item));

            binding.sideAlert.setVisibility(item.getType() == AdminNotificationItem.Type.ACTION ? android.view.View.VISIBLE : android.view.View.GONE);
            binding.ivAlert.setVisibility(item.getType() == AdminNotificationItem.Type.ACTION ? android.view.View.VISIBLE : android.view.View.GONE);

            if (item.getType() == AdminNotificationItem.Type.PAYMENT) {
                binding.tvBadge.setBackgroundResource(R.drawable.bg_pill_active);
                binding.tvBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE7FBF2));
                binding.tvBadge.setTextColor(0xFF28A76A);
                binding.tvTitle.setTextColor(0xFF263E4E);
            } else if (item.getType() == AdminNotificationItem.Type.SEPARATION) {
                binding.tvBadge.setBackgroundResource(R.drawable.bg_pill_inactive);
                binding.tvBadge.setTextColor(0xFF6B7280);
                binding.tvTitle.setTextColor(0xFF263E4E);
            } else {
                binding.tvBadge.setBackgroundResource(R.drawable.bg_pill_inactive);
                binding.tvBadge.setTextColor(0xFF6B7280);
                binding.tvTitle.setTextColor(0xFFE53E3E);
            }
        }
    }
}
