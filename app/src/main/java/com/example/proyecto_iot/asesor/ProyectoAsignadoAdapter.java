package com.example.proyecto_iot.asesor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyecto_iot.R;
//import com.example.proyecto_iot.asesor.ProyectoDetalleActivity;
import com.example.proyecto_iot.entity.Proyecto;
import com.example.proyecto_iot.data.ProjectImageLoader;

import java.util.List;

public class ProyectoAsignadoAdapter extends RecyclerView.Adapter<ProyectoAsignadoAdapter.ViewHolder> {

    private Context context;
    private List<Proyecto> proyectos;

    public ProyectoAsignadoAdapter(Context context, List<Proyecto> proyectos) {
        this.context = context;
        this.proyectos = proyectos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_asesor_proyecto_asignado, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Proyecto proyecto = proyectos.get(position);

        holder.txtNombre.setText(proyecto.getNombre());
        holder.txtDireccion.setText(proyecto.getDireccion());
        holder.txtBadge.setText(proyecto.getEstado() != null ? proyecto.getEstado() : "En venta");
        holder.txtPrecio.setText(proyecto.getPrecioDesde());

        // Mostrar distrito solo si no está vacío
        if (proyecto.getDistrito() != null && !proyecto.getDistrito().isEmpty()) {
            holder.txtDistrito.setText(proyecto.getDistrito());
            holder.txtDistrito.setVisibility(View.VISIBLE);
        } else {
            holder.txtDistrito.setVisibility(View.GONE);
        }

        // Cargar imagen (con Glide)
        String imageUrl = proyecto.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ProjectImageLoader.load(holder.imgProyecto, imageUrl, R.drawable.user_property_hero_real);
        } else {
            holder.imgProyecto.setImageResource(R.drawable.user_property_hero_real);
        }

        // Click para abrir detalle
        holder.itemView.setOnClickListener(v -> {
//            Intent intent = new Intent(context, ProyectoDetalleActivity.class);
//            intent.putExtra("proyectoId", proyecto.getId());
//            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return proyectos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProyecto;
        TextView txtBadge, txtNombre, txtDireccion, txtDistrito, txtPrecio;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProyecto = itemView.findViewById(R.id.imgProyecto);
            txtBadge = itemView.findViewById(R.id.txtBadge);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtDireccion = itemView.findViewById(R.id.txtDireccion);
            txtDistrito = itemView.findViewById(R.id.txtDistrito);
            txtPrecio = itemView.findViewById(R.id.txtPrecio);
        }
    }
}
