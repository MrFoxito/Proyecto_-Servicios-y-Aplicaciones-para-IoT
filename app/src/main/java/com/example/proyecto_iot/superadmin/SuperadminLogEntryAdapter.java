package com.example.proyecto_iot.superadmin;

import com.example.proyecto_iot.R;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SuperadminLogEntryAdapter extends RecyclerView.Adapter<SuperadminLogEntryAdapter.LogEntryViewHolder> {
    private final List<SuperadminLogEntryItem> items;

    public SuperadminLogEntryAdapter(List<SuperadminLogEntryItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public LogEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sa_log_entry, parent, false);
        return new LogEntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogEntryViewHolder holder, int position) {
        SuperadminLogEntryItem item = items.get(position);
        holder.accent.setBackgroundColor(item.getAccentColor());
        ViewCompat.setBackgroundTintList(holder.iconContainer, ColorStateList.valueOf(item.getAccentColor()));
        holder.icon.setImageResource(item.getIconResId());
        holder.icon.setColorFilter(item.getIconTint());
        holder.title.setText(item.getTitle());
        holder.subtitle.setText(item.getSubtitle());
        holder.time.setText(SuperadminRangeFilterHelper.formatLogTimestamp(item.getDateIso(), item.getTime()));
        holder.detail.setText(item.getDetail());
        holder.status.setText(item.getStatus());
        holder.status.setTextColor(item.getStatusColor());

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        if (params != null) {
            params.topMargin = position == 0 ? 0 : holder.itemView.getResources().getDimensionPixelSize(R.dimen.space_12);
            holder.itemView.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LogEntryViewHolder extends RecyclerView.ViewHolder {
        private final View accent;
        private final View iconContainer;
        private final ImageView icon;
        private final TextView title;
        private final TextView subtitle;
        private final TextView time;
        private final TextView detail;
        private final TextView status;

        LogEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            accent = itemView.findViewById(R.id.viewLogAccent);
            iconContainer = itemView.findViewById(R.id.logIconContainer);
            icon = itemView.findViewById(R.id.ivLogIcon);
            title = itemView.findViewById(R.id.tvLogTitle);
            subtitle = itemView.findViewById(R.id.tvLogSubtitle);
            time = itemView.findViewById(R.id.tvLogTime);
            detail = itemView.findViewById(R.id.tvLogDetail);
            status = itemView.findViewById(R.id.tvLogStatus);
        }
    }
}
