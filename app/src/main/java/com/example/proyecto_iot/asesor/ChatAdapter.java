package com.example.proyecto_iot.asesor;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.entity.Chat;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Chat> chatList;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    public ChatAdapter(List<Chat> chatList, OnChatClickListener listener) {
        this.chatList = chatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asesor_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.txtUserName.setText(chat.getUserName());
        holder.txtLastMessage.setText(chat.getLastMessage());
        holder.txtTime.setText(chat.getTime());

        if (chat.getProfileImageRes() != 0) {
            holder.imgProfile.setImageResource(chat.getProfileImageRes());
            holder.imgProfile.setVisibility(View.VISIBLE);
            holder.txtInitials.setVisibility(View.GONE);
        } else {
            holder.txtInitials.setText(chat.getInitials());
            holder.txtInitials.setVisibility(View.VISIBLE);
            holder.imgProfile.setVisibility(View.GONE);
        }

        if (chat.isUnread()) {
            holder.unreadDot.setVisibility(View.VISIBLE);
            holder.txtTime.setTextColor(Color.parseColor("#8F7E00")); // Gold for unread
        } else {
            holder.unreadDot.setVisibility(View.GONE);
            holder.txtTime.setTextColor(Color.parseColor("#9AA3AF")); // Grey for read
        }

        holder.itemView.setOnClickListener(v -> listener.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile;
        TextView txtInitials, txtUserName, txtLastMessage, txtTime;
        View unreadDot;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtInitials = itemView.findViewById(R.id.txtInitials);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtLastMessage = itemView.findViewById(R.id.txtLastMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            unreadDot = itemView.findViewById(R.id.unreadDot);
        }
    }
}
