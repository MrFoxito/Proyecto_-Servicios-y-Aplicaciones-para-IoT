package com.example.proyecto_iot.usuario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.data.ProjectImageLoader;
import com.example.proyecto_iot.R;

import java.util.ArrayList;
import java.util.List;

public class UsuarioPropertyListAdapter extends RecyclerView.Adapter<UsuarioPropertyListAdapter.PropertyViewHolder> {

    public interface OnPropertyClickListener {
        void onPropertyClick(UsuarioPropertyListItem item);
    }

    private final List<UsuarioPropertyListItem> items = new ArrayList<>();
    private final OnPropertyClickListener clickListener;

    public UsuarioPropertyListAdapter(List<UsuarioPropertyListItem> items, OnPropertyClickListener clickListener) {
        this.items.addAll(items);
        this.clickListener = clickListener;
    }

    public void setItems(List<UsuarioPropertyListItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario_propiedad_listado, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        UsuarioPropertyListItem item = items.get(position);
        holder.label.setText(item.getLabel());
        holder.title.setText(item.getTitle());
        holder.location.setText(item.getLocation());
//        holder.typology.setText(item.getTypologiesSummary().isEmpty() ? "Tipologia por definir" : item.getTypologiesSummary());
        holder.price.setText(item.getPrice());
        ProjectImageLoader.load(holder.image, item.getImageUrl(), item.getImageResId());
        holder.itemView.setOnClickListener(v -> clickListener.onPropertyClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PropertyViewHolder extends RecyclerView.ViewHolder {
        private final TextView label;
        private final TextView title;
        private final TextView location;
        private final TextView typology;
        private final TextView price;
        private final ImageView image;

        PropertyViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.tvPropertyItemLabel);
            title = itemView.findViewById(R.id.tvPropertyItemTitle);
            location = itemView.findViewById(R.id.tvPropertyItemLocation);
            typology = itemView.findViewById(R.id.tvPropertyItemTypology);
            price = itemView.findViewById(R.id.tvPropertyItemPrice);
            image = itemView.findViewById(R.id.ivPropertyItemImage);
        }
    }
}
