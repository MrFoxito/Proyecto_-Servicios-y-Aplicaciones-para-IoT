package com.example.proyecto_iot.usuario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;

import java.util.List;

public class UsuarioNotificationAdapter extends RecyclerView.Adapter<UsuarioNotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(UsuarioNotificationItem item);
    }

    private final List<UsuarioNotificationItem> items;
    private final OnNotificationClickListener clickListener;

    public UsuarioNotificationAdapter(List<UsuarioNotificationItem> items, OnNotificationClickListener clickListener) {
        this.items = items;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario_notificacion, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        UsuarioNotificationItem item = items.get(position);
        holder.time.setText(item.getTime());
        holder.title.setText(item.getTitle());
        holder.body.setText(item.getBody());
        holder.visitTime.setText(item.getTime());
        holder.visitTitle.setText(item.getTitle());
        holder.visitBody.setText(item.getBody());

        boolean approval = item.getType() == UsuarioNotificationItem.TYPE_APPROVAL;
        holder.approvalCard.setVisibility(approval ? View.VISIBLE : View.GONE);
        holder.visitCard.setVisibility(approval ? View.GONE : View.VISIBLE);
        holder.cta.setVisibility(approval ? View.VISIBLE : View.GONE);
        holder.cityPhoto.setVisibility(approval ? View.GONE : View.VISIBLE);
        holder.timeline.setVisibility(approval ? View.GONE : View.VISIBLE);

        if (approval) {
            holder.cta.setText(item.getCtaLabel());
            holder.cta.setOnClickListener(v -> clickListener.onNotificationClick(item));
            holder.approvalCard.setOnClickListener(v -> clickListener.onNotificationClick(item));
        } else {
            holder.visitCard.setOnClickListener(v -> clickListener.onNotificationClick(item));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final View approvalCard;
        private final View visitCard;
        private final View timeline;
        private final TextView time;
        private final TextView title;
        private final TextView body;
        private final TextView visitTime;
        private final TextView visitTitle;
        private final TextView visitBody;
        private final TextView cta;
        private final FrameLayout cityPhoto;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            approvalCard = itemView.findViewById(R.id.notificationApprovalCard);
            visitCard = itemView.findViewById(R.id.notificationVisitCard);
            timeline = itemView.findViewById(R.id.notificationTimeline);
            time = itemView.findViewById(R.id.tvNotificationItemTime);
            title = itemView.findViewById(R.id.tvNotificationItemTitle);
            body = itemView.findViewById(R.id.tvNotificationItemBody);
            visitTime = itemView.findViewById(R.id.tvNotificationItemTimeVisit);
            visitTitle = itemView.findViewById(R.id.tvNotificationItemTitleVisit);
            visitBody = itemView.findViewById(R.id.tvNotificationItemBodyVisit);
            cta = itemView.findViewById(R.id.btnNotificationItemCta);
            cityPhoto = itemView.findViewById(R.id.notificationItemCityPhoto);
        }
    }
}
