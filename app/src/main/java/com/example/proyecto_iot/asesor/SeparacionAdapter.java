package com.example.proyecto_iot.asesor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.entity.Separacion;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SeparacionAdapter extends RecyclerView.Adapter<SeparacionAdapter.ViewHolder> {

    private List<Separacion> separaciones = new ArrayList<>();
    private final OnSeparacionActionListener listener;

    public interface OnSeparacionActionListener {
        /**
         * Se llama cuando el usuario hace clic en "Gestionar Solicitud" o "Ver Detalles"
         * @param separacion La separación seleccionada
         */
        void onVerDetalles(Separacion separacion);

        /**
         * Se llama cuando el usuario hace clic en el botón de acción principal
         * (Validar Pago, Aprobar, etc.)
         * @param separacion La separación que se va a aprobar/validar
         */
        void onAccionPrincipal(Separacion separacion);
    }

    public SeparacionAdapter(OnSeparacionActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asesor_separacion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Separacion sep = separaciones.get(position);

        // ID o número de separación
        String idDisplay = sep.getId() != null ? "ID: " + sep.getId().substring(0, Math.min(sep.getId().length(), 8)) : "ID: ---";
        holder.txtId.setText(idDisplay);

        // Estado con color y texto
        String estado = sep.getEstado() != null ? sep.getEstado() : "Pendiente";
        holder.txtStatus.setText(estado.toUpperCase());
        applyStatusStyle(holder.txtStatus, estado);

        // Cliente
        holder.txtClient.setText("Cliente: " + (sep.getClienteNombre() != null ? sep.getClienteNombre() : "---"));

        // Fecha
        if (sep.getCreatedAt() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", new Locale("es", "ES"));
            String fecha = sdf.format(new Date(sep.getCreatedAt()));
            holder.txtDate.setText("Solicitado: " + fecha);
        } else if (sep.getFechaTexto() != null) {
            holder.txtDate.setText("Solicitado: " + sep.getFechaTexto());
        } else {
            holder.txtDate.setText("Solicitado: ---");
        }

        // Nombre del inmueble
        holder.txtPropertyName.setText(sep.getInmuebleNombre() != null ? sep.getInmuebleNombre() : "Sin nombre");

        // Monto
        holder.txtPrice.setText(sep.getMontoTexto() != null ? sep.getMontoTexto() : "$---");

        // Imagen (placeholder, puedes usar Glide si tienes URL)
        // Si tienes una URL de imagen, puedes cargarla. Por ahora usamos placeholder.
        holder.imgProperty.setImageResource(R.drawable.as_property_04);

        // --- Configurar botones según estado ---
        // Botón de ver detalles siempre visible
        holder.btnVerDetalles.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVerDetalles(sep);
            }
        });

        // Botón de acción principal cambia según estado
        String estadoLower = estado.toLowerCase();
        switch (estadoLower) {
            case "pendiente":
                holder.btnAccionPrincipal.setText("Validar Pago");
                holder.btnAccionPrincipal.setBackgroundResource(R.drawable.as_button_dark);
                holder.btnAccionPrincipal.setVisibility(View.VISIBLE);
                holder.btnAccionPrincipal.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAccionPrincipal(sep);
                    }
                });
                break;

            case "pagada":
                holder.btnAccionPrincipal.setText("Aprobar");
                holder.btnAccionPrincipal.setBackgroundResource(R.drawable.as_button_gold);
                holder.btnAccionPrincipal.setVisibility(View.VISIBLE);
                holder.btnAccionPrincipal.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAccionPrincipal(sep);
                    }
                });
                break;

            case "aprobada":
                holder.btnAccionPrincipal.setText("Ver Detalle");
                holder.btnAccionPrincipal.setBackgroundResource(R.drawable.as_card_soft);
                holder.btnAccionPrincipal.setVisibility(View.VISIBLE);
                holder.btnAccionPrincipal.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onVerDetalles(sep);
                    }
                });
                break;

            case "rechazada":
                // Ocultar botón de acción o mostrar "Ver Detalle"
                holder.btnAccionPrincipal.setText("Ver Detalle");
                holder.btnAccionPrincipal.setBackgroundResource(R.drawable.as_card_soft);
                holder.btnAccionPrincipal.setVisibility(View.VISIBLE);
                holder.btnAccionPrincipal.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onVerDetalles(sep);
                    }
                });
                break;

            default:
                holder.btnAccionPrincipal.setVisibility(View.GONE);
                break;
        }
    }

    private void applyStatusStyle(TextView tvStatus, String estado) {
        String estadoLower = estado.toLowerCase();
        switch (estadoLower) {
            case "pagada":
            case "aprobada":
                tvStatus.setBackgroundResource(R.drawable.as_status_green);
                tvStatus.setTextColor(android.graphics.Color.parseColor("#0A2D3A"));
                break;
            case "pendiente":
                tvStatus.setBackgroundResource(R.drawable.as_status_pending);
                tvStatus.setTextColor(android.graphics.Color.parseColor("#0A2D3A"));
                break;
            case "rechazada":
                tvStatus.setBackgroundResource(R.drawable.as_status_red);
                tvStatus.setTextColor(android.graphics.Color.WHITE);
                break;
            default:
                tvStatus.setBackgroundResource(R.drawable.as_chip_light);
                tvStatus.setTextColor(android.graphics.Color.parseColor("#746D4A"));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return separaciones.size();
    }

    public void updateList(List<Separacion> newList) {
        this.separaciones = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProperty;
        TextView txtId, txtStatus, txtClient, txtDate, txtPropertyName, txtPrice;
        TextView btnVerDetalles, btnAccionPrincipal;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProperty = itemView.findViewById(R.id.imgProperty);
            txtId = itemView.findViewById(R.id.txtId);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtClient = itemView.findViewById(R.id.txtClient);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtPropertyName = itemView.findViewById(R.id.txtPropertyName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            btnVerDetalles = itemView.findViewById(R.id.btnVerDetalles);
            btnAccionPrincipal = itemView.findViewById(R.id.btnAccionPrincipal);
        }
    }
}