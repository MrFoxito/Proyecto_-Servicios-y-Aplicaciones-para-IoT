package com.example.proyecto_iot.asesor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.entity.Separacion;
import java.util.List;

public class SeparacionAdapter extends RecyclerView.Adapter<SeparacionAdapter.SeparacionViewHolder> {

    private List<Separacion> separacionList;
    private OnSeparacionActionListener listener;

    public interface OnSeparacionActionListener {
        void onVerDetalles(Separacion separacion);
        void onAprobarPago(Separacion separacion);
    }

    public SeparacionAdapter(List<Separacion> separacionList, OnSeparacionActionListener listener) {
        this.separacionList = separacionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SeparacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asesor_separacion, parent, false);
        return new SeparacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeparacionViewHolder holder, int position) {
        Separacion separacion = separacionList.get(position);
        holder.txtId.setText("ID: " + separacion.getId());
        holder.txtClient.setText("Cliente: " + separacion.getClientName());
        holder.txtPropertyName.setText(separacion.getPropertyName());
        holder.txtPrice.setText(separacion.getPrice());
        holder.txtDate.setText(separacion.getDate());
        holder.txtStatus.setText(separacion.getStatus().toUpperCase());
        holder.imgProperty.setImageResource(separacion.getImageRes());

        holder.btnVerDetalles.setOnClickListener(v -> listener.onVerDetalles(separacion));
        holder.btnAccionPrincipal.setOnClickListener(v -> listener.onAprobarPago(separacion));
    }

    @Override
    public int getItemCount() {
        return separacionList.size();
    }

    static class SeparacionViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProperty;
        TextView txtId, txtStatus, txtClient, txtDate, txtPropertyName, txtPrice, btnVerDetalles, btnAccionPrincipal;

        public SeparacionViewHolder(@NonNull View itemView) {
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
