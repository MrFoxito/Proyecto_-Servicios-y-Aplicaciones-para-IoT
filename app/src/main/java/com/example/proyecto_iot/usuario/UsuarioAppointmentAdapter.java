package com.example.proyecto_iot.usuario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;

import java.util.List;

public class UsuarioAppointmentAdapter extends RecyclerView.Adapter<UsuarioAppointmentAdapter.AppointmentViewHolder> {

    public interface OnAppointmentClickListener {
        void onAppointmentClick(UsuarioAppointmentItem item);
    }

    private final List<UsuarioAppointmentItem> items;
    private final OnAppointmentClickListener clickListener;

    public UsuarioAppointmentAdapter(List<UsuarioAppointmentItem> items, OnAppointmentClickListener clickListener) {
        this.items = items;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario_actividad_cita, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        UsuarioAppointmentItem item = items.get(position);
        holder.image.setImageResource(item.getImageResId());
        holder.title.setText(item.getTitle());
        holder.status.setText(item.getStatus());
        holder.dateTime.setText(item.getDateTime());
        holder.advisor.setText(item.getAdvisor());
        holder.itemView.setOnClickListener(v -> clickListener.onAppointmentClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final ImageView image;
        private final TextView title;
        private final TextView status;
        private final TextView dateTime;
        private final TextView advisor;

        AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ivAppointmentImage);
            title = itemView.findViewById(R.id.tvAppointmentTitle);
            status = itemView.findViewById(R.id.tvAppointmentStatus);
            dateTime = itemView.findViewById(R.id.tvAppointmentDateTime);
            advisor = itemView.findViewById(R.id.tvAppointmentAdvisor);
        }
    }
}
