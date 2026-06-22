package com.example.proyecto_iot.usuario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;

import java.util.List;

public class UsuarioChatListAdapter extends RecyclerView.Adapter<UsuarioChatListAdapter.ChatViewHolder> {

    public interface OnChatClickListener {
        void onChatClick(UsuarioChatListItem item);
    }

    private final List<UsuarioChatListItem> items;
    private final OnChatClickListener clickListener;

    public UsuarioChatListAdapter(List<UsuarioChatListItem> items, OnChatClickListener clickListener) {
        this.items = items;
        this.clickListener = clickListener;
    }

    public void submitItems(List<UsuarioChatListItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        UsuarioChatListItem item = items.get(position);
        holder.name.setText(item.getName());
        holder.time.setText(item.getTime());
        holder.message.setText(item.getMessage());
        holder.initials.setText(item.getInitials());

        if (item.usesInitials()) {
            holder.avatarImage.setVisibility(View.GONE);
            holder.initialsContainer.setVisibility(View.VISIBLE);
        } else {
            holder.initialsContainer.setVisibility(View.GONE);
            holder.avatarImage.setVisibility(View.VISIBLE);
            holder.avatarImage.setImageResource(item.getAvatarResId());
        }

        if (item.isUnread()) {
            holder.unreadBadge.setVisibility(View.VISIBLE);
            holder.unreadBadge.setText("1");
        } else {
            holder.unreadBadge.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> clickListener.onChatClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatarImage;
        private final View initialsContainer;
        private final TextView initials;
        private final TextView name;
        private final TextView time;
        private final TextView message;
        private final TextView unreadBadge;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.ivChatAvatar);
            initialsContainer = itemView.findViewById(R.id.chatInitialsContainer);
            initials = itemView.findViewById(R.id.tvChatInitials);
            name = itemView.findViewById(R.id.tvChatName);
            time = itemView.findViewById(R.id.tvChatTime);
            message = itemView.findViewById(R.id.tvChatMessage);
            unreadBadge = itemView.findViewById(R.id.tvChatUnreadBadge);
        }
    }
}
