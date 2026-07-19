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

        String projectName = chat.getProjectName();
        holder.txtNombre.setText(projectName == null || projectName.trim().isEmpty() ? "Proyecto" : projectName);
        holder.txtParticipante.setText("Cliente: " + (chat.getClienteNombre() != null ? chat.getClienteNombre() : "Cliente"));

        String lastMessage = chat.getUltimoMensaje() != null ? chat.getUltimoMensaje() : "";
        holder.txtUltimoMensaje.setText(lastMessage);

        // Hora del último mensaje (formatear desde lastMessageAt)
        if (chat.getLastMessageAt() > 0) {
            try {
                Date date = new Date(chat.getLastMessageAt());
                SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                holder.txtHora.setText(outputFormat.format(date));
            } catch (Exception e) {
                holder.txtHora.setText("");
            }
        } else {
            holder.txtHora.setText("");
        }

        String projectImageUrl = chat.getProjectImageUrl();
        if (projectImageUrl != null && !projectImageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(projectImageUrl)
                    .placeholder(R.drawable.user_featured_house)
                    .error(R.drawable.user_featured_house)
                    .centerCrop()
                    .into(holder.imgAvatar);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.user_featured_house);
        }

        // Indicador de no leído
        holder.indicatorUnread.setVisibility(chat.isUnread() ? View.VISIBLE : View.GONE);

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

    /**
     * Filtra la lista por nombre del cliente o por contenido del mensaje.
     * @param query Texto a buscar (case-insensitive)
     */
    public void filter(String query) {
        // Si necesitas filtro, puedes implementarlo aquí.
        // Por ahora, este método no hace nada para no romper,
        // pero puedes añadir lógica si lo deseas.
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtNombre, txtParticipante, txtUltimoMensaje, txtHora;
        View indicatorUnread;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtParticipante = itemView.findViewById(R.id.txtParticipante);
            txtUltimoMensaje = itemView.findViewById(R.id.txtUltimoMensaje);
            txtHora = itemView.findViewById(R.id.txtHora);
            indicatorUnread = itemView.findViewById(R.id.indicatorUnread);
        }
    }
}
