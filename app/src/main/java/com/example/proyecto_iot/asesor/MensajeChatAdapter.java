package com.example.proyecto_iot.asesor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.entity.MensajeChat;
import java.util.List;

public class MensajeChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_DATE = 0;
    private static final int VIEW_TYPE_IN = 1;
    private static final int VIEW_TYPE_OUT = 2;

    private List<MensajeChat> mensajes;

    public MensajeChatAdapter(List<MensajeChat> mensajes) {
        this.mensajes = mensajes;
    }

    @Override
    public int getItemViewType(int position) {
        MensajeChat mensaje = mensajes.get(position);
        if (mensaje.isDateHeader()) {
            return VIEW_TYPE_DATE;
        } else if (mensaje.isSentByMe()) {
            return VIEW_TYPE_OUT;
        } else {
            return VIEW_TYPE_IN;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_DATE) {
            View view = inflater.inflate(R.layout.item_asesor_chat_date, parent, false);
            return new DateViewHolder(view);
        } else if (viewType == VIEW_TYPE_OUT) {
            View view = inflater.inflate(R.layout.item_asesor_mensaje_enviado, parent, false);
            return new MensajeViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_asesor_mensaje_recibido, parent, false);
            return new MensajeViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MensajeChat mensaje = mensajes.get(position);
        if (holder instanceof DateViewHolder) {
            ((DateViewHolder) holder).txtDateHeader.setText(mensaje.getText());
        } else if (holder instanceof MensajeViewHolder) {
            MensajeViewHolder msgHolder = (MensajeViewHolder) holder;
            msgHolder.txtMensaje.setText(mensaje.getText());
            msgHolder.txtHora.setText(mensaje.getTime());
        }
    }

    @Override
    public int getItemCount() {
        return mensajes != null ? mensajes.size() : 0;
    }

    public void setMensajes(List<MensajeChat> mensajes) {
        this.mensajes = mensajes;
        notifyDataSetChanged();
    }

    static class MensajeViewHolder extends RecyclerView.ViewHolder {
        TextView txtMensaje;
        TextView txtHora;

        public MensajeViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMensaje = itemView.findViewById(R.id.txtMensaje);
            txtHora = itemView.findViewById(R.id.txtHora);
        }
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView txtDateHeader;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDateHeader = itemView.findViewById(R.id.txtDateHeader);
        }
    }
}
