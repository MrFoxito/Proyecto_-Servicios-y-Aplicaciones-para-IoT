package com.example.proyecto_iot.superadmin;

import android.os.Bundle;
import android.widget.Toast;
import com.example.proyecto_iot.data.DataMigrationRepository;

import com.example.proyecto_iot.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

public class SuperadminResumenActivity extends BaseSuperadminActivity {

    @Override
    protected void onResume() {
        super.onResume();
        loadRealData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_resumen);
        setupCommonNavigation();
        new DataMigrationRepository().run(new DataMigrationRepository.MigrationCallback() {
            @Override
            public void onSuccess(DataMigrationRepository.MigrationResult result) {
                if (result.usersUpdated + result.projectsUpdated + result.assignmentsCreated
                        + result.assignmentsUpdated + result.mediaMigrated > 0) {
                    Toast.makeText(SuperadminResumenActivity.this,
                            "Datos estabilizados. Imágenes pendientes: " + result.mediaPending,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SuperadminResumenActivity.this,
                        "Migración pendiente: " + message, Toast.LENGTH_LONG).show();
            }
        });

        RecyclerView controlAcceso = findViewById(R.id.recyclerControlAcceso);
        if (controlAcceso != null) {
            controlAcceso.setLayoutManager(new LinearLayoutManager(this));
        }

        RecyclerView resumenLogs = findViewById(R.id.recyclerResumenLogs);
        if (resumenLogs != null) {
            resumenLogs.setLayoutManager(new LinearLayoutManager(this));
        }
        
        loadRealData();
    }

    private void loadRealData() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        
        firestore.collection("usuarios")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<SuperadminControlAccesoItem> items = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String name = doc.getString("nombre");
                        if (name == null || name.trim().isEmpty()) {
                            name = (doc.getString("nombres") + " " + doc.getString("apellidos")).trim();
                        }
                        if (name.isEmpty()) name = "Usuario";
                        
                        String role = doc.getString("rol");
                        if (role == null) role = "cliente";
                        
                        int avatarResId = R.drawable.sa_profile_admin;
                        if ("asesor".equalsIgnoreCase(role)) avatarResId = R.drawable.sa_profile_asesor_1;
                        if ("cliente".equalsIgnoreCase(role)) avatarResId = R.drawable.sa_profile_asesor_2;
                        
                        items.add(new SuperadminControlAccesoItem(name, role.toUpperCase(Locale.ROOT), avatarResId));
                    }
                    RecyclerView recyclerControl = findViewById(R.id.recyclerControlAcceso);
                    if (recyclerControl != null) {
                        recyclerControl.setAdapter(new SuperadminControlAccesoAdapter(items));
                    }
                });

        firestore.collection("separaciones")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<SuperadminResumenLogItem> items = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String label = "SEPARACIÓN";
                        String amount = doc.getString("montoTexto");
                        if (amount == null) amount = doc.getDouble("amount") + " " + doc.getString("currency");
                        String message = "Nueva separación por " + amount;
                        int accentColor = 0xFF4CAF50;
                        int labelColor = 0xFF388E3C;
                        Long createdAt = doc.getLong("createdAt");
                        String dateIso = "Reciente";
                        if (createdAt != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                            dateIso = sdf.format(new Date(createdAt));
                        }
                        items.add(new SuperadminResumenLogItem(label, message, accentColor, labelColor, dateIso));
                    }
                    RecyclerView recyclerLogs = findViewById(R.id.recyclerResumenLogs);
                    if (recyclerLogs != null) {
                        recyclerLogs.setAdapter(new SuperadminResumenLogAdapter(items));
                    }
                });
    }
}
