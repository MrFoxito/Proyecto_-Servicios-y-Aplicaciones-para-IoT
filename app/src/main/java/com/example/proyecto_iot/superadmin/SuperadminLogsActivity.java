package com.example.proyecto_iot.superadmin;

import android.os.Bundle;

import com.example.proyecto_iot.R;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class SuperadminLogsActivity extends BaseSuperadminActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_logs);
        setupCommonNavigation();

        RecyclerView recyclerView = findViewById(R.id.recyclerLogs);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new SuperadminLogEntryAdapter(buildLogEntries()));
        }
    }

    private List<SuperadminLogEntryItem> buildLogEntries() {
        return Arrays.asList(
                new SuperadminLogEntryItem(
                        ContextCompat.getColor(this, R.color.sa_danger),
                        android.R.drawable.stat_notify_error,
                        ContextCompat.getColor(this, R.color.sa_danger),
                        "Error de Sistema",
                        "Kernel-Level Exception",
                        "14:20",
                        "- ADMIN_042",
                        "CRITICO",
                        ContextCompat.getColor(this, R.color.sa_danger)
                ),
                new SuperadminLogEntryItem(
                        ContextCompat.getColor(this, R.color.sa_gold),
                        android.R.drawable.ic_menu_save,
                        ContextCompat.getColor(this, R.color.sa_gold),
                        "Pago Fallido",
                        "Ref: TXN-9921-BA",
                        "Hace 5 min",
                        "- USER_ID: 8821",
                        "ALERTA",
                        ContextCompat.getColor(this, R.color.sa_gold)
                ),
                new SuperadminLogEntryItem(
                        ContextCompat.getColor(this, R.color.sa_dark),
                        android.R.drawable.checkbox_on_background,
                        ContextCompat.getColor(this, R.color.sa_dark),
                        "Login Exitoso",
                        "Acceso desde IP: 192.168.1.1",
                        "13:45",
                        "- PRINCIPAL ARCHITECT",
                        "EXITO",
                        ContextCompat.getColor(this, R.color.sa_dark)
                ),
                new SuperadminLogEntryItem(
                        ContextCompat.getColor(this, R.color.sa_dark),
                        android.R.drawable.ic_menu_manage,
                        ContextCompat.getColor(this, R.color.sa_dark),
                        "Nueva Agencia",
                        "Inmobiliaria del Este",
                        "12:10",
                        "- ADMIN_SYSTEM",
                        "INFO",
                        ContextCompat.getColor(this, R.color.sa_dark)
                ),
                new SuperadminLogEntryItem(
                        ContextCompat.getColor(this, R.color.sa_dark),
                        android.R.drawable.ic_menu_add,
                        ContextCompat.getColor(this, R.color.sa_dark),
                        "Registro de Usuario",
                        "Validacion de Correo Completada",
                        "11:55",
                        "- USER_ID: 8824",
                        "EXITO",
                        ContextCompat.getColor(this, R.color.sa_dark)
                )
        );
    }
}


