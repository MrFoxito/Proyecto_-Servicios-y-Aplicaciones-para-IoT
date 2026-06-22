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
import com.example.proyecto_iot.entity.Chat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<Chat> chatList = new ArrayList<>();
    private final OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public ChatAdapter(OnChatClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asesor_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        // Nombre del cliente
        holder.txtNombre.setText(chat.getClienteNombre() != null ? chat.getClienteNombre() : "Cliente");

        // Último mensaje
        holder.txtUltimoMensaje.setText(chat.getUltimoMensaje() != null ? chat.getUltimoMensaje() : "");

        // Hora del último mensaje (formatear desde fechaHora si está disponible)
        // Podrías agregar un campo en Chat para almacenar la fecha del último mensaje.
        // Por ahora usamos un placeholder.
        if (chat.getUltimoMensajeFecha() != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(chat.getUltimoMensajeFecha());
                SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                holder.txtHora.setText(outputFormat.format(date));
            } catch (Exception e) {
                holder.txtHora.setText("");
            }
        } else {
            holder.txtHora.setText("");
        }

        // Avatar del cliente
        String avatarUrl = chat.getClienteAvatarUrl();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.sa_profile_asesor_3)
                    .circleCrop()
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.sa_profile_asesor_3);
        }

        // Indicador de no leído
        if (chat.isUnread()) {
            holder.indicatorUnread.setVisibility(View.VISIBLE);
        } else {
            holder.indicatorUnread.setVisibility(View.GONE);
        }

        // Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void updateList(List<Chat> newList) {
        this.chatList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void filter(String query) {
        // Si quieres filtrar por nombre, implementa según tu lógica.
        // Por ahora, simplemente actualiza toda la lista.
        // Si necesitas filtro, puedes mantener una copia completa y aplicar filtro.
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtNombre, txtUltimoMensaje, txtHora;
        View indicatorUnread;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtUltimoMensaje = itemView.findViewById(R.id.txtUltimoMensaje);
            txtHora = itemView.findViewById(R.id.txtHora);
            indicatorUnread = itemView.findViewById(R.id.indicatorUnread);
        }
    }
}