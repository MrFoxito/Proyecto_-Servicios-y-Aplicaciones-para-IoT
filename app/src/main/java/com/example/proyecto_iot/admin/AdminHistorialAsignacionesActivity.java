package com.example.proyecto_iot.admin;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.admin.adapter.AdminAssignmentHistoryAdapter;
import com.example.proyecto_iot.admin.model.AdminAssignmentRecord;
import com.example.proyecto_iot.admin.storage.AdminLocalStorage;
import com.example.proyecto_iot.databinding.ActivityAdminHistorialAsignacionesBinding;

import java.util.List;

public class AdminHistorialAsignacionesActivity extends BaseAdminActivity {

    private ActivityAdminHistorialAsignacionesBinding binding;
    private AdminAssignmentHistoryAdapter adapter;
    private AdminLocalStorage adminLocalStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminHistorialAsignacionesBinding.inflate(getLayoutInflater());
        setContentView(binding);
        adminLocalStorage = new AdminLocalStorage(this);

        setupBackButton();
        setupRecycler();
        renderHistory();
    }

    private void setupRecycler() {
        adapter = new AdminAssignmentHistoryAdapter();
        binding.rvHistorialAsignaciones.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistorialAsignaciones.setAdapter(adapter);
    }

    private void renderHistory() {
        List<AdminAssignmentRecord> records = adminLocalStorage.getAssignmentHistory();
        adapter.setItems(records);

        boolean empty = records.isEmpty();
        binding.rvHistorialAsignaciones.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.tvHistorialVacio.setVisibility(empty ? View.VISIBLE : View.GONE);

        binding.tvResumenHistorial.setText(empty
                ? "0 asignaciones guardadas"
                : records.size() + " asignaciones guardadas localmente");

        if (empty) {
            binding.tvUltimaAsignacion.setText("Asigna un proyecto a un asesor para comenzar el historial.");
            return;
        }

        AdminAssignmentRecord latest = records.get(0);
        binding.tvUltimaAsignacion.setText(
                "Ultima asignacion: " + latest.getAdvisorName()
                        + " -> " + latest.getProjectTitle()
                        + " | " + latest.getAssignedAt()
        );
    }
}
