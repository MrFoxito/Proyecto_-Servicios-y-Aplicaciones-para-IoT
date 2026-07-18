package com.example.proyecto_iot.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        setupSwipeDismiss();
    }

    private void setupSwipeDismiss() {
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
                AdminAssignmentRecord item = adapter.getItemAt(viewHolder.getBindingAdapterPosition());
                if (item != null) {
                    adminLocalStorage.removeAssignmentRecord(item.getId());
                    Toast.makeText(AdminHistorialAsignacionesActivity.this,
                            "Asignacion retirada del historial", Toast.LENGTH_SHORT).show();
                }
                renderHistory();
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvHistorialAsignaciones);
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
