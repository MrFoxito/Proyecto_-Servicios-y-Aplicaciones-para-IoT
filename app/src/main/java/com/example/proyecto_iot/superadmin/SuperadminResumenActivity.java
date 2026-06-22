package com.example.proyecto_iot.superadmin;

import android.os.Bundle;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SuperadminResumenActivity extends BaseSuperadminActivity {

    @Override
    protected void onResume() {
        super.onResume();
        RecyclerView recyclerControl = findViewById(R.id.recyclerControlAcceso);
        if (recyclerControl != null) {
            recyclerControl.setAdapter(new SuperadminControlAccesoAdapter(
                    new LocalSchemaStorage(this).getSuperadminAccessItems()
            ));
        }

        RecyclerView recyclerLogs = findViewById(R.id.recyclerResumenLogs);
        if (recyclerLogs != null) {
            recyclerLogs.setAdapter(new SuperadminResumenLogAdapter(
                    new LocalSchemaStorage(this).getSuperadminSummaryLogs()
            ));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_resumen);
        setupCommonNavigation();

        RecyclerView controlAcceso = findViewById(R.id.recyclerControlAcceso);
        LocalSchemaStorage storage = new LocalSchemaStorage(this);
        if (controlAcceso != null) {
            controlAcceso.setLayoutManager(new LinearLayoutManager(this));
            controlAcceso.setAdapter(new SuperadminControlAccesoAdapter(storage.getSuperadminAccessItems()));
        }

        RecyclerView resumenLogs = findViewById(R.id.recyclerResumenLogs);
        if (resumenLogs != null) {
            resumenLogs.setLayoutManager(new LinearLayoutManager(this));
            resumenLogs.setAdapter(new SuperadminResumenLogAdapter(storage.getSuperadminSummaryLogs()));
        }
    }
}


