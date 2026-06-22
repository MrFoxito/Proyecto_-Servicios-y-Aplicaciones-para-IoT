package com.example.proyecto_iot.usuario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.proyecto_iot.R;

import java.util.ArrayList;
import java.util.List;

public class UsuarioProjectGalleryAdapter extends RecyclerView.Adapter<UsuarioProjectGalleryAdapter.GalleryViewHolder> {

    private final List<String> imageUrls = new ArrayList<>();
    private final OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(int position);
    }

    public UsuarioProjectGalleryAdapter(OnImageClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<String> urls) {
        imageUrls.clear();
        if (urls != null) {
            imageUrls.addAll(urls);
        }
        notifyDataSetChanged();
    }

    public List<String> getItems() {
        return imageUrls;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario_project_gallery_image, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        holder.bind(imageUrls.get(position), position);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivGalleryImage);
        }

        void bind(String url, int position) {
            Glide.with(imageView)
                    .load(url)
                    .centerCrop()
                    .into(imageView);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onImageClick(position);
                }
            });
        }
    }
}
