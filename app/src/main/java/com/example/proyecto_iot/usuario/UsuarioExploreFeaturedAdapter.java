package com.example.proyecto_iot.usuario;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.ProjectImageLoader;

import java.util.ArrayList;
import java.util.List;

/** Horizontal presentation-only carousel for the first Explore page. */
public class UsuarioExploreFeaturedAdapter extends RecyclerView.Adapter<UsuarioExploreFeaturedAdapter.ViewHolder> {
    public interface OnProjectClickListener {
        void onProjectClick(UsuarioPropertyListItem item);
    }

    private final List<UsuarioPropertyListItem> items = new ArrayList<>();
    private final OnProjectClickListener clickListener;
    private boolean loading = true;

    public UsuarioExploreFeaturedAdapter(OnProjectClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setItems(List<UsuarioPropertyListItem> newItems, boolean isLoading) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        loading = isLoading;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario_explore_featured_project, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (loading) {
            holder.image.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
            holder.image.setBackgroundResource(R.drawable.explore_skeleton_bg);
            holder.title.setText("");
            holder.title.setBackgroundResource(R.drawable.explore_skeleton_bg);
            holder.location.setText("");
            holder.location.setBackgroundResource(R.drawable.explore_skeleton_bg);
            holder.badge.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
            return;
        }

        UsuarioPropertyListItem item = items.get(position);
        holder.image.setBackground(null);
        ProjectImageLoader.load(holder.image, item.getImageUrl(), item.getImageResId());
        holder.title.setBackground(null);
        holder.title.setText(item.getTitle());
        holder.location.setBackground(null);
        String location = item.getExploreLocation();
        holder.location.setText(location.isEmpty() ? "Ubicación por definir" : location);
        String badge = item.getLabel();
        holder.badge.setText(badge);
        holder.badge.setVisibility(badge == null || badge.trim().isEmpty() ? View.GONE : View.VISIBLE);
        holder.itemView.setOnClickListener(v -> clickListener.onProjectClick(item));
    }

    @Override
    public int getItemCount() {
        return loading ? 2 : items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView badge;
        final TextView title;
        final TextView location;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ivExploreFeaturedImage);
            badge = itemView.findViewById(R.id.tvExploreFeaturedBadge);
            title = itemView.findViewById(R.id.tvExploreFeaturedTitle);
            location = itemView.findViewById(R.id.tvExploreFeaturedLocation);
        }
    }
}
