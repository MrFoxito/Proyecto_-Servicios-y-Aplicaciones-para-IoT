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

        // --- Fecha formateada: "25 JUN · 10:00 AM" ---
        String fechaFormateada = cita.getFechaFormateada(); // "25 de junio de 2026" (largo)
        // Extraemos solo "25 JUN" para el formato corto
        String fechaCorta = "";
        try {
            SimpleDateFormat inputSdf = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
            SimpleDateFormat outputSdf = new SimpleDateFormat("dd MMM", new Locale("es", "ES"));
            fechaCorta = outputSdf.format(inputSdf.parse(fechaFormateada)).toUpperCase();
        } catch (Exception e) {
            // Fallback: usar fechaISO
            try {
                SimpleDateFormat inputSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputSdf = new SimpleDateFormat("dd MMM", new Locale("es", "ES"));
                fechaCorta = outputSdf.format(inputSdf.parse(cita.getFechaISO())).toUpperCase();
            } catch (Exception ex) {
                fechaCorta = cita.getFechaISO();
            }
        }

        String horaFormateada = cita.getHoraFormateada(); // "10:00 AM"
        holder.txtFecha.setText(fechaCorta + " · " + horaFormateada);

        // --- Propiedad y cliente ---
        holder.txtPropiedad.setText(cita.getProyectoNombre() != null ? cita.getProyectoNombre() : "Sin proyecto");
        holder.txtCliente.setText(cita.getClienteNombre() != null ? cita.getClienteNombre() : "Cliente");

        // --- Proyecto chip (si existe) ---
        String proyecto = cita.getProyectoNombre();
        if (proyecto != null && !proyecto.isEmpty()) {
            holder.txtProyecto.setVisibility(View.VISIBLE);
            holder.txtProyecto.setText(proyecto);
        } else {
            holder.txtProyecto.setVisibility(View.GONE);
        }

        // --- Badge de estado ---
        String estado = cita.getEstado() != null ? cita.getEstado() : "Pendiente";
        holder.txtStatus.setText(estado.toUpperCase());
        switch (estado.toLowerCase()) {
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
            default: // pasada, no conectada, cancelada
                holder.txtStatus.setBackgroundResource(R.drawable.as_chip_light);
                holder.txtStatus.setTextColor(Color.parseColor("#746D4A"));
                break;
        }

        // --- Badge de cierre ---
        holder.badgeCierre.setVisibility(cita.isHasCierre() ? View.VISIBLE : View.GONE);

        // --- Texto de acción dinámico ---
        if (cita.isHasCierre()) {
            holder.txtAccion.setText("Ver separación asociada");
        } else if ("Pendiente".equalsIgnoreCase(estado)) {
            holder.txtAccion.setText("Pendiente de seguimiento");
        } else {
            holder.txtAccion.setText("Ver detalle de cita");
        }

        // --- Click listeners ---
        holder.itemView.setOnClickListener(v -> listener.onCitaClick(cita));
        holder.btnDetalle.setOnClickListener(v -> listener.onCitaClick(cita));
    }

    @Override
    public int getItemCount() {
        return citas != null ? citas.size() : 0;
    }

    // Método para actualizar la lista desde fuera (útil con Firestore)
    public void setCitas(List<Cita> nuevasCitas) {
        this.citas = nuevasCitas;
        notifyDataSetChanged();
    }

    static class HistorialViewHolder extends RecyclerView.ViewHolder {
        TextView txtFecha, txtStatus, badgeCierre, txtPropiedad, txtCliente, txtProyecto, txtAccion;
        ImageButton btnDetalle;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            txtFecha = itemView.findViewById(R.id.txtHcFecha);
            txtStatus = itemView.findViewById(R.id.txtHcStatus);
            badgeCierre = itemView.findViewById(R.id.badgeCierre);
            txtPropiedad = itemView.findViewById(R.id.txtHcPropiedad);
            txtCliente = itemView.findViewById(R.id.txtHcCliente);
            txtProyecto = itemView.findViewById(R.id.txtHcProyecto);
            txtAccion = itemView.findViewById(R.id.txtHcAccion);
            btnDetalle = itemView.findViewById(R.id.btnHcDetalle);
        }
    }
}