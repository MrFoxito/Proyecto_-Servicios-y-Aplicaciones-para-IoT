package com.example.proyecto_iot.superadmin;

import com.example.proyecto_iot.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SuperadminSolicitudAsesorAdapter extends RecyclerView.Adapter<SuperadminSolicitudAsesorAdapter.SolicitudViewHolder> {
    private final List<SuperadminSolicitudAsesorItem> items;

    public SuperadminSolicitudAsesorAdapter(List<SuperadminSolicitudAsesorItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public SolicitudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sa_asesor_solicitud, parent, false);
        return new SolicitudViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SolicitudViewHolder holder, int position) {
        SuperadminSolicitudAsesorItem item = items.get(position);
        holder.avatar.setImageResource(item.getAvatarResId());
        holder.name.setText(item.getName());
        holder.email.setText(item.getEmail());
        holder.agency.setText(item.getAgency());
        holder.status.setText(item.getStatus());

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

    static class SolicitudViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView name;
        private final TextView email;
        private final TextView agency;
        private final TextView status;

        SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.ivAdvisorAvatar);
            name = itemView.findViewById(R.id.tvAdvisorName);
            email = itemView.findViewById(R.id.tvAdvisorEmail);
            agency = itemView.findViewById(R.id.tvAdvisorAgency);
            status = itemView.findViewById(R.id.tvAdvisorStatus);
        }
    }
}


