package com.example.proyecto_iot.asesor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.entity.MensajeChat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MensajeChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_DATE = 0;
    private static final int VIEW_TYPE_RECEIVED = 1;
    private static final int VIEW_TYPE_SENT = 2;

    private List<MensajeChat> mensajes = new ArrayList<>();

    public void setMensajes(List<MensajeChat> mensajes) {
        this.mensajes = mensajes;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        MensajeChat msg = mensajes.get(position);
        if (msg.isDateHeader()) {
            return VIEW_TYPE_DATE;
        } else if (msg.isSentByMe()) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_DATE) {
            return new DateViewHolder(inflater.inflate(R.layout.item_asesor_chat_date, parent, false));
        } else if (viewType == VIEW_TYPE_SENT) {
            return new MensajeViewHolder(inflater.inflate(R.layout.item_asesor_mensaje_enviado, parent, false));
        } else {
            return new MensajeViewHolder(inflater.inflate(R.layout.item_asesor_mensaje_recibido, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MensajeChat msg = mensajes.get(position);

        if (holder instanceof DateViewHolder) {
            ((DateViewHolder) holder).txtDateHeader.setText(formatDateHeader(msg.getDate()));
        } else if (holder instanceof MensajeViewHolder) {
            MensajeViewHolder mh = (MensajeViewHolder) holder;
            mh.txtMensaje.setText(msg.getTexto());
            mh.txtHora.setText(msg.getTime());
        }
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    private String formatDateHeader(String date) {
        if (date == null) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date msgDate = sdf.parse(date);
            Date today = new Date();
            String todayStr = sdf.format(today);

            if (todayStr.equals(date)) {
                return "Hoy";
            } else {
                SimpleDateFormat displayFormat = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
                return displayFormat.format(msgDate);
            }
        } catch (Exception e) {
            return date;
        }
    }

    static class MensajeViewHolder extends RecyclerView.ViewHolder {
        TextView txtMensaje, txtHora;
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