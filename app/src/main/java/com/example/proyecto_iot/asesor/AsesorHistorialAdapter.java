package com.example.proyecto_iot.asesor;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.entity.Cita;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AsesorHistorialAdapter extends RecyclerView.Adapter<AsesorHistorialAdapter.HistorialViewHolder> {

    private List<Cita> citas;
    private OnCitaClickListener listener;

    public interface OnCitaClickListener {
        void onCitaClick(Cita cita);
    }

    public AsesorHistorialAdapter(List<Cita> citas, OnCitaClickListener listener) {
        this.citas = citas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asesor_historial_cita, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        Cita cita = citas.get(position);

        // Fecha formateada: "18 ABR · 09:30 AM"
        try {
            SimpleDateFormat inputSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputSdf = new SimpleDateFormat("dd MMM", new Locale("es", "ES"));
            String dateFormatted = outputSdf.format(inputSdf.parse(cita.getDate())).toUpperCase();
            holder.txtFecha.setText(dateFormatted + " · " + cita.getTime());
        } catch (Exception e) {
            holder.txtFecha.setText(cita.getDate() + " · " + cita.getTime());
        }

        holder.txtPropiedad.setText(cita.getPropertyName());
        holder.txtCliente.setText(cita.getClientName());

        // Proyecto chip: ocultar si está vacío
        if (cita.getProyecto() != null && !cita.getProyecto().isEmpty()) {
            holder.txtProyecto.setVisibility(View.VISIBLE);
            holder.txtProyecto.setText(cita.getProyecto());
        } else {
            holder.txtProyecto.setVisibility(View.GONE);
        }

        // Badge de estado con color
        holder.txtStatus.setText(cita.getStatus().toUpperCase());
        switch (cita.getStatus().toLowerCase()) {
            case "cerrada":
            case "confirmada":
                holder.txtStatus.setBackgroundResource(R.drawable.as_status_green);
                holder.txtStatus.setTextColor(Color.parseColor("#0A2D3A"));
                break;
            case "en camino":
                holder.txtStatus.setBackgroundResource(R.drawable.as_status_blue);
                holder.txtStatus.setTextColor(Color.parseColor("#0A2D3A"));
                break;
            case "pendiente":
            case "reprogramada":
                holder.txtStatus.setBackgroundResource(R.drawable.as_status_pending);
                holder.txtStatus.setTextColor(Color.parseColor("#0A2D3A"));
                break;
            default: // pasada, no conectada
                holder.txtStatus.setBackgroundResource(R.drawable.as_chip_light);
                holder.txtStatus.setTextColor(Color.parseColor("#746D4A"));
                break;
        }

        // Badge CIERRE
        holder.badgeCierre.setVisibility(cita.hasCierre() ? View.VISIBLE : View.GONE);

        // Texto de acción según estado
        if (cita.hasCierre()) {
            holder.txtAccion.setText("Ver separación asociada");
        } else if ("Pendiente".equalsIgnoreCase(cita.getStatus())) {
            holder.txtAccion.setText("Pendiente de seguimiento");
        } else {
            holder.txtAccion.setText("Ver detalle de cita");
        }

        holder.itemView.setOnClickListener(v -> listener.onCitaClick(cita));
        holder.btnDetalle.setOnClickListener(v -> listener.onCitaClick(cita));
    }

    @Override
    public int getItemCount() {
        return citas.size();
    }

    static class HistorialViewHolder extends RecyclerView.ViewHolder {
        TextView txtFecha, txtStatus, badgeCierre, txtPropiedad, txtCliente, txtProyecto, txtAccion;
        ImageButton btnDetalle;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            txtFecha       = itemView.findViewById(R.id.txtHcFecha);
            txtStatus      = itemView.findViewById(R.id.txtHcStatus);
            badgeCierre    = itemView.findViewById(R.id.badgeCierre);
            txtPropiedad   = itemView.findViewById(R.id.txtHcPropiedad);
            txtCliente     = itemView.findViewById(R.id.txtHcCliente);
            txtProyecto    = itemView.findViewById(R.id.txtHcProyecto);
            txtAccion      = itemView.findViewById(R.id.txtHcAccion);
            btnDetalle     = itemView.findViewById(R.id.btnHcDetalle);
        }
    }
}
