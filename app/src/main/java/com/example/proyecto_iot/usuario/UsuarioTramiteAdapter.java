package com.example.proyecto_iot.usuario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;

import java.util.List;

public class UsuarioTramiteAdapter extends RecyclerView.Adapter<UsuarioTramiteAdapter.TramiteViewHolder> {

    public interface OnTramiteClickListener {
        void onTramiteClick(UsuarioTramiteItem item);
    }

    private final List<UsuarioTramiteItem> items;
    private final OnTramiteClickListener clickListener;

    public UsuarioTramiteAdapter(List<UsuarioTramiteItem> items, OnTramiteClickListener clickListener) {
        this.items = items;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public TramiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario_actividad_tramite, parent, false);
        return new TramiteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TramiteViewHolder holder, int position) {
        UsuarioTramiteItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.id.setText(item.getId());
        holder.status.setText(item.getStatus());
        holder.note.setText(item.getNote());
        holder.due.setText(item.getDue());
        holder.status.setTextColor(ContextCompat.getColor(
                holder.itemView.getContext(),
                item.canPay() ? R.color.app_accent_gold : R.color.app_text_secondary
        ));
        holder.itemView.setOnClickListener(v -> clickListener.onTramiteClick(item));
        holder.viewDetails.setOnClickListener(v -> clickListener.onTramiteClick(item));
        holder.progress.setVisibility(item.canPay() ? View.GONE : View.VISIBLE);
        holder.divider.setVisibility(item.canPay() ? View.VISIBLE : View.GONE);
        holder.footer.setVisibility(item.canPay() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TramiteViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView id;
        private final TextView status;
        private final TextView note;
        private final TextView due;
        private final TextView viewDetails;
        private final View progress;
        private final View divider;
        private final View footer;

        TramiteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTramiteItemTitle);
            id = itemView.findViewById(R.id.tvTramiteItemId);
            status = itemView.findViewById(R.id.tvTramiteItemStatus);
            note = itemView.findViewById(R.id.tvTramiteItemNote);
            due = itemView.findViewById(R.id.tvTramiteItemDue);
            viewDetails = itemView.findViewById(R.id.btnTramiteItemDetails);
            progress = itemView.findViewById(R.id.tramiteProgressContainer);
            divider = itemView.findViewById(R.id.tramiteDivider);
            footer = itemView.findViewById(R.id.tramiteFooter);
        }
    }
}
