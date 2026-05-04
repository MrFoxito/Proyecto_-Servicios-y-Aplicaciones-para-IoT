package com.example.proyecto_iot.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.adapter.AdminRequestsAdapter;
import com.example.proyecto_iot.admin.model.AdminRequestItem;
import com.example.proyecto_iot.databinding.ActivityAdminSolicitudAsesoresBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminSolicitudAsesoresActivity extends BaseAdminActivity {

    private ActivityAdminSolicitudAsesoresBinding binding;
    private AdminRequestsAdapter adapter;
    private final List<AdminRequestItem> allRequests = Arrays.asList(
            new AdminRequestItem("Elena Valdes", "Registro enviado hoy", "Solicita unirse a The Editorial Estate como asesora inmobiliaria.", "PENDIENTE", R.drawable.sa_profile_asesor_1),
            new AdminRequestItem("Sofia Mendez", "Aceptada hace 2 dias", "Perfil aprobado para integrarse al equipo comercial.", "ACEPTADA", R.drawable.sa_profile_asesor_2),
            new AdminRequestItem("Julian Costa", "Registro enviado ayer", "Solicita habilitar acceso para gestionar proyectos activos.", "PENDIENTE", R.drawable.sa_profile_asesor_3),
            new AdminRequestItem("Mariana Tello", "Registro enviado hoy", "Solicita integrarse al equipo para cubrir proyectos premium en San Isidro.", "PENDIENTE", R.drawable.sa_profile_asesor_2),
            new AdminRequestItem("Renzo Huaman", "Aceptada hace 1 dia", "Perfil validado para seguimiento de leads y cierres en Surco.", "ACEPTADA", R.drawable.sa_profile_asesor_1),
            new AdminRequestItem("Camila Paredes", "Registro enviado hace 3 horas", "Solicita acceso al panel de proyectos residenciales de Miraflores.", "PENDIENTE", R.drawable.sa_profile_asesor_2),
            new AdminRequestItem("Fabio Quispe", "Aceptada hace 4 dias", "Habilitado para administrar cartera de clientes y visitas agendadas.", "ACEPTADA", R.drawable.sa_profile_asesor_3),
            new AdminRequestItem("Lucia Ferrer", "Registro enviado ayer", "Solicita participar en preventa de proyectos multifamiliares.", "PENDIENTE", R.drawable.sa_profile_asesor_2),
            new AdminRequestItem("Diego Rivas", "Aceptada hace 5 dias", "Perfil aprobado para proyectos de ticket medio en Lima centro.", "ACEPTADA", R.drawable.sa_profile_asesor_1),
            new AdminRequestItem("Valeria Nunez", "Registro enviado hoy", "Solicita incorporarse al equipo para proyectos frente al mar.", "PENDIENTE", R.drawable.sa_profile_asesor_2)
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminSolicitudAsesoresBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBackButton();
        setupRecycler();
        setupFilters();
        renderRequests("todos");
    }

    private void setupRecycler() {
        adapter = new AdminRequestsAdapter(item -> openScreen(AdminVerSolicitudActivity.class));
        binding.rvSolicitudes.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSolicitudes.setAdapter(adapter);
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
        selectFilter(selected, others);
        renderRequests(filter);
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
