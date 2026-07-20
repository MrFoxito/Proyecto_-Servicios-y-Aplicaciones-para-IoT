package com.example.proyecto_iot.asesor;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final List<DocumentSnapshot> typologyDocuments = new ArrayList<>();
    private final Map<String, String> assignmentIdsByProjectId = new HashMap<>();
    private final Map<String, ClientOption> clientsByLabel = new HashMap<>();
    private boolean isSubmitting = false;

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
        boolean fromCita = isFromAppointment(intent);
        if (!fromCita) {
            loadProjectsAndClients();
        } else {
            // La cita está vinculada al proyecto, no a una unidad concreta. El asesor
            // debe elegir la tipología real antes de registrar la separación.
            loadTypologiesForProject(valueOr(intent.getStringExtra(EXTRA_PROJECT_ID)),
                    valueOr(intent.getStringExtra(EXTRA_TIPOLOGY_ID)));
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

        boolean fromCita = isFromAppointment(intent);

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
                DocumentSnapshot project = findSelectedProject(txtProyecto.getText().toString());
                if (project != null) loadTypologiesForProject(project.getId());
            });
        }

        TextView badgeTipologia = findViewById(R.id.badgeSepTipologiaVinculada);
        String tipologia = valueOr(intent.getStringExtra(EXTRA_TIPOLOGY_ID));
        if (fromCita) {
            boolean hasLinkedTypology = !tipologia.isEmpty();
            if (hasLinkedTypology) {
                txtTipologia.setText(tipologia, false);
            }
            layoutSepTipologia.setEnabled(!hasLinkedTypology);
            txtTipologia.setEnabled(!hasLinkedTypology);
            badgeTipologia.setVisibility(hasLinkedTypology ? View.VISIBLE : View.GONE);
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
                        double amount = SeparationPricingPolicy.number(firstValue(doc,
                                "separationAmount", "montoSeparacion"));
                        if (SeparationPricingPolicy.hasAmount(amount)) {
                            txtSugerido.setText("Monto sugerido para esta tipología: "
                                    + SeparationPricingPolicy.formatPen(amount));
                        }
                        break;
                    }
                }
            });
        }

        // The same handler is used for manual separations and separations opened
        // from a citation. It simply updates the suggested price for the chosen unit.
        bindTypologySelection();

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
        if (isSubmitting) return;
        String asesorId = currentUid();
        if (asesorId.isEmpty()) {
            Toast.makeText(this, "No hay sesion Firebase activa para registrar separacion.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = getIntent();
        boolean fromCita = isFromAppointment(intent);

        String selectedClientName = txtCliente.getText().toString().trim();
        String clienteId = "";
        if (fromCita) {
            clienteId = valueOr(intent.getStringExtra(EXTRA_CLIENTE_ID));
        } else {
            ClientOption selectedClient = clientsByLabel.get(selectedClientName);
            clienteId = selectedClient == null ? "" : selectedClient.uid;
            if (selectedClient != null) selectedClientName = selectedClient.name;
        }

        String selectedProjectName = txtProyecto.getText().toString().trim();
        String propertyId = "";
        if (fromCita) {
            propertyId = valueOr(intent.getStringExtra(EXTRA_PROJECT_ID));
        } else {
            DocumentSnapshot selectedProject = findSelectedProject(selectedProjectName);
            if (selectedProject != null) {
                propertyId = selectedProject.getId();
                selectedProjectName = projectName(selectedProject);
            }
        }

        String selectedTypologyName = txtTipologia.getText().toString().trim();
        String tipologiaId = "";
        DocumentSnapshot selectedTypology = findSelectedTypology(selectedTypologyName);
        if (fromCita) {
            tipologiaId = selectedTypology == null
                    ? valueOr(intent.getStringExtra(EXTRA_TIPOLOGY_ID))
                    : selectedTypology.getId();
        } else {
            if (selectedTypology != null) tipologiaId = selectedTypology.getId();
        }

        if (clienteId.isEmpty()) {
            Toast.makeText(this, "Seleccione un cliente válido de la lista.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (propertyId.isEmpty()) {
            Toast.makeText(this, "Seleccione un proyecto válido de la lista.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tipologiaId.isEmpty()) {
            Toast.makeText(this, "Selecciona una tipología con precios configurados.", Toast.LENGTH_SHORT).show();
            return;
        }
        String typedAmount = txtMonto.getText() == null ? "" : txtMonto.getText().toString().trim();
        if (!SeparationPricingPolicy.hasAmount(SeparationPricingPolicy.number(typedAmount))) {
            Toast.makeText(this, "Ingresa un monto de separación válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseSeparationRepository.SeparationDraft draft = new FirebaseSeparationRepository.SeparationDraft();
        draft.clienteId = clienteId;
        draft.clienteNombre = selectedClientName;
        draft.asesorId = asesorId;
        draft.asesorNombre = AuthSessionManager.getInstance(this).getUserName();
        draft.citaId = valueOr(intent.getStringExtra(EXTRA_CITA_ID));
        draft.propertyId = propertyId;
        draft.projectId = propertyId;
        draft.proyectoId = propertyId;
        draft.assignmentId = fromCita ? "" : valueOr(assignmentIdsByProjectId.get(propertyId));
        draft.tipologiaId = tipologiaId;
        draft.formaPago = activePago;
        draft.inmuebleNombre = selectedProjectName;
        draft.montoTexto = typedAmount;
        applyPricingSnapshot(draft, selectedTypology);
        draft.estado = "Pendiente";
        draft.createdByRole = "asesor";
        setSubmitting(true);

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
                setSubmitting(false);
                Toast.makeText(AsesorRegistrarSeparacionActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadProjectsAndClients() {
        String advisorUid = currentUid();
        if (advisorUid.isEmpty()) {
            Toast.makeText(this, "No hay sesion Firebase activa.", Toast.LENGTH_LONG).show();
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Assignments are authoritative. The legacy project list on the profile may be stale.
        db.collection("asignaciones").whereEqualTo("asesorId", advisorUid).get()
                .addOnSuccessListener(snapshot -> {
                    if (!isScreenActive()) return;
                    projectDocuments.clear();
                    assignmentIdsByProjectId.clear();
                    List<DocumentSnapshot> activeAssignments = new ArrayList<>();
                    for (DocumentSnapshot assignment : snapshot.getDocuments()) {
                        if (!"ACTIVO".equalsIgnoreCase(valueOr(assignment.getString("estado")))) continue;
                        if (!firstNonEmpty(assignment.getString("projectId"), assignment.getString("propertyId"),
                                assignment.getString("proyectoId")).isEmpty()) activeAssignments.add(assignment);
                    }
                    loadAssignedProjects(db, activeAssignments, 0, new ArrayList<>());
                })
                .addOnFailureListener(error -> {
                    if (!isScreenActive()) return;
                    Toast.makeText(this, "No se pudieron cargar los proyectos asignados. Intenta nuevamente.",
                            Toast.LENGTH_LONG).show();
                });

        // Client choice is keyed by UID from an appointment context, never a name-only match.
        db.collection("citas").whereEqualTo("asesorId", advisorUid).get()
                .addOnSuccessListener(snapshot -> {
                    if (!isScreenActive()) return;
                    clientsByLabel.clear();
                    for (DocumentSnapshot cita : snapshot.getDocuments()) {
                        String clientId = valueOr(cita.getString("clienteId"));
                        if (clientId.isEmpty()) continue;
                        String name = firstNonEmpty(cita.getString("clienteNombre"), "Cliente");
                        clientsByLabel.put(clientLabel(name, clientId), new ClientOption(clientId, name));
                    }
                    updateDropdown(txtCliente, new ArrayList<>(clientsByLabel.keySet()));
                })
                .addOnFailureListener(error -> {
                    if (!isScreenActive()) return;
                    Toast.makeText(this, "No se pudieron cargar los clientes con citas asignadas. Intenta nuevamente.",
                            Toast.LENGTH_LONG).show();
                });
    }

    private void loadAssignedProjects(FirebaseFirestore db, List<DocumentSnapshot> assignments,
                                      int index, List<String> labels) {
        if (!isScreenActive()) return;
        if (index >= assignments.size()) {
            updateDropdown(txtProyecto, labels);
            if (labels.isEmpty()) Toast.makeText(this, "No tienes proyectos activos asignados.", Toast.LENGTH_LONG).show();
            return;
        }
        DocumentSnapshot assignment = assignments.get(index);
        String projectId = firstNonEmpty(assignment.getString("projectId"), assignment.getString("propertyId"),
                assignment.getString("proyectoId"));
        db.collection("proyectos").document(projectId).get().addOnCompleteListener(task -> {
            if (!isScreenActive()) return;
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot project = task.getResult();
                projectDocuments.add(project);
                assignmentIdsByProjectId.put(project.getId(), assignment.getId());
                labels.add(projectLabel(project));
            }
            loadAssignedProjects(db, assignments, index + 1, labels);
        });
    }

    private void loadTypologiesForProject(String projectId) {
        loadTypologiesForProject(projectId, "");
    }

    private void loadTypologiesForProject(String projectId, String linkedTypologyId) {
        if (!isScreenActive()) return;
        if (valueOr(projectId).isEmpty()) {
            typologyDocuments.clear();
            updateDropdown(txtTipologia, new ArrayList<>());
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("proyectos_tipologias")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isScreenActive()) return;
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
                    DocumentSnapshot linkedTypology = null;
                    for (DocumentSnapshot typology : typologyDocuments) {
                        if (typology.getId().equals(valueOr(linkedTypologyId))) {
                            linkedTypology = typology;
                            break;
                        }
                    }
                    if (linkedTypology != null) {
                        txtTipologia.setText(typologyName(linkedTypology), false);
                        showSuggestedAmount(linkedTypology);
                    } else {
                        txtTipologia.setText("");
                    }
                    updateDropdown(txtTipologia, typologyNames);
                })
                .addOnFailureListener(error -> {
                    if (!isScreenActive()) return;
                    typologyDocuments.clear();
                    updateDropdown(txtTipologia, new ArrayList<>());
                    Toast.makeText(this, "No se pudieron cargar las tipologías. Intenta nuevamente.",
                            Toast.LENGTH_LONG).show();
                });
    }

    private void bindTypologySelection() {
        txtTipologia.setOnItemClickListener((parent, view, position, id) -> {
            DocumentSnapshot selected = findSelectedTypology(txtTipologia.getText().toString());
            if (selected != null) showSuggestedAmount(selected);
        });
    }

    private void showSuggestedAmount(DocumentSnapshot typology) {
        TextView suggested = findViewById(R.id.txtSepMontoSugerido);
        double amount = SeparationPricingPolicy.number(firstValue(typology,
                "separationAmount", "montoSeparacion"));
        if (SeparationPricingPolicy.hasAmount(amount)) {
            suggested.setText("Monto sugerido para esta tipología: "
                    + SeparationPricingPolicy.formatPen(amount));
        }
    }

    private String typologyName(DocumentSnapshot typology) {
        return firstNonEmpty(typology.getString("title"), typology.getString("nombre"));
    }

    private void updateDropdown(AutoCompleteTextView view, List<String> items) {
        if (isScreenActive() && view != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, items);
            view.setAdapter(adapter);
        }
    }

    private boolean isScreenActive() {
        return !isFinishing() && !isDestroyed();
    }

    private String currentUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user == null ? "" : user.getUid();
    }

    private String valueOr(String value) {
        return value == null ? "" : value.trim();
    }

    private String firstNonEmpty(String... values) {
        if (values != null) for (String value : values) if (!valueOr(value).isEmpty()) return valueOr(value);
        return "";
    }

    private boolean isFromAppointment(Intent intent) {
        return intent != null && !valueOr(intent.getStringExtra(EXTRA_CITA_ID)).isEmpty()
                && !valueOr(intent.getStringExtra(EXTRA_CLIENTE_ID)).isEmpty()
                && !valueOr(intent.getStringExtra(EXTRA_PROJECT_ID)).isEmpty();
    }

    private DocumentSnapshot findSelectedTypology(String label) {
        for (DocumentSnapshot typology : typologyDocuments) {
            String title = firstNonEmpty(typology.getString("title"), typology.getString("nombre"));
            if (title.equalsIgnoreCase(valueOr(label))) return typology;
        }
        return null;
    }

    private void applyPricingSnapshot(FirebaseSeparationRepository.SeparationDraft draft,
                                      DocumentSnapshot typology) {
        double typedSeparation = SeparationPricingPolicy.number(draft.montoTexto);
        draft.montoSeparacion = typedSeparation;
        draft.montoSeparacionTexto = SeparationPricingPolicy.formatPen(typedSeparation);
        draft.currency = "PEN";
        if (typology == null) return;

        double total = SeparationPricingPolicy.number(firstValue(typology, "totalAmount", "montoTotal"));
        if (SeparationPricingPolicy.hasAmount(total)) {
            draft.precioTotal = total;
            String label = firstNonEmpty(typology.getString("totalAmountLabel"),
                    typology.getString("montoTotalLabel"));
            draft.precioTotalTexto = SeparationPricingPolicy.hasAmount(SeparationPricingPolicy.number(label))
                    ? label : SeparationPricingPolicy.formatPen(total);
        }
        String currency = valueOr(typology.getString("currency"));
        if (!currency.isEmpty()) draft.currency = currency;
    }

    private Object firstValue(DocumentSnapshot document, String first, String second) {
        Object value = document.get(first);
        return value != null ? value : document.get(second);
    }

    private DocumentSnapshot findSelectedProject(String label) {
        for (DocumentSnapshot project : projectDocuments) {
            if (projectLabel(project).equals(label) || projectName(project).equalsIgnoreCase(valueOr(label))) return project;
        }
        return null;
    }

    private String projectName(DocumentSnapshot project) {
        return firstNonEmpty(project.getString("nombre"), project.getString("title"), "Proyecto");
    }

    private String projectLabel(DocumentSnapshot project) {
        return projectName(project) + " · " + project.getId();
    }

    private String clientLabel(String name, String uid) {
        return name + " · " + uid;
    }

    private void setSubmitting(boolean submitting) {
        isSubmitting = submitting;
        View confirm = findViewById(R.id.btnConfirmarRegistroSeparacion);
        if (confirm != null) confirm.setEnabled(!submitting);
    }

    private static final class ClientOption {
        final String uid;
        final String name;
        ClientOption(String uid, String name) { this.uid = uid; this.name = name; }
    }
}
