package com.example.proyecto_iot.admin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.adapter.AdminNotificationsAdapter;
import com.example.proyecto_iot.admin.model.AdminNotificationItem;
import com.example.proyecto_iot.admin.storage.AdminLocalStorage;
import com.example.proyecto_iot.data.FirebaseAdminNotificationRepository;
import com.example.proyecto_iot.databinding.ActivityAdminNotificacionesBinding;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminNotificacionesActivity extends BaseAdminActivity {

    private static final String FILTER_SCREEN_KEY = "admin_notifications";

    private ActivityAdminNotificacionesBinding binding;
    private AdminNotificationsAdapter adapter;
    private AdminLocalStorage adminLocalStorage;
    private final Set<String> dismissedIds = new HashSet<>();
    private String activeFilter = "todos";
    private List<AdminNotificationItem> baseNotifications = new ArrayList<>();
    private final FirebaseAdminNotificationRepository notificationRepository = new FirebaseAdminNotificationRepository();
    private ListenerRegistration notificationsRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminNotificacionesBinding.inflate(getLayoutInflater());
        setContentView(binding);
        adminLocalStorage = new AdminLocalStorage(this);
        dismissedIds.addAll(adminLocalStorage.getDismissedNotificationIds());
        activeFilter = adminLocalStorage.getLastFilter(FILTER_SCREEN_KEY, "todos");

        setupBackButton();
        setupRecycler();
        setupFilters();

        restoreLastFilter();
        listenNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            renderNotifications(activeFilter);
        }
    }

    private void setupRecycler() {
        adapter = new AdminNotificationsAdapter(item -> {
            if (item.getType() == AdminNotificationItem.Type.PAYMENT) {
                Intent intent = new Intent(this, AdminDetallePagoActivity.class);
                intent.putExtra("separation_id", item.getSeparationId());
                intent.putExtra("notification_id", item.getId());
                startActivity(intent);
            } else if (item.getType() == AdminNotificationItem.Type.SEPARATION) {
                Intent intent = new Intent(this, AdminDetalleSeparacionActivity.class);
                intent.putExtra("separation_id", item.getSeparationId());
                intent.putExtra("notification_id", item.getId());
                startActivity(intent);
            }
        });
        binding.rvNotificaciones.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotificaciones.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return adapter.isNotificationPosition(viewHolder.getBindingAdapterPosition())
                        ? super.getSwipeDirs(recyclerView, viewHolder)
                        : 0;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                AdminNotificationItem item = adapter.getNotificationAt(viewHolder.getBindingAdapterPosition());
                if (item != null) {
                    dismissedIds.add(item.getId());
                    adminLocalStorage.saveDismissedNotificationIds(dismissedIds);
                    renderNotifications(activeFilter);
                    notificationRepository.deleteNotification(item.getId(), new FirebaseAdminNotificationRepository.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            dismissedIds.remove(item.getId());
                            adminLocalStorage.saveDismissedNotificationIds(dismissedIds);
                            Toast.makeText(AdminNotificacionesActivity.this, "Notificacion eliminada", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String message) {
                            dismissedIds.remove(item.getId());
                            adminLocalStorage.saveDismissedNotificationIds(dismissedIds);
                            renderNotifications(activeFilter);
                            Toast.makeText(AdminNotificacionesActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(binding.rvNotificaciones);
    }

    private void setupFilters() {
        binding.filtroTodos.setOnClickListener(v -> {
            activeFilter = "todos";
            adminLocalStorage.saveLastFilter(FILTER_SCREEN_KEY, activeFilter);
            selectFilter(binding.filtroTodos, binding.filtroSeparaciones, binding.filtroPagos);
            renderNotifications(activeFilter);
        });
        binding.filtroSeparaciones.setOnClickListener(v -> {
            activeFilter = "separaciones";
            adminLocalStorage.saveLastFilter(FILTER_SCREEN_KEY, activeFilter);
            selectFilter(binding.filtroSeparaciones, binding.filtroTodos, binding.filtroPagos);
            renderNotifications(activeFilter);
        });
        binding.filtroPagos.setOnClickListener(v -> {
            activeFilter = "pagos";
            adminLocalStorage.saveLastFilter(FILTER_SCREEN_KEY, activeFilter);
            selectFilter(binding.filtroPagos, binding.filtroTodos, binding.filtroSeparaciones);
            renderNotifications(activeFilter);
        });
    }

    private void restoreLastFilter() {
        if ("separaciones".equals(activeFilter)) {
            selectFilter(binding.filtroSeparaciones, binding.filtroTodos, binding.filtroPagos);
        } else if ("pagos".equals(activeFilter)) {
            selectFilter(binding.filtroPagos, binding.filtroTodos, binding.filtroSeparaciones);
        } else {
            activeFilter = "todos";
            selectFilter(binding.filtroTodos, binding.filtroSeparaciones, binding.filtroPagos);
        }
        adminLocalStorage.saveLastFilter(FILTER_SCREEN_KEY, activeFilter);
        renderNotifications(activeFilter);
    }

    private void renderNotifications(String filter) {
        List<AdminNotificationItem> filtered = new ArrayList<>();
        for (AdminNotificationItem item : baseNotifications) {
            if (dismissedIds.contains(item.getId())) {
                continue;
            }
            if (item.getType() != AdminNotificationItem.Type.PAYMENT
                    && item.getType() != AdminNotificationItem.Type.SEPARATION) {
                continue;
            }
            boolean include = "todos".equals(filter)
                    || ("pagos".equals(filter) && item.getType() == AdminNotificationItem.Type.PAYMENT)
                    || ("separaciones".equals(filter) && item.getType() == AdminNotificationItem.Type.SEPARATION);
            if (include) {
                filtered.add(item);
            }
        }

        List<AdminNotificationsAdapter.RowItem> rows = new ArrayList<>();
        addSectionRows(rows, filtered, AdminNotificationItem.Section.TODAY, "HOY");
        addSectionRows(rows, filtered, AdminNotificationItem.Section.YESTERDAY, "AYER");

        adapter.setRows(rows);
        boolean empty = rows.isEmpty();
        binding.tvSinNotificaciones.setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
        binding.rvNotificaciones.setVisibility(empty ? android.view.View.GONE : android.view.View.VISIBLE);
    }

    private void listenNotifications() {
        if (notificationsRegistration != null) {
            notificationsRegistration.remove();
        }
        notificationsRegistration = notificationRepository.listenAllowedAdminNotifications(new FirebaseAdminNotificationRepository.NotificationsCallback() {
            @Override
            public void onSuccess(List<AdminNotificationItem> notifications) {
                baseNotifications = notifications;
                renderNotifications(activeFilter);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AdminNotificacionesActivity.this, message, Toast.LENGTH_LONG).show();
                baseNotifications = new ArrayList<>();
                renderNotifications(activeFilter);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (notificationsRegistration != null) {
            notificationsRegistration.remove();
        }
        super.onDestroy();
    }

    private void addSectionRows(List<AdminNotificationsAdapter.RowItem> rows, List<AdminNotificationItem> filtered, AdminNotificationItem.Section section, String label) {
        boolean addedHeader = false;
        for (AdminNotificationItem item : filtered) {
            if (item.getSection() != section) {
                continue;
            }
            if (!addedHeader) {
                rows.add(new AdminNotificationsAdapter.SectionRow(label));
                addedHeader = true;
            }
            rows.add(new AdminNotificationsAdapter.NotificationRow(item));
        }
    }

    private void selectFilter(TextView selected, TextView... others) {
        selected.setBackgroundResource(R.drawable.bg_pill_active);
        selected.setTextColor(Color.WHITE);

        for (TextView other : others) {
            other.setBackgroundResource(R.drawable.bg_pill_inactive);
            other.setTextColor(Color.parseColor("#8C7A65"));
        }
    }
}
