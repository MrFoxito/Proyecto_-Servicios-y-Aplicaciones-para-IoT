package com.example.proyecto_iot.superadmin;

import android.os.Bundle;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SuperadminAprobacionAsesoresActivity extends BaseSuperadminActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_aprobacion_asesores);
        setupCommonNavigation();

        RecyclerView recyclerView = findViewById(R.id.recyclerSolicitudesAsesores);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new SuperadminSolicitudAsesorAdapter(loadPendingRequests()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        RecyclerView recyclerAsesores = findViewById(R.id.recyclerSolicitudesAsesores);
        if (recyclerAsesores != null) {
            recyclerAsesores.setAdapter(new SuperadminSolicitudAsesorAdapter(loadPendingRequests()));
        }
    }

    private List<SuperadminSolicitudAsesorItem> loadPendingRequests() {
        List<SuperadminSolicitudAsesorItem> pending = new ArrayList<>();
        for (SuperadminSolicitudAsesorItem item : new LocalSchemaStorage(this).getSuperadminAdvisorRequests()) {
            if ("PENDIENTE".equalsIgnoreCase(item.getStatus())) {
                pending.add(item);
            }
        }
        return pending;
    }
}


