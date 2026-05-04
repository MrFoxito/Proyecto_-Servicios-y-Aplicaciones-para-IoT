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
import com.example.proyecto_iot.databinding.ActivityAdminNotificacionesBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminNotificacionesActivity extends BaseAdminActivity {

    private ActivityAdminNotificacionesBinding binding;
    private AdminNotificationsAdapter adapter;
    private final Set<String> dismissedIds = new HashSet<>();
    private String activeFilter = "todos";
    private final List<AdminNotificationItem> baseNotifications = Arrays.asList(
            new AdminNotificationItem(
                    "payment_1",
                    AdminNotificationItem.Section.TODAY,
                    AdminNotificationItem.Type.PAYMENT,
                    "Pago Recibido",
                    "Hace menos de 10 min",
                    "Unidad 402 - Torre B",
                    "$4,500.00 USD",
                    "REVISAR DETALLE"
            ),
            new AdminNotificationItem(
                    "separation_1",
                    AdminNotificationItem.Section.TODAY,
                    AdminNotificationItem.Type.SEPARATION,
                    "Nueva Separacion",
                    "Hace 2 horas",
                    "Cliente: Carlos Mendoza",
                    "Deposito: $1,000.00 USD",
                    "REVISAR DETALLE"
            ),
            new AdminNotificationItem(
                    "action_1",
                    AdminNotificationItem.Section.YESTERDAY,
                    AdminNotificationItem.Type.ACTION,
                    "Accion Requerida",
                    "Ayer, 14:30",
                    "Pago expirado para Separacion #8492",
                    "",
                    "REVISAR DETALLE"
            ),
            new AdminNotificationItem(
                    "payment_2",
                    AdminNotificationItem.Section.TODAY,
                    AdminNotificationItem.Type.PAYMENT,
                    "Pago Confirmado",
                    "Hace 25 min",
                    "Unidad 1103 - Torre A",
                    "$6,200.00 USD",
                    "REVISAR DETALLE"
            ),
            new AdminNotificationItem(
                    "separation_2",
                    AdminNotificationItem.Section.TODAY,
                    AdminNotificationItem.Type.SEPARATION,
                    "Nueva Separacion",
                    "Hace 40 min",
                    "Cliente: Andrea Ponce",
                    "Deposito: $1,500.00 USD",
                    "REVISAR DETALLE"
            ),
            new AdminNotificationItem(
                    "payment_3",
                    AdminNotificationItem.Section.TODAY,
                    AdminNotificationItem.Type.PAYMENT,
                    "Pago Recibido",
                    "Hace 1 hora",
                    "Unidad 804 - Torre C",
                    "$3,900.00 USD",
                    "REVISAR DETALLE"
            ),
            new AdminNotificationItem(
                    "action_2",
                    AdminNotificationItem.Section.TODAY,
                    AdminNotificationItem.Type.ACTION,
                    "Accion Requerida",
                    "Hace 2 horas",
                    "Contrato pendiente de validacion para Separacion #8510",
                    "",
                    "REVISAR DETALLE"
            ),
            new AdminNotificationItem(
                    "separation_3",
                    AdminNotificationItem.Section.YESTERDAY,
                    AdminNotificationItem.Type.SEPARATION,
                    "Nueva Separacion",
                    "Ayer, 18:20",
                    "Cliente: Mariana Torres",
                    "Deposito: $1,000.00 USD",
                    "REVISAR DETALLE"
            ),
            new AdminNotificationItem(
                    "payment_4",
                    AdminNotificationItem.Section.YESTERDAY,
                    AdminNotificationItem.Type.PAYMENT,
                    "Pago Confirmado",
                    "Ayer, 16:45",
                    "Unidad 305 - Torre D",
                    "$2,800.00 USD",
                    "REVISAR DETALLE"
            ),
            new AdminNotificationItem(
                    "action_3",
                    AdminNotificationItem.Section.YESTERDAY,
                    AdminNotificationItem.Type.ACTION,
                    "Accion Requerida",
                    "Ayer, 11:10",
                    "Solicitud de devolucion pendiente para Reserva #1204",
                    "",
                    "REVISAR DETALLE"
            ),
            new AdminNotificationItem(
                    "payment_5",
                    AdminNotificationItem.Section.YESTERDAY,
                    AdminNotificationItem.Type.PAYMENT,
                    "Pago Recibido",
                    "Ayer, 09:15",
                    "Unidad 902 - Torre B",
                    "$5,100.00 USD",
                    "REVISAR DETALLE"
            ),
            new AdminNotificationItem(
                    "separation_4",
                    AdminNotificationItem.Section.YESTERDAY,
                    AdminNotificationItem.Type.SEPARATION,
                    "Nueva Separacion",
                    "Ayer, 08:00",
                    "Cliente: Diego Alvarado",
                    "Deposito: $900.00 USD",
                    "REVISAR DETALLE"
            )
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminNotificacionesBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBackButton();
        setupRecycler();
        setupFilters();

        renderNotifications(activeFilter);
    }

    private void setupRecycler() {
        adapter = new AdminNotificationsAdapter(item -> {
            if (item.getType() == AdminNotificationItem.Type.PAYMENT) {
                openScreen(AdminDetallePagoActivity.class);
            } else {
                openScreen(AdminDetalleSeparacionActivity.class);
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
                    renderNotifications(activeFilter);
                }
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(binding.rvNotificaciones);
    }

    private void setupFilters() {
        binding.filtroTodos.setOnClickListener(v -> {
            activeFilter = "todos";
            selectFilter(binding.filtroTodos, binding.filtroSeparaciones, binding.filtroPagos);
            renderNotifications(activeFilter);
        });
        binding.filtroSeparaciones.setOnClickListener(v -> {
            activeFilter = "separaciones";
            selectFilter(binding.filtroSeparaciones, binding.filtroTodos, binding.filtroPagos);
            renderNotifications(activeFilter);
        });
        binding.filtroPagos.setOnClickListener(v -> {
            activeFilter = "pagos";
            selectFilter(binding.filtroPagos, binding.filtroTodos, binding.filtroSeparaciones);
            renderNotifications(activeFilter);
        });
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
