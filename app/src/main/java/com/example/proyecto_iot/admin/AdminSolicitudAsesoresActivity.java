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
import com.example.proyecto_iot.admin.adapter.AdminRequestsAdapter;
import com.example.proyecto_iot.admin.model.AdminRequestItem;
import com.example.proyecto_iot.admin.storage.AdminLocalStorage;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.databinding.ActivityAdminSolicitudAsesoresBinding;

import java.util.ArrayList;
import java.util.List;

public class AdminSolicitudAsesoresActivity extends BaseAdminActivity {

    private static final String FILTER_SCREEN_KEY = "admin_advisor_requests";

    private ActivityAdminSolicitudAsesoresBinding binding;
    private AdminRequestsAdapter adapter;
    private AdminLocalStorage adminLocalStorage;
    private List<AdminRequestItem> allRequests = new ArrayList<>();
    private String activeFilter = "todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminSolicitudAsesoresBinding.inflate(getLayoutInflater());
        setContentView(binding);
        adminLocalStorage = new AdminLocalStorage(this);
        allRequests = new LocalSchemaStorage(this).getAdminRequests();

        setupBackButton();
        setupRecycler();
        setupFilters();
        restoreLastFilter();
    }

    private void setupRecycler() {
        adapter = new AdminRequestsAdapter(item -> {
            Intent intent = new Intent(this, AdminVerSolicitudActivity.class);
            intent.putExtra("request_id", item.getId());
            intent.putExtra("request_name", item.getName());
            intent.putExtra("request_email", item.getEmail());
            intent.putExtra("request_project", item.getProjectName());
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        binding.rvSolicitudes.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSolicitudes.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(
                    @NonNull RecyclerView recyclerView,
                    @NonNull RecyclerView.ViewHolder viewHolder,
                    @NonNull RecyclerView.ViewHolder target
            ) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                AdminRequestItem item = adapter.getItemAt(viewHolder.getBindingAdapterPosition());
                if (item == null) {
                    renderRequests(activeFilter);
                    return;
                }
                boolean deleted = new LocalSchemaStorage(AdminSolicitudAsesoresActivity.this)
                        .deleteAdvisorRequest(item.getId());
                if (deleted) {
                    allRequests = new LocalSchemaStorage(AdminSolicitudAsesoresActivity.this).getAdminRequests();
                    renderRequests(activeFilter);
                    Toast.makeText(AdminSolicitudAsesoresActivity.this, "Solicitud eliminada", Toast.LENGTH_SHORT).show();
                } else {
                    renderRequests(activeFilter);
                    Toast.makeText(AdminSolicitudAsesoresActivity.this, "No se pudo eliminar la solicitud", Toast.LENGTH_LONG).show();
                }
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvSolicitudes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            allRequests = new LocalSchemaStorage(this).getAdminRequests();
            renderRequests(activeFilter);
        }
    }

    private void setupFilters() {
        binding.filtroSolicitudesTodas.setOnClickListener(
                v -> applyFilter("todos", binding.filtroSolicitudesTodas, binding.filtroSolicitudesPendientes, binding.filtroSolicitudesAceptadas)
        );
        binding.filtroSolicitudesPendientes.setOnClickListener(
                v -> applyFilter("pendientes", binding.filtroSolicitudesPendientes, binding.filtroSolicitudesTodas, binding.filtroSolicitudesAceptadas)
        );
        binding.filtroSolicitudesAceptadas.setOnClickListener(
                v -> applyFilter("aceptadas", binding.filtroSolicitudesAceptadas, binding.filtroSolicitudesTodas, binding.filtroSolicitudesPendientes)
        );
    }

    private void applyFilter(String filter, TextView selected, TextView... others) {
        activeFilter = filter;
        adminLocalStorage.saveLastFilter(FILTER_SCREEN_KEY, filter);
        selectFilter(selected, others);
        renderRequests(filter);
    }

    private void restoreLastFilter() {
        String filter = adminLocalStorage.getLastFilter(FILTER_SCREEN_KEY, "todos");
        if ("pendientes".equals(filter)) {
            applyFilter("pendientes", binding.filtroSolicitudesPendientes, binding.filtroSolicitudesTodas, binding.filtroSolicitudesAceptadas);
        } else if ("aceptadas".equals(filter)) {
            applyFilter("aceptadas", binding.filtroSolicitudesAceptadas, binding.filtroSolicitudesTodas, binding.filtroSolicitudesPendientes);
        } else {
            applyFilter("todos", binding.filtroSolicitudesTodas, binding.filtroSolicitudesPendientes, binding.filtroSolicitudesAceptadas);
        }
    }

    private void renderRequests(String filter) {
        List<AdminRequestItem> filtered = new ArrayList<>();
        for (AdminRequestItem item : allRequests) {
            boolean matches = "todos".equals(filter)
                    || ("pendientes".equals(filter) && "PENDIENTE".equals(item.getStatus()))
                    || ("aceptadas".equals(filter) && "ACEPTADA".equals(item.getStatus()));
            if (matches) {
                filtered.add(item);
            }
        }
        adapter.setItems(filtered);
    }

    private void selectFilter(TextView selected, TextView... others) {
        selected.setBackgroundResource(R.drawable.bg_pill_active);
        selected.setTextColor(Color.WHITE);

        for (TextView item : others) {
            item.setBackgroundResource(R.drawable.bg_pill_inactive);
            item.setTextColor(Color.parseColor("#8C7A65"));
        }
    }
}
