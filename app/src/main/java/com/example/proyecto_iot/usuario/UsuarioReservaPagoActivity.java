package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseAppointmentRepository;
import com.example.proyecto_iot.data.FirebaseSeparationRepository;
import com.example.proyecto_iot.data.ProjectBusinessRules;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Confirmation step for a temporary unit hold. It never marks a client payment as completed. */
public class UsuarioReservaPagoActivity extends AppCompatActivity {

    public static final String EXTRA_PROPERTY_ID = "extra_reserva_property_id";
    public static final String EXTRA_PROPERTY_TITLE = "extra_reserva_property_title";
    public static final String EXTRA_PROPERTY_PRICE = "extra_reserva_property_price";
    public static final String EXTRA_PROPERTY_LOCATION = "extra_reserva_property_location";
    public static final String EXTRA_PROPERTY_STATUS = "extra_reserva_property_status";
    public static final String EXTRA_PROPERTY_IMAGE_URL = "extra_reserva_property_image_url";

    private final FirebaseSeparationRepository separationRepository = new FirebaseSeparationRepository();
    private final FirebaseAppointmentRepository appointmentRepository = new FirebaseAppointmentRepository();
    private final List<FirebaseSeparationRepository.TemporaryUnit> units = new ArrayList<>();
    private final List<FirebaseAppointmentRepository.Advisor> advisors = new ArrayList<>();

