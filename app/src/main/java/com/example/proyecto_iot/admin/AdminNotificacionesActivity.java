package com.example.proyecto_iot.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.adapter.AdminNotificationsAdapter;
import com.example.proyecto_iot.admin.model.AdminNotificationItem;
import com.example.proyecto_iot.admin.storage.AdminLocalStorage;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.databinding.ActivityAdminNotificacionesBinding;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminNotificacionesBinding.inflate(getLayoutInflater());
        setContentView(binding);
        adminLocalStorage = new AdminLocalStorage(this);
        baseNotifications = new LocalSchemaStorage(this).getAdminNotifications();
        dismissedIds.addAll(adminLocalStorage.getDismissedNotificationIds());
        activeFilter = adminLocalStorage.getLastFilter(FILTER_SCREEN_KEY, "todos");

        setupBackButton();
        setupRecycler();
        setupFilters();

        restoreLastFilter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            baseNotifications = new LocalSchemaStorage(this).getAdminNotifications();
            renderNotifications(activeFilter);
        }
    }

    private void setupRecycler() {
        adapter = new AdminNotificationsAdapter(item -> {
            if (item.getType() == AdminNotificationItem.Type.PAYMENT) {
                openScreen(AdminDetallePagoActivity.class);
            } else if (item.getType() == AdminNotificationItem.Type.SEPARATION) {
                openScreen(AdminDetalleSeparacionActivity.class);
            } else if (item.getTitle().toLowerCase(java.util.Locale.ROOT).contains("solicitud")) {
                openScreen(AdminSolicitudAsesoresActivity.class);
            } else {
                openScreen(AdminProyectosActivity.class);
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
