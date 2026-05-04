package com.example.proyecto_iot.asesor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.entity.Cita;
import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {

    private List<Cita> citaList;
    private OnCitaClickListener listener;

    public interface OnCitaClickListener {
        void onCitaClick(Cita cita);
    }

    public TimelineAdapter(List<Cita> citaList, OnCitaClickListener listener) {
        this.citaList = citaList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cita_timeline, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        Cita cita = citaList.get(position);
        
        // Split time for formatting
        String[] timeParts = cita.getTime().split(" ");
        holder.txtCitaTime.setText(timeParts[0]);
        holder.txtCitaAmPm.setText(timeParts.length > 1 ? timeParts[1] : "");

        holder.txtCitaStatus.setText(cita.getStatus().toUpperCase());
        holder.txtCitaTitle.setText(cita.getClientName()); // Or a combined title
        holder.txtCitaProperty.setText(cita.getPropertyName());


        // Apply status colors/styles if needed
        if ("Pasada".equalsIgnoreCase(cita.getStatus())) {
            holder.txtCitaStatus.setBackgroundResource(R.drawable.as_chip_light);
        } else if ("Confirmada".equalsIgnoreCase(cita.getStatus())) {
            holder.txtCitaStatus.setBackgroundResource(R.drawable.as_status_green);
        } else {
            holder.txtCitaStatus.setBackgroundResource(R.drawable.as_status_pending);
        }

        holder.itemView.setOnClickListener(v -> listener.onCitaClick(cita));
    }

    @Override
    public int getItemCount() {
        return citaList.size();
    }

    static class TimelineViewHolder extends RecyclerView.ViewHolder {
        TextView txtCitaTime, txtCitaAmPm, txtCitaStatus, txtCitaTitle, txtCitaProperty;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCitaTime = itemView.findViewById(R.id.txtCitaTime);
            txtCitaAmPm = itemView.findViewById(R.id.txtCitaAmPm);
            txtCitaStatus = itemView.findViewById(R.id.txtCitaStatus);
            txtCitaTitle = itemView.findViewById(R.id.txtCitaTitle);
            txtCitaProperty = itemView.findViewById(R.id.txtCitaProperty);
        }
    }
}
