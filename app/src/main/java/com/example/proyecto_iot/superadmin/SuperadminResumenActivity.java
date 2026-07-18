package com.example.proyecto_iot.superadmin;

import android.os.Bundle;
import android.widget.TextView;
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
        
        // Fetch User Count
        firestore.collection("usuarios").get().addOnSuccessListener(snapshot -> {
            TextView tvUsuariosCount = findViewById(R.id.tvUsuariosCount);
            if (tvUsuariosCount != null) {
                int count = snapshot.size();
                tvUsuariosCount.setText(count >= 1000 ? String.format(Locale.US, "%.1fk", count/1000.0f) : String.valueOf(count));
            }
            TextView tvUsuariosGrowth = findViewById(R.id.tvUsuariosGrowth);
            if (tvUsuariosGrowth != null) tvUsuariosGrowth.setText("^ 12%"); // Example growth
        });

        // Fetch Agency Count
        firestore.collection("empresas").get().addOnSuccessListener(snapshot -> {
            TextView tvAgenciasCount = findViewById(R.id.tvAgenciasCount);
            if (tvAgenciasCount != null) {
                tvAgenciasCount.setText(String.valueOf(snapshot.size()));
            }
        });

        // Fetch Total Reservations
        firestore.collection("separaciones").get().addOnSuccessListener(snapshot -> {
            double total = 0;
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Double amount = null;
                for (String field : new String[]{"amount", "monto", "montoTexto"}) {
                    if (amount != null) break;
                    Object val = doc.get(field);
                    if (val instanceof Number) {
                        amount = ((Number) val).doubleValue();
                    } else if (val instanceof String) {
                        try {
                            amount = Double.parseDouble(((String) val).replaceAll("[^0-9.]", ""));
                        } catch (Exception ignored) {}
                    }
                }
                if (amount != null) total += amount;
            }
            TextView tvReservasTotal = findViewById(R.id.tvReservasTotal);
            if (tvReservasTotal != null) {
                if (total >= 1000000) {
                    tvReservasTotal.setText(String.format(Locale.US, "S/ %.1fM", total / 1000000.0));
                } else if (total >= 1000) {
                    tvReservasTotal.setText(String.format(Locale.US, "S/ %.1fK", total / 1000.0));
                } else {
                    tvReservasTotal.setText(String.format(Locale.US, "S/ %.0f", total));
                }
            }
        });
        
        firestore.collection("usuarios")
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
                        if (items.size() >= 3) break;
                    }
                    RecyclerView recyclerControl = findViewById(R.id.recyclerControlAcceso);
                    if (recyclerControl != null) {
                        recyclerControl.setAdapter(new SuperadminControlAccesoAdapter(items));
                    }
                });

        loadResumenLogs();
    }

    private void loadResumenLogs() {
        com.google.firebase.firestore.FirebaseFirestore firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        firestore.collection("logs_sistema")
                .limit(3)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<SuperadminResumenLogItem> items = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        String nivel = doc.getString("nivel");
                        if (nivel == null) nivel = "info";
                        int color = android.graphics.Color.parseColor("#0F172A");
                        if ("critico".equalsIgnoreCase(nivel)) color = android.graphics.Color.parseColor("#DC2626");
                        else if ("alerta".equalsIgnoreCase(nivel)) color = android.graphics.Color.parseColor("#EAB308");

                        String tipo = doc.getString("tipo");
                        if (tipo == null) tipo = "sistema";

                        String fecha = doc.getString("fecha");
                        if (fecha == null) fecha = doc.getString("dateIso");
                        if (fecha == null) fecha = "";

                        items.add(new SuperadminResumenLogItem(
                                tipo.toUpperCase(Locale.ROOT),
                                doc.getString("resumen") != null ? doc.getString("resumen") : (doc.getString("titulo") != null ? doc.getString("titulo") : ""),
                                color,
                                color,
                                fecha
                        ));
                    }
                    RecyclerView recyclerLogs = findViewById(R.id.recyclerResumenLogs);
                    if (recyclerLogs != null) {
                        recyclerLogs.setAdapter(new SuperadminResumenLogAdapter(items));
                    }
                });
    }
}
