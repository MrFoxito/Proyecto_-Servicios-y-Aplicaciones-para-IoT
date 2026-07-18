package com.example.proyecto_iot.superadmin;

import com.example.proyecto_iot.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SuperadminResumenLogAdapter extends RecyclerView.Adapter<SuperadminResumenLogAdapter.ResumenLogViewHolder> {
    private final List<SuperadminResumenLogItem> items;

    public SuperadminResumenLogAdapter(List<SuperadminResumenLogItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ResumenLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sa_resumen_log, parent, false);
        return new ResumenLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResumenLogViewHolder holder, int position) {
        SuperadminResumenLogItem item = items.get(position);
        holder.accent.setBackgroundColor(item.getAccentColor());
        holder.label.setText(item.getLabel());
        holder.label.setTextColor(item.getLabelColor());
        holder.message.setText(item.getMessage());

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        if (params != null) {
            params.topMargin = position == 0 ? 0 : holder.itemView.getResources().getDimensionPixelSize(R.dimen.space_12);
            holder.itemView.setLayoutParams(params);
        }

        holder.itemView.setOnClickListener(v -> {
            v.getContext().startActivity(new android.content.Intent(v.getContext(), SuperadminLogsActivity.class));
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ResumenLogViewHolder extends RecyclerView.ViewHolder {
        private final View accent;
        private final TextView label;
        private final TextView message;

        ResumenLogViewHolder(@NonNull View itemView) {
            super(itemView);
            accent = itemView.findViewById(R.id.viewResumenLogAccent);
            label = itemView.findViewById(R.id.tvResumenLogLabel);
            message = itemView.findViewById(R.id.tvResumenLogMessage);
        }
    }
}

