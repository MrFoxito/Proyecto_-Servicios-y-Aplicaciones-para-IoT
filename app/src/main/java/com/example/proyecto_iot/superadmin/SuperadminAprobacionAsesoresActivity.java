package com.example.proyecto_iot.superadmin;

import android.os.Bundle;

import com.example.proyecto_iot.R;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
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
            recyclerView.setAdapter(new SuperadminSolicitudAsesorAdapter(buildSolicitudes()));
        }
    }

    private List<SuperadminSolicitudAsesorItem> buildSolicitudes() {
        return Arrays.asList(
                new SuperadminSolicitudAsesorItem(
                        "Carlos Mendez",
                        "c.mendez@advisor.com",
                        "Agencia: Elite Residences",
                        R.drawable.sa_avatar_04,
                        "PENDIENTE"
                ),
                new SuperadminSolicitudAsesorItem(
                        "Elena Rodriguez",
                        "e.rodriguez@advisor.com",
                        "Agencia: Global Estates",
                        R.drawable.sa_avatar_05,
                        "PENDIENTE"
                ),
                new SuperadminSolicitudAsesorItem(
                        "Julian Castro",
                        "j.castro@advisor.com",
                        "Agencia: Prime Realty",
                        R.drawable.sa_avatar_06,
                        "PENDIENTE"
                )
        );
    }
}


