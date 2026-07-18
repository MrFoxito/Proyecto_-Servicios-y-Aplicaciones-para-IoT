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

        android.view.View backButton = findViewById(R.id.btnBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerSolicitudesAsesores);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            loadPendingRequests();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPendingRequests();
    }

    private void loadPendingRequests() {
        com.google.firebase.firestore.FirebaseFirestore firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        firestore.collection("usuarios")
                .whereEqualTo("rol", "asesor")
                .whereEqualTo("estado", "pendiente")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<SuperadminSolicitudAsesorItem> pending = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        String nombres = doc.getString("nombres");
                        if (nombres == null) nombres = doc.getString("nombre");
                        String apellidos = doc.getString("apellidos");
                        String name = (nombres != null ? nombres : "") + (apellidos != null ? " " + apellidos : "");
                        if (name.trim().isEmpty()) name = "Asesor Sin Nombre";

                        String email = doc.getString("correo");
                        if (email == null) email = doc.getString("email");
                        if (email == null) email = "Sin correo";

                        String agencyId = doc.getString("inmobiliariaId");
                        if (agencyId == null) agencyId = doc.getString("empresaId");
                        if (agencyId == null) agencyId = "Independiente";
                        String agency = "AGENCIA: " + agencyId;

                        pending.add(new SuperadminSolicitudAsesorItem(
                                doc.getId(),
                                name.trim(),
                                email,
                                agency,
                                R.drawable.sa_profile_admin,
                                "PENDIENTE",
                                ""
                        ));
                    }
                    
                    RecyclerView recyclerAsesores = findViewById(R.id.recyclerSolicitudesAsesores);
                    if (recyclerAsesores != null) {
                        recyclerAsesores.setAdapter(new SuperadminSolicitudAsesorAdapter(pending));
                    }
                });
    }
}


