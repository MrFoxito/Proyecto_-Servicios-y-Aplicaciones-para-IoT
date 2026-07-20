package com.example.proyecto_iot.asesor;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.entity.EventoCita;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventoCitaAdapter extends RecyclerView.Adapter<EventoCitaAdapter.EventoViewHolder> {

    private List<EventoCita> eventos;

    public EventoCitaAdapter(List<EventoCita> eventos) {
        this.eventos = eventos;
    }

    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asesor_evento_cita, parent, false);
        return new EventoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        EventoCita evento = eventos.get(position);
        
        holder.txtTitulo.setText(isBlank(evento.getTitulo()) ? "Actividad de la cita" : evento.getTitulo());
        
        if (evento.getDetalle() != null && !evento.getDetalle().isEmpty()) {
            holder.txtDetalle.setText(evento.getDetalle());
            holder.txtDetalle.setVisibility(View.VISIBLE);
        } else {
            holder.txtDetalle.setVisibility(View.GONE);
        }

        // Formatear fecha simple
        try {
            SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);
            Date date = isoFmt.parse(evento.getFechaHora());
            SimpleDateFormat displayFmt = new SimpleDateFormat("dd MMM HH:mm", new Locale("es", "ES"));
            holder.txtFecha.setText(displayFmt.format(date));
        } catch (Exception e) {
            holder.txtFecha.setText(isBlank(evento.getFechaHora()) ? "Fecha no disponible" : evento.getFechaHora());
        }

        // Los documentos heredados pueden no incluir tipo; se muestran como actividad neutra.
        String type = isBlank(evento.getTipo()) ? "INFO" : evento.getTipo().toUpperCase(Locale.ROOT);
        switch (type) {
            case "AGENDADA":
                holder.indicadorColor.setBackgroundColor(Color.parseColor("#9AA3AF"));
                break;
            case "CONFIRMADA":
                holder.indicadorColor.setBackgroundColor(Color.parseColor("#D6C65E"));
                break;
            case "REPROGRAMADA":
                holder.indicadorColor.setBackgroundColor(Color.parseColor("#E07A5F"));
                break;
            case "SEPARACION":
                holder.indicadorColor.setBackgroundColor(Color.parseColor("#8F7E00"));
                break;
            case "NOTA":
                holder.indicadorColor.setBackgroundColor(Color.parseColor("#4E5961"));
                break;
            default:
                holder.indicadorColor.setBackgroundColor(Color.parseColor("#9AA3AF"));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return eventos != null ? eventos.size() : 0;
    }

    public void setEventos(List<EventoCita> eventos) {
        this.eventos = eventos;
        notifyDataSetChanged();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    static class EventoViewHolder extends RecyclerView.ViewHolder {
        View indicadorColor;
        TextView txtTitulo;
        TextView txtFecha;
        TextView txtDetalle;

        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            indicadorColor = itemView.findViewById(R.id.indicadorColor);
            txtTitulo = itemView.findViewById(R.id.txtEventoTitulo);
            txtFecha = itemView.findViewById(R.id.txtEventoFecha);
            txtDetalle = itemView.findViewById(R.id.txtEventoDetalle);
        }
    }
}
