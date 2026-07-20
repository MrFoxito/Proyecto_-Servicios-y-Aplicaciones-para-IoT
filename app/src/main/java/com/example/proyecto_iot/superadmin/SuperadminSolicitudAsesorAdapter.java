package com.example.proyecto_iot.superadmin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.superadmin.notifications.SuperadminNotificationHelper;
import com.example.proyecto_iot.data.ProfileAvatarLoader;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuperadminSolicitudAsesorAdapter extends RecyclerView.Adapter<SuperadminSolicitudAsesorAdapter.SolicitudViewHolder> {
    private final List<SuperadminSolicitudAsesorItem> items;

    public SuperadminSolicitudAsesorAdapter(List<SuperadminSolicitudAsesorItem> items) {
        this.items = items;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public SolicitudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sa_asesor_solicitud, parent, false);
        return new SolicitudViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SolicitudViewHolder holder, int position) {
        SuperadminSolicitudAsesorItem item = items.get(position);
        ProfileAvatarLoader.load(holder.avatar, item.getAvatarUrl(), item.getName(),
                R.drawable.sa_avatar_placeholder);
        holder.name.setText(item.getName());
        holder.email.setText(item.getEmail());
        holder.agency.setText(item.getAgency());
        holder.status.setText(item.getStatus());
        setDecisionEnabled(holder, true);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        if (params != null) {
            params.topMargin = position == 0 ? 0
                    : holder.itemView.getResources().getDimensionPixelSize(R.dimen.space_12);
            holder.itemView.setLayoutParams(params);
        }
        holder.btnApprove.setOnClickListener(v -> decide(holder, item, true));
        holder.btnReject.setOnClickListener(v -> decide(holder, item, false));
    }

    private void decide(SolicitudViewHolder holder, SuperadminSolicitudAsesorItem item, boolean approve) {
        setDecisionEnabled(holder, false);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.runTransaction(transaction -> {
                    DocumentSnapshot advisor = transaction.get(firestore.collection("usuarios").document(item.getUid()));
                    if (!advisor.exists()
                            || !"asesor".equalsIgnoreCase(advisor.getString("rol"))
                            || !"pendiente".equalsIgnoreCase(advisor.getString("estado"))) {
                        throw new FirebaseFirestoreException("La solicitud ya fue procesada.",
                                FirebaseFirestoreException.Code.ABORTED);
                    }

                    Map<String, Object> changes = new HashMap<>();
                    changes.put("estado", approve ? "activo" : "rechazado");
                    changes.put("updatedAt", System.currentTimeMillis());
                    if (approve) {
                        String companyId = value(advisor.getString("empresaSolicitadaId"));
                        String companyName = value(advisor.getString("empresaSolicitadaNombre"));
                        if (companyId.isEmpty()) {
                            throw new FirebaseFirestoreException("La solicitud no tiene una inmobiliaria válida.",
                                    FirebaseFirestoreException.Code.FAILED_PRECONDITION);
                        }
                        DocumentSnapshot company = transaction.get(firestore.collection("empresas").document(companyId));
                        if (!company.exists() || !"activo".equalsIgnoreCase(company.getString("estado"))) {
                            throw new FirebaseFirestoreException("La inmobiliaria solicitada ya no está activa.",
                                    FirebaseFirestoreException.Code.FAILED_PRECONDITION);
                        }
                        String authoritativeCompanyName = value(company.getString("nombre"));
                        if (!authoritativeCompanyName.isEmpty()) companyName = authoritativeCompanyName;
                        changes.put("empresaId", companyId);
                        changes.put("inmobiliariaId", companyId);
                        changes.put("empresaNombre", companyName);
                        changes.put("approvedAt", System.currentTimeMillis());
                    } else {
                        changes.put("rejectedAt", System.currentTimeMillis());
                    }
                    transaction.update(advisor.getReference(), changes);
                    return null;
                })
                .addOnSuccessListener(unused -> {
                    if (approve) {
                        com.example.proyecto_iot.data.SystemLogger.logEvent(
                                "sistema", "info", "Asesor Aprobado",
                                "Usuario: " + item.getName(), "Solicitud Aceptada",
                                "Se otorgó el rol de asesor a " + item.getEmail()
                        );
                        SuperadminNotificationHelper.showAdvisorRequestApprovedNotification(
                                holder.itemView.getContext(), item.getName());
                    } else {
                        com.example.proyecto_iot.data.SystemLogger.logEvent(
                                "sistema", "alerta", "Asesor Rechazado",
                                "Usuario: " + item.getName(), "Solicitud Rechazada",
                                "Se denegó la solicitud de asesor de " + item.getEmail()
                        );
                        SuperadminNotificationHelper.showAdvisorRequestRejectedNotification(
                                holder.itemView.getContext(), item.getName());
                    }
                    Toast.makeText(holder.itemView.getContext(), approve ? "Solicitud aprobada" : "Solicitud rechazada",
                            Toast.LENGTH_SHORT).show();
                    removeItem(holder.getBindingAdapterPosition());
                })
                .addOnFailureListener(error -> {
                    setDecisionEnabled(holder, true);
                    Toast.makeText(holder.itemView.getContext(), error.getMessage() == null
                                    ? "No se pudo procesar la solicitud."
                                    : error.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setDecisionEnabled(SolicitudViewHolder holder, boolean enabled) {
        holder.btnApprove.setEnabled(enabled);
        holder.btnReject.setEnabled(enabled);
        holder.btnApprove.setAlpha(enabled ? 1f : 0.55f);
        holder.btnReject.setAlpha(enabled ? 1f : 0.55f);
    }

    private void removeItem(int adapterPosition) {
        if (adapterPosition == RecyclerView.NO_POSITION) return;
        items.remove(adapterPosition);
        notifyItemRemoved(adapterPosition);
        notifyItemRangeChanged(adapterPosition, items.size() - adapterPosition);
    }

    private String value(String text) {
        return text == null ? "" : text.trim();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getUid().hashCode();
    }

    static class SolicitudViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView name;
        private final TextView email;
        private final TextView agency;
        private final TextView status;
        private final Button btnApprove;
        private final Button btnReject;

        SolicitudViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.ivAdvisorAvatar);
            name = itemView.findViewById(R.id.tvAdvisorName);
            email = itemView.findViewById(R.id.tvAdvisorEmail);
            agency = itemView.findViewById(R.id.tvAdvisorAgency);
            status = itemView.findViewById(R.id.tvAdvisorStatus);
            btnApprove = itemView.findViewById(R.id.btnApproveAdvisor);
            btnReject = itemView.findViewById(R.id.btnRejectAdvisor);
        }
    }
}
