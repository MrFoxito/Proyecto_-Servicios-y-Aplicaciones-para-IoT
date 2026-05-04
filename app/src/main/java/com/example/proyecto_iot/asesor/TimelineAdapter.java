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

public class TimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;

    private List<Object> items;
    private OnCitaClickListener listener;

    public interface OnCitaClickListener {
        void onCitaClick(Cita cita);
    }

    public TimelineAdapter(List<Object> items, OnCitaClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof String) {
            return TYPE_SEPARATOR;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_SEPARATOR) {
            return new SeparatorViewHolder(inflater.inflate(R.layout.item_timeline_separator, parent, false));
        } else {
            return new TimelineViewHolder(inflater.inflate(R.layout.item_asesor_cita_timeline, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == TYPE_SEPARATOR) {
            String dateLabel = (String) items.get(position);
            ((SeparatorViewHolder) holder).txtSeparatorDate.setText(dateLabel);
        } else {
            Cita cita = (Cita) items.get(position);
            TimelineViewHolder itemHolder = (TimelineViewHolder) holder;
            
            String[] timeParts = cita.getTime().split(" ");
            itemHolder.txtCitaTime.setText(timeParts[0]);
            itemHolder.txtCitaAmPm.setText(timeParts.length > 1 ? timeParts[1] : "");

            itemHolder.txtCitaStatus.setText(cita.getStatus().toUpperCase());
            itemHolder.txtCitaTitle.setText(cita.getClientName());
            itemHolder.txtCitaProperty.setText(cita.getPropertyName());

            if ("Pasada".equalsIgnoreCase(cita.getStatus())) {
                itemHolder.txtCitaStatus.setBackgroundResource(R.drawable.as_chip_light);
            } else if ("Confirmada".equalsIgnoreCase(cita.getStatus())) {
                itemHolder.txtCitaStatus.setBackgroundResource(R.drawable.as_status_green);
            } else {
                itemHolder.txtCitaStatus.setBackgroundResource(R.drawable.as_status_pending);
            }

            itemHolder.itemView.setOnClickListener(v -> listener.onCitaClick(cita));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TimelineViewHolder extends RecyclerView.ViewHolder {
        TextView txtCitaTime, txtCitaAmPm, txtCitaStatus, txtCitaTitle, txtCitaProperty;
        ImageView imgCitaProperty;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCitaTime = itemView.findViewById(R.id.txtCitaTime);
            txtCitaAmPm = itemView.findViewById(R.id.txtCitaAmPm);
            txtCitaStatus = itemView.findViewById(R.id.txtCitaStatus);
            txtCitaTitle = itemView.findViewById(R.id.txtCitaTitle);
            txtCitaProperty = itemView.findViewById(R.id.txtCitaProperty);
        }
    }

    static class SeparatorViewHolder extends RecyclerView.ViewHolder {
        TextView txtSeparatorDate;
        public SeparatorViewHolder(@NonNull View itemView) {
            super(itemView);
            txtSeparatorDate = itemView.findViewById(R.id.txtSeparatorDate);
        }
    }


}