    private String propertyId = "";
    private String propertyTitle = "";
    private String propertyLocation = "";
    private String propertyImageUrl = "";
    private String propertyStatus = ProjectBusinessRules.STATUS_PLANOS;
    private Spinner unitSpinner;
    private Spinner advisorSpinner;
    private TextView amount;
    private TextView availability;
    private View confirm;
    private boolean creating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_reserva_pago);
        applyInsets();
        bindInput();
        setupActions();
        loadSelectionData();
    }

    private void bindInput() {
        Intent intent = getIntent();
        propertyId = value(intent == null ? null : intent.getStringExtra(EXTRA_PROPERTY_ID));
        propertyTitle = value(intent == null ? null : intent.getStringExtra(EXTRA_PROPERTY_TITLE));
        propertyLocation = value(intent == null ? null : intent.getStringExtra(EXTRA_PROPERTY_LOCATION));
        propertyImageUrl = value(intent == null ? null : intent.getStringExtra(EXTRA_PROPERTY_IMAGE_URL));
        propertyStatus = ProjectBusinessRules.normalizeStatus(intent == null ? null : intent.getStringExtra(EXTRA_PROPERTY_STATUS));

        ((TextView) findViewById(R.id.tvReservaPropertyTitle)).setText(
                propertyTitle.isEmpty() ? getString(R.string.property_title) : propertyTitle);
        ((TextView) findViewById(R.id.tvReservaPropertyLocation)).setText(propertyLocation);
        ImageView image = findViewById(R.id.ivReservaProject);
        if (!propertyImageUrl.isEmpty()) {
            Glide.with(this).load(propertyImageUrl).centerCrop().into(image);
        } else {
            image.setImageResource(R.drawable.user_property_hero_real);
        }
        unitSpinner = findViewById(R.id.spReservaUnit);
        advisorSpinner = findViewById(R.id.spReservaAdvisor);
        amount = findViewById(R.id.tvReservaPropertyValue);
        availability = findViewById(R.id.tvReservaUnitAvailability);
        confirm = findViewById(R.id.btnProcederPago);
        amount.setText("Selecciona una unidad");
    }

    private void setupActions() {
        findViewById(R.id.btnBackReservaPago).setOnClickListener(v -> finish());
        unitSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { updateSelectedUnit(); }
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) { updateSelectedUnit(); }
        });
        confirm.setOnClickListener(v -> confirmTemporarySeparation());
    }

    private void loadSelectionData() {
        if (propertyId.isEmpty() || !ProjectBusinessRules.canCreateSeparation(propertyStatus)) {
            Toast.makeText(this, "Este proyecto no permite separaciones por el momento.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        separationRepository.readTemporarySeparationUnits(propertyId, new FirebaseSeparationRepository.TemporaryUnitsCallback() {
            @Override public void onSuccess(List<FirebaseSeparationRepository.TemporaryUnit> items) {
                if (!active()) return;
                units.clear();
                if (items != null) units.addAll(items);
                List<String> labels = new ArrayList<>();
                for (FirebaseSeparationRepository.TemporaryUnit unit : units) {
                    labels.add(unit.title + " · " + unit.availabilityLabel);
                }
                unitSpinner.setAdapter(new ArrayAdapter<>(UsuarioReservaPagoActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, labels));
                updateSelectedUnit();
            }
            @Override public void onError(String message) {
                if (active()) Toast.makeText(UsuarioReservaPagoActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
        appointmentRepository.getAdvisorsForProject(propertyId, new FirebaseAppointmentRepository.AdvisorsCallback() {
            @Override public void onSuccess(List<FirebaseAppointmentRepository.Advisor> items) {
                if (!active()) return;
                advisors.clear();
                if (items != null) advisors.addAll(items);
                List<String> labels = new ArrayList<>();
                for (FirebaseAppointmentRepository.Advisor advisor : advisors) labels.add(advisor.name);
                advisorSpinner.setAdapter(new ArrayAdapter<>(UsuarioReservaPagoActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, labels));
                updateConfirmEnabled();
            }
            @Override public void onError(String message) {
                if (active()) Toast.makeText(UsuarioReservaPagoActivity.this,
                        "No se pudo cargar el asesor asignado: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateSelectedUnit() {
        FirebaseSeparationRepository.TemporaryUnit unit = selectedUnit();
        if (unit == null) {
            amount.setText("No hay unidades disponibles");
            availability.setText("Este proyecto todavía no tiene unidades separables configuradas.");
        } else {
            amount.setText(formatPen(unit.separationAmount));
            availability.setText(unit.availabilityLabel + (unit.selectable ? "" : ". Elige otra unidad."));
        }
        updateConfirmEnabled();
    }

    private void updateConfirmEnabled() {
        boolean ready = !creating && selectedUnit() != null && selectedUnit().selectable && selectedAdvisor() != null;
        confirm.setEnabled(ready);
        confirm.setAlpha(ready ? 1f : .5f);
    }

    private void confirmTemporarySeparation() {
        FirebaseSeparationRepository.TemporaryUnit unit = selectedUnit();
        FirebaseAppointmentRepository.Advisor advisor = selectedAdvisor();
        if (creating || unit == null || !unit.selectable || advisor == null) return;
        String message = "Proyecto: " + propertyTitle
                + "\nUnidad: " + unit.title
                + "\nMonto de separación: " + formatPen(unit.separationAmount)
                + "\n\nLa unidad quedará bloqueada durante 24 horas. Se liberará automáticamente si el pago no se confirma.";
        new AlertDialog.Builder(this)
                .setTitle("Confirmar separación")
                .setMessage(message)
                .setNegativeButton("Volver", null)
                .setPositiveButton("Retener unidad", (dialog, which) -> createTemporarySeparation(unit, advisor))
                .show();
    }

    private void createTemporarySeparation(FirebaseSeparationRepository.TemporaryUnit unit,
                                           FirebaseAppointmentRepository.Advisor advisor) {
        creating = true;
        updateConfirmEnabled();
        FirebaseSeparationRepository.TemporarySeparationDraft draft = new FirebaseSeparationRepository.TemporarySeparationDraft();
        draft.projectId = propertyId;
        draft.typologyId = unit.id;
        draft.advisorId = advisor.uid;
        draft.assignmentId = advisor.assignmentId;
        separationRepository.createTemporarySeparation(draft, new FirebaseSeparationRepository.SimpleCallback() {
            @Override public void onSuccess(String separationId) {
                if (!active()) return;
                Intent intent = new Intent(UsuarioReservaPagoActivity.this, UsuarioSeguimientoSeparacionActivity.class);
                intent.putExtra(UsuarioSeguimientoSeparacionActivity.EXTRA_SEPARATION_ID, separationId);
                startActivity(intent);
                finish();
            }
            @Override public void onError(String message) {
                if (!active()) return;
                creating = false;
                updateConfirmEnabled();
                Toast.makeText(UsuarioReservaPagoActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private FirebaseSeparationRepository.TemporaryUnit selectedUnit() {
        int index = unitSpinner == null ? -1 : unitSpinner.getSelectedItemPosition();
        return index >= 0 && index < units.size() ? units.get(index) : null;
    }

    private FirebaseAppointmentRepository.Advisor selectedAdvisor() {
        int index = advisorSpinner == null ? -1 : advisorSpinner.getSelectedItemPosition();
        return index >= 0 && index < advisors.size() ? advisors.get(index) : null;
    }

    private String formatPen(double value) {
        return "S/ " + NumberFormat.getNumberInstance(new Locale("es", "PE")).format(value);
    }

    private boolean active() { return !isFinishing() && !isDestroyed(); }
    private String value(String text) { return text == null ? "" : text.trim(); }

    private void applyInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View root = findViewById(R.id.reservaPagoRoot);
        final int left = root.getPaddingLeft(), top = root.getPaddingTop(), right = root.getPaddingRight(), bottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
