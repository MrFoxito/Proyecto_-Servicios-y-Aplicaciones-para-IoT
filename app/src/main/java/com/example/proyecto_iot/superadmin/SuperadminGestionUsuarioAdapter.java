package com.example.proyecto_iot.superadmin;

import com.example.proyecto_iot.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.superadmin.notifications.SuperadminNotificationHelper;
import com.example.proyecto_iot.data.ProfileAvatarLoader;

import java.util.List;

public class SuperadminGestionUsuarioAdapter extends RecyclerView.Adapter<SuperadminGestionUsuarioAdapter.GestionUsuarioViewHolder> {
    private final List<SuperadminGestionUsuarioItem> items;
    private OnUserToggledListener toggledListener;

    public interface OnUserToggledListener {
        void onUserToggled();
    }

    public SuperadminGestionUsuarioAdapter(List<SuperadminGestionUsuarioItem> items) {
        this.items = items;
        setHasStableIds(true);
    }

    public void setOnUserToggledListener(OnUserToggledListener listener) {
        this.toggledListener = listener;
    }

    @NonNull
    @Override
    public GestionUsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sa_gestion_usuario, parent, false);
        return new GestionUsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GestionUsuarioViewHolder holder, int position) {
        SuperadminGestionUsuarioItem item = items.get(position);
        ProfileAvatarLoader.load(holder.avatar, item.getAvatarUrl(), item.getName(),
                R.drawable.sa_avatar_placeholder);
        holder.name.setText(item.getName());
        holder.email.setText(item.getEmail());
        holder.agency.setText(item.getAgency());

        updateToggleVisuals(holder, item.isActive());

        holder.toggle.setOnClickListener(v -> {
            boolean nowActive = !item.isActive();
            String newStatus = nowActive ? "activo" : "inactivo";
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("usuarios").document(item.getUid())
                    .update("estado", newStatus)
                    .addOnSuccessListener(aVoid -> {
                        String action = nowActive ? "Activado" : "Desactivado";
                        String nivel = nowActive ? "info" : "alerta";
                        com.example.proyecto_iot.data.SystemLogger.logEvent(
                                "sistema", nivel, "Usuario " + action,
                                "Usuario: " + item.getName(), "Estado Actualizado", "El usuario " + item.getEmail() + " ha sido " + action.toLowerCase()
                        );
                        item.setActive(nowActive);
                        updateToggleVisuals(holder, nowActive);
                        SuperadminNotificationHelper.showUserStatusChangedNotification(v.getContext(), item.getName(), nowActive);
                        if (toggledListener != null) {
                            toggledListener.onUserToggled();
                        }
                    });
        });

        holder.divider.setVisibility(position == items.size() - 1 ? View.GONE : View.VISIBLE);
    }

    private void updateToggleVisuals(GestionUsuarioViewHolder holder, boolean active) {
        if (active) {
            holder.status.setText("ACTIVO");
            holder.status.setBackgroundResource(R.drawable.sa_pill_active);
            holder.status.setTextColor(ContextCompat.getColor(holder.status.getContext(), R.color.sa_text_primary));
            holder.toggle.setBackgroundResource(R.drawable.sa_toggle_on);
        } else {
            holder.status.setText("INACTIVO");
            holder.status.setBackgroundResource(R.drawable.sa_pill_inactive);
            holder.status.setTextColor(ContextCompat.getColor(holder.status.getContext(), R.color.sa_text_secondary));
            holder.toggle.setBackgroundResource(R.drawable.sa_toggle_off);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getUid().hashCode();
    }

    static class GestionUsuarioViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView name;
        private final TextView email;
        private final TextView agency;
        private final TextView status;
        private final View toggle;
        private final View divider;

        GestionUsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.ivUserAvatar);
            name = itemView.findViewById(R.id.tvUserName);
            email = itemView.findViewById(R.id.tvUserEmail);
            agency = itemView.findViewById(R.id.tvUserAgency);
            status = itemView.findViewById(R.id.tvUserStatus);
            toggle = itemView.findViewById(R.id.viewUserToggle);
            divider = itemView.findViewById(R.id.viewUserDivider);
        }
    }
}
