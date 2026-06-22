package com.example.proyecto_iot.asesor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.data.FirebaseSeparationRepository;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AsesorRegistrarSeparacionActivity extends BaseAsesorActivity {

    public static final String EXTRA_CLIENTE = "extra_cliente";
    public static final String EXTRA_CLIENTE_ID = "extra_cliente_id";
    public static final String EXTRA_PROPIEDAD = "extra_propiedad";
    public static final String EXTRA_PROJECT_ID = "extra_proyecto_id";
    public static final String EXTRA_TIPOLOGY_ID = "extra_tipologia_id";
    public static final String EXTRA_PROYECTO = "extra_proyecto";
    public static final String EXTRA_CITA_ID = "extra_cita_id";

    private final FirebaseSeparationRepository separationRepository = new FirebaseSeparationRepository();
    private String activePago = "efectivo";
    private final List<DocumentSnapshot> projectDocuments = new ArrayList<>();
    private final List<DocumentSnapshot> clientDocuments = new ArrayList<>();
    private final List<DocumentSnapshot> typologyDocuments = new ArrayList<>();

    private TextInputLayout layoutSepProyecto, layoutSepTipologia, layoutSepCliente;
    private AutoCompleteTextView txtProyecto, txtTipologia, txtCliente;
    private TextInputEditText txtMonto;
    private MaterialButtonToggleGroup toggleGroupPago;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_registrar_separaciones);

        initViews();
        setupBackButton();
        prefillFromIntent();
        setupPagoChips();
        setupActions();

        Intent intent = getIntent();
        boolean fromCita = intent.getStringExtra(EXTRA_CLIENTE) != null && intent.getStringExtra(EXTRA_PROYECTO) != null;
        if (!fromCita) {
            loadProjectsAndClients();
        }
    }

    private void initViews() {
        layoutSepProyecto = findViewById(R.id.layoutSepProyecto);
        layoutSepTipologia = findViewById(R.id.layoutSepTipologia);
        layoutSepCliente = findViewById(R.id.layoutSepCliente);
        txtProyecto = findViewById(R.id.txtSepProyecto);
        txtTipologia = findViewById(R.id.txtSepTipologia);
        txtCliente = findViewById(R.id.txtSepCliente);
        txtMonto = findViewById(R.id.txtSepMonto);
        toggleGroupPago = findViewById(R.id.toggleGroupPago);
    }

    private void prefillFromIntent() {
        Intent intent = getIntent();
        String cliente = intent.getStringExtra(EXTRA_CLIENTE);
        String proyecto = intent.getStringExtra(EXTRA_PROYECTO);
        String citaId = intent.getStringExtra(EXTRA_CITA_ID);

        boolean fromCita = cliente != null && proyecto != null;

        View banner = findViewById(R.id.bannerCitaVinculada);
        TextView txtBannerDetalle = findViewById(R.id.txtBannerCitaDetalle);
        if (fromCita) {
            banner.setVisibility(View.VISIBLE);
            txtBannerDetalle.setText("Cita" + (citaId != null ? " #" + citaId : "") + " vinculada.");
        } else {
            banner.setVisibility(View.GONE);
        }

        TextView txtCitaId = findViewById(R.id.txtSepCitaId);
        if (citaId != null) {
            txtCitaId.setText(citaId);
        }

        TextView badgeProyecto = findViewById(R.id.badgeSepProyectoVinculado);
        if (fromCita) {
            txtProyecto.setText(proyecto, false);
            layoutSepProyecto.setEnabled(false);
            badgeProyecto.setVisibility(View.VISIBLE);
        } else {
            layoutSepProyecto.setEnabled(true);
            badgeProyecto.setVisibility(View.GONE);
            txtProyecto.setOnItemClickListener((parent, view1, position, id1) -> {
                String selected = txtProyecto.getText().toString();
                for (DocumentSnapshot doc : projectDocuments) {
                    if (selected.equalsIgnoreCase(doc.getString("nombre"))) {
                        loadTypologiesForProject(doc.getId());
                        break;
                    }
                }
            });
        }

        TextView badgeTipologia = findViewById(R.id.badgeSepTipologiaVinculada);
        String tipologia = intent.getStringExtra(EXTRA_TIPOLOGY_ID);
        if (tipologia == null) {
            tipologia = intent.getStringExtra(EXTRA_PROPIEDAD);
        }
        if (fromCita) {
            if (tipologia != null) {
                txtTipologia.setText(tipologia, false);
            }
            layoutSepTipologia.setEnabled(false);
            badgeTipologia.setVisibility(View.VISIBLE);
        } else {
            layoutSepTipologia.setEnabled(true);
            badgeTipologia.setVisibility(View.GONE);
            TextView txtSugerido = findViewById(R.id.txtSepMontoSugerido);
            txtTipologia.setOnItemClickListener((parent, view1, position, id1) -> {
                String selected = txtTipologia.getText().toString();
                for (DocumentSnapshot doc : typologyDocuments) {
                    String title = doc.getString("title");
                    if (title == null) title = doc.getString("nombre");
                    if (selected.equalsIgnoreCase(title)) {
                        String amount = doc.getString("separationAmount");
                        if (amount == null) amount = doc.getString("montoSeparacion");
                        if (amount != null && !amount.isEmpty()) {
                            txtSugerido.setText("Monto sugerido para esta tipología: " + amount);
                        }
                        break;
                    }
                }
            });
        }

        TextView badgeCliente = findViewById(R.id.badgeSepClienteVinculado);
        if (fromCita) {
            txtCliente.setText(cliente, false);
            layoutSepCliente.setEnabled(false);
            badgeCliente.setVisibility(View.VISIBLE);
        } else {
            layoutSepCliente.setEnabled(true);
            badgeCliente.setVisibility(View.GONE);
        }

        TextView txtSugerido = findViewById(R.id.txtSepMontoSugerido);
        String propiedad = intent.getStringExtra(EXTRA_PROPIEDAD);
        if (propiedad != null && propiedad.toLowerCase().contains("penthouse")) {
            txtSugerido.setText("Monto sugerido para este proyecto: $20,000.00");
        }
    }

    private void setupPagoChips() {
        toggleGroupPago.setSingleSelection(true);
        toggleGroupPago.check(R.id.chipPagoEfectivo);
        activePago = "efectivo";

        toggleGroupPago.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.chipPagoEfectivo) {
                    activePago = "efectivo";
                } else if (checkedId == R.id.chipPagoTransferencia) {
                    activePago = "transferencia";
                } else if (checkedId == R.id.chipPagoFinanciamiento) {
                    activePago = "financiamiento";
                }
            }
        });
    }

    private void setupActions() {
        findViewById(R.id.btnConfirmarRegistroSeparacion).setOnClickListener(v -> registrarSeparacion());

        findViewById(R.id.btnCancelarRegistroSeparacion).setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void registrarSeparacion() {
        String asesorId = currentUid();
        if (asesorId.isEmpty()) {
            Toast.makeText(this, "No hay sesion Firebase activa para registrar separacion.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = getIntent();
        boolean fromCita = intent.getStringExtra(EXTRA_CLIENTE) != null && intent.getStringExtra(EXTRA_PROYECTO) != null;

        String selectedClientName = txtCliente.getText().toString().trim();
        String clienteId = "";
        if (fromCita) {
            clienteId = valueOr(intent.getStringExtra(EXTRA_CLIENTE_ID));
        } else {
            for (DocumentSnapshot doc : clientDocuments) {
                String docNombre = getNombreUsuario(doc);
                if (selectedClientName.equalsIgnoreCase(docNombre)) {
                    clienteId = doc.getId();
                    break;
                }
            }
        }

        String selectedProjectName = txtProyecto.getText().toString().trim();
        String propertyId = "";
        if (fromCita) {
            propertyId = valueOr(intent.getStringExtra(EXTRA_PROJECT_ID));
        } else {
            for (DocumentSnapshot doc : projectDocuments) {
                if (selectedProjectName.equalsIgnoreCase(doc.getString("nombre"))) {
                    propertyId = doc.getId();
                    break;
                }
            }
        }

        String selectedTypologyName = txtTipologia.getText().toString().trim();
        String tipologiaId = "";
        if (fromCita) {
            tipologiaId = valueOr(intent.getStringExtra(EXTRA_TIPOLOGY_ID));
            if (tipologiaId.isEmpty()) {
                tipologiaId = valueOr(intent.getStringExtra(EXTRA_PROPIEDAD));
            }
        } else {
            for (DocumentSnapshot doc : typologyDocuments) {
                String title = doc.getString("title");
                if (title == null) title = doc.getString("nombre");
                if (selectedTypologyName.equalsIgnoreCase(title)) {
                    tipologiaId = doc.getId();
                    break;
                }
            }
        }

        if (clienteId.isEmpty()) {
            Toast.makeText(this, "Seleccione un cliente válido de la lista.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (propertyId.isEmpty()) {
            Toast.makeText(this, "Seleccione un proyecto válido de la lista.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseSeparationRepository.SeparationDraft draft = new FirebaseSeparationRepository.SeparationDraft();
        draft.clienteId = clienteId;
        draft.clienteNombre = selectedClientName;
        draft.asesorId = asesorId;
        draft.asesorNombre = AuthSessionManager.getInstance(this).getUserName();
        draft.citaId = valueOr(intent.getStringExtra(EXTRA_CITA_ID));
        draft.propertyId = propertyId;
        draft.tipologiaId = tipologiaId;
        draft.formaPago = activePago;
        draft.inmuebleNombre = selectedProjectName;
        draft.montoTexto = txtMonto.getText() != null ? txtMonto.getText().toString() : "";
        draft.estado = "Pendiente";
        draft.createdByRole = "asesor";

        separationRepository.createSeparation(draft, new FirebaseSeparationRepository.SimpleCallback() {
            @Override
            public void onSuccess(String separationId) {
                Intent targetIntent = new Intent(AsesorRegistrarSeparacionActivity.this, AsesorSolicitudSeparacionActivity.class);
                targetIntent.putExtra("separacionId", separationId);
                startActivity(targetIntent);
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AsesorRegistrarSeparacionActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadProjectsAndClients() {
        String currentUid = currentUid();
        if (currentUid.isEmpty()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. Cargar Proyectos Asignados
        db.collection("usuarios").document(currentUid).get()
                .addOnSuccessListener(doc -> {
                    List<String> proyectosIds = (List<String>) doc.get("proyectos_asignados");
                    Log.e("loadProjectsAndClients", "proyectosIds: " + proyectosIds);
                    if (proyectosIds != null && !proyectosIds.isEmpty()) {
                        db.collection("proyectos")
                                .whereIn(FieldPath.documentId(), proyectosIds)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    projectDocuments.clear();
                                    List<String> projectNames = new ArrayList<>();
                                    for (DocumentSnapshot projectDoc : querySnapshot.getDocuments()) {
                                        String name = projectDoc.getString("nombre");
                                        if (name != null) {
                                            projectDocuments.add(projectDoc);
                                            projectNames.add(name);
                                        }
                                    }
                                    updateDropdown(txtProyecto, projectNames);
                                });
                    }
                });

        // 2. Cargar Clientes con Conversaciones
        db.collection("conversaciones")
                .whereArrayContains("participantUids", currentUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<String> clientIds = new HashSet<>();
                    List<String> clientNames = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String clienteId = doc.getString("clienteUid");
                        if (clienteId != null) clientIds.add(clienteId);
                        String nombre = doc.getString("clienteNombre");
                        if (nombre != null) clientNames.add(nombre);
                    }
                        updateDropdown(txtCliente, clientNames);
                });
    }

    private void loadTypologiesForProject(String projectId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d("loadTypologiesForProject", "projectId: " + projectId);
        db.collection("proyectos_tipologias")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    typologyDocuments.clear();
                    List<String> typologyNames = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String title = doc.getString("title");
                        if (title == null) title = doc.getString("nombre");
                        if (title != null) {
                            typologyDocuments.add(doc);
                            typologyNames.add(title);
                        }
                    }
                    txtTipologia.setText("");
                    updateDropdown(txtTipologia, typologyNames);
                });
    }

    private void updateDropdown(AutoCompleteTextView view, List<String> items) {
        if (!isDestroyed() && !isFinishing() && view.isEnabled()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, items);
            view.setAdapter(adapter);
        }
    }

    private String getNombreUsuario(DocumentSnapshot doc) {
        String nombre=doc.getString("nombre");
        if (nombre != null) return nombre;
        String nombres = doc.getString("nombres");
        String apellidos = doc.getString("apellidos");
        if (nombres != null || apellidos != null) {
            nombre = nombres + " " + apellidos;
        }
        return nombre;
    }

    private String currentUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user == null ? "" : user.getUid();
    }

    private String valueOr(String value) {
        return value == null ? "" : value.trim();
    }
}
