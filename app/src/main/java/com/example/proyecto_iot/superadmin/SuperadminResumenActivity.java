package com.example.proyecto_iot.superadmin;

import android.os.Bundle;

import com.example.proyecto_iot.R;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class SuperadminResumenActivity extends BaseSuperadminActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_resumen);
        setupCommonNavigation();

        RecyclerView controlAcceso = findViewById(R.id.recyclerControlAcceso);
        if (controlAcceso != null) {
            controlAcceso.setLayoutManager(new LinearLayoutManager(this));
            controlAcceso.setAdapter(new SuperadminControlAccesoAdapter(buildControlAcceso()));
        }

        RecyclerView resumenLogs = findViewById(R.id.recyclerResumenLogs);
        if (resumenLogs != null) {
            resumenLogs.setLayoutManager(new LinearLayoutManager(this));
            resumenLogs.setAdapter(new SuperadminResumenLogAdapter(buildResumenLogs()));
        }
    }

    private List<SuperadminControlAccesoItem> buildControlAcceso() {
        return Arrays.asList(
                new SuperadminControlAccesoItem("Mateo Valdes", "Admin Regional", R.drawable.sa_avatar_07),
                new SuperadminControlAccesoItem("Elena Ramos", "Gestor de Datos", R.drawable.sa_avatar_08),
                new SuperadminControlAccesoItem("Julian Costa", "Auditor Senior", R.drawable.sa_avatar_09)
        );
    }

    private List<SuperadminResumenLogItem> buildResumenLogs() {
        return Arrays.asList(
                new SuperadminResumenLogItem(
                        "ACCESO      Hace 2 min",
                        "M. Valdes inicio sesion desde Madrid, ES",
                        ContextCompat.getColor(this, R.color.sa_gold),
                        ContextCompat.getColor(this, R.color.sa_gold)
                ),
                new SuperadminResumenLogItem(
                        "ACTUALIZACION      Hace 15 min",
                        "Cambio de politica en Agencia Luz Propiedades",
                        ContextCompat.getColor(this, R.color.sa_dark),
                        ContextCompat.getColor(this, R.color.sa_text_primary)
                ),
                new SuperadminResumenLogItem(
                        "ALERTA      Hace 1 h",
                        "Intento de acceso fallido: Admin_04",
                        ContextCompat.getColor(this, R.color.sa_danger),
                        ContextCompat.getColor(this, R.color.sa_danger)
                ),
                new SuperadminResumenLogItem(
                        "NUEVO USUARIO      Hace 3 h",
                        "E. Ramos registro un nuevo auditor regional",
                        ContextCompat.getColor(this, R.color.app_chip_inactive_text),
                        ContextCompat.getColor(this, R.color.app_chip_inactive_text)
                )
        );
    }
}


