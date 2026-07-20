package com.example.proyecto_iot.superadmin;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.ProfileAvatarLoader;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SuperadminControlAccesoAdapter extends RecyclerView.Adapter<SuperadminControlAccesoAdapter.ControlAccesoViewHolder> {
    private final List<SuperadminControlAccesoItem> items;

    public SuperadminControlAccesoAdapter(List<SuperadminControlAccesoItem> items) {
        this.items = items;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ControlAccesoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ControlAccesoViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sa_control_acceso, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ControlAccesoViewHolder holder, int position) {
        SuperadminControlAccesoItem item = items.get(position);
        ProfileAvatarLoader.load(holder.avatar, item.getAvatarUrl(), item.getName(),
                R.drawable.sa_avatar_placeholder);
        holder.name.setText(item.getName());
        holder.role.setText(item.getRole());

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        if (params != null) {
            params.topMargin = position == 0 ? 0 : holder.itemView.getResources().getDimensionPixelSize(R.dimen.space_8);
            holder.itemView.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getUid().hashCode();
    }

    static class ControlAccesoViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView name;
        private final TextView role;

        ControlAccesoViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.ivAccessAvatar);
            name = itemView.findViewById(R.id.tvAccessName);
            role = itemView.findViewById(R.id.tvAccessRole);
        }
    }
}

