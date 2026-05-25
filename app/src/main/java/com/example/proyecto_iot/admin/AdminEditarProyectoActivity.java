package com.example.proyecto_iot.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.adapter.AdminProjectFormAmenitiesAdapter;
import com.example.proyecto_iot.admin.adapter.AdminProjectFormTypologiesAdapter;
import com.example.proyecto_iot.admin.adapter.AdminProjectVisualEditorAdapter;
import com.example.proyecto_iot.admin.model.AdminEditedProjectRecord;
import com.example.proyecto_iot.admin.model.AdminProjectDraft;
import com.example.proyecto_iot.admin.model.AdminProjectFormAmenityItem;
import com.example.proyecto_iot.admin.model.AdminProjectFormTypologyItem;
import com.example.proyecto_iot.admin.notifications.AdminNotificationHelper;
import com.example.proyecto_iot.admin.storage.AdminLocalStorage;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.databinding.ActivityAdminEditarProyectoBinding;

import java.util.List;
import java.util.Locale;

public class AdminEditarProyectoActivity extends BaseAdminActivity {

    private ActivityAdminEditarProyectoBinding binding;
    private AdminProjectVisualEditorAdapter visualAdapter;
    private AdminProjectFormTypologiesAdapter typologiesAdapter;
    private AdminProjectFormAmenitiesAdapter amenitiesAdapter;
    private AdminLocalStorage adminLocalStorage;
    private String originalProjectTitle = "";
    private String selectedStatus = "En venta";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminEditarProyectoBinding.inflate(getLayoutInflater());
        setContentView(binding);
        adminLocalStorage = new AdminLocalStorage(this);
        originalProjectTitle = getIntent().getStringExtra("project_title");
        if (originalProjectTitle == null) {
            originalProjectTitle = "";
        }
        AdminNotificationHelper.setup(this);

        binding.btnBack.setOnClickListener(v -> saveDraftAndFinish());
        binding.btnCancelar.setOnClickListener(v -> saveDraftAndFinish());
        binding.btnGuardar.setOnClickListener(v -> {
            AdminProjectDraft draft = buildDraftFromUi();
            new LocalSchemaStorage(this).updateAdminProject(originalProjectTitle, draft);
            adminLocalStorage.saveEditedProject(draft);
            adminLocalStorage.clearEditProjectDraft();
            AdminNotificationHelper.showProjectEditedNotification(this, draft.getProjectName());
            Toast.makeText(this, "Cambios guardados en storage local", Toast.LENGTH_SHORT).show();
            finish();
        });

        setupVisualGallery();
        setupProjectCollections();

        setupEstadoSelector(
                binding.tvEstadoVentaEditar,
                binding.tvEstadoPlanosEditar,
                binding.tvEstadoPreventaEditar,
                binding.tvEstadoVentaEditar
        );

        binding.mapaProyectoEditar.setOnClickListener(v -> mostrarDialogoMapa(binding.tvMapaProyectoEditar));
        binding.tvMapaProyectoEditar.setOnClickListener(v -> mostrarDialogoMapa(binding.tvMapaProyectoEditar));
        binding.btnGestionarTipologiasEditar.setOnClickListener(v -> mostrarDialogoTipologia(-1));
        binding.btnAgregarAreaComunEditar.setOnClickListener(v -> mostrarDialogoAmenidad());

        restoreDraftIfAvailable();
        renderEditHistory();
    }

    @Override
    public void onBackPressed() {
        saveDraftAndFinish();
    }

    private void setupVisualGallery() {
        visualAdapter = new AdminProjectVisualEditorAdapter(item ->
                Toast.makeText(this, item.getActionLabel() + ": " + item.getTitle(), Toast.LENGTH_SHORT).show()
        );
        binding.rvMaterialVisualEditar.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvMaterialVisualEditar.setAdapter(visualAdapter);
        visualAdapter.setItems(new LocalSchemaStorage(this).getAdminProjectEditVisuals());
    }

    private void setupProjectCollections() {
        typologiesAdapter = new AdminProjectFormTypologiesAdapter(new AdminProjectFormTypologiesAdapter.Listener() {
            @Override
            public void onEditRequested(AdminProjectFormTypologyItem item, int position) {
                mostrarDialogoTipologia(position);
            }

            @Override
            public void onDeleteRequested(AdminProjectFormTypologyItem item, int position) {
                typologiesAdapter.removeItem(position);
            }
        });
        binding.rvTipologiasEditar.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTipologiasEditar.setAdapter(typologiesAdapter);
        typologiesAdapter.setItems(getSeedTypologies());

        amenitiesAdapter = new AdminProjectFormAmenitiesAdapter();
        binding.rvAmenidadesEditar.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvAmenidadesEditar.setAdapter(amenitiesAdapter);
        amenitiesAdapter.setItems(getSeedAmenities());
    }

    private List<AdminProjectFormTypologyItem> getSeedTypologies() {
        return new LocalSchemaStorage(this).getAdminProjectFormTypologies();
    }

    private List<AdminProjectFormAmenityItem> getSeedAmenities() {
        return new LocalSchemaStorage(this).getAdminProjectFormAmenities();
    }

    private void setupEstadoSelector(TextView seleccionado, TextView... opciones) {
        for (TextView opcion : opciones) {
            opcion.setOnClickListener(v -> aplicarEstado((TextView) v, opciones));
        }
        aplicarEstado(seleccionado, opciones);
    }

    private void aplicarEstado(TextView seleccionado, TextView... opciones) {
        selectedStatus = seleccionado.getText().toString();
        for (TextView opcion : opciones) {
            boolean activo = opcion == seleccionado;
            opcion.setBackgroundResource(activo ? R.drawable.bg_pill_active : android.R.color.transparent);
            opcion.setTextColor(activo ? android.graphics.Color.WHITE : android.graphics.Color.parseColor("#9AA3AF"));
            opcion.setTypeface(null, activo ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }
    }

    private void mostrarDialogoMapa(TextView tvMapa) {
        EditText input = createDialogField("Ej: Av. Santa Fe 455, Lomas de Santa Fe", "");

        new AlertDialog.Builder(this)
                .setTitle("Editar ubicacion en mapa")
                .setMessage("Actualiza el punto o referencia visible del proyecto.")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String ubicacion = input.getText().toString().trim();
                    if (ubicacion.isEmpty()) {
                        Toast.makeText(this, "Ubicacion del mapa conservada", Toast.LENGTH_SHORT).show();
                    } else {
                        tvMapa.setText("Mapa: " + ubicacion);
                    }
                })
                .show();
    }

    private void saveDraftAndFinish() {
        adminLocalStorage.saveEditProjectDraft(buildDraftFromUi());
        Toast.makeText(this, "Borrador de edicion guardado localmente", Toast.LENGTH_SHORT).show();
        finish();
    }

    private AdminProjectDraft buildDraftFromUi() {
        return new AdminProjectDraft(
                binding.etNombreProyectoEditar.getText().toString().trim(),
                binding.etDescripcionProyectoEditar.getText().toString().trim(),
                binding.etDireccionProyectoEditar.getText().toString().trim(),
                binding.etCiudadProyectoEditar.getText().toString().trim(),
                binding.tvMapaProyectoEditar.getText().toString(),
                selectedStatus,
                binding.etFechaEntregaEditar.getText().toString().trim(),
                typologiesAdapter.getItems(),
                amenitiesAdapter.getItems()
        );
    }

    private void restoreDraftIfAvailable() {
        AdminProjectDraft draft = adminLocalStorage.getEditProjectDraft();
        if (draft == null) {
            draft = new LocalSchemaStorage(this).getAdminProjectDraftForEdit(originalProjectTitle);
        }
        if (draft == null) {
            return;
        }

        binding.etNombreProyectoEditar.setText(draft.getProjectName());
        binding.etDescripcionProyectoEditar.setText(draft.getDescription());
        binding.etDireccionProyectoEditar.setText(draft.getAddress());
        binding.etCiudadProyectoEditar.setText(draft.getCity());
        binding.etFechaEntregaEditar.setText(draft.getDeliveryDate());
        if (!draft.getMapLabel().isEmpty()) {
            binding.tvMapaProyectoEditar.setText(draft.getMapLabel());
        }
        if (!draft.getTypologies().isEmpty()) {
            typologiesAdapter.setItems(draft.getTypologies());
        }
        if (!draft.getAmenities().isEmpty()) {
            amenitiesAdapter.setItems(draft.getAmenities());
        }
        applyStatusValue(draft.getStatus());
        Toast.makeText(this, "Borrador local de edicion restaurado", Toast.LENGTH_SHORT).show();
    }

    private void renderEditHistory() {
        List<AdminEditedProjectRecord> history = adminLocalStorage.getEditedProjectHistory();
        if (history.isEmpty()) {
            binding.cardHistorialEdiciones.setVisibility(View.GONE);
            return;
        }

        binding.cardHistorialEdiciones.setVisibility(View.VISIBLE);
        StringBuilder builder = new StringBuilder();
        int limit = Math.min(history.size(), 3);
        for (int i = 0; i < limit; i++) {
            AdminEditedProjectRecord record = history.get(i);
            if (i > 0) {
                builder.append("\n");
            }
            builder.append("- ")
                    .append(record.getProjectName())
                    .append(" | ")
                    .append(record.getStatus())
                    .append(" | ")
                    .append(record.getEditedAt());
        }
        binding.tvHistorialEdicionesListado.setText(builder.toString());
    }

    private void applyStatusValue(String status) {
        if ("En planos".equals(status)) {
            aplicarEstado(binding.tvEstadoPlanosEditar, binding.tvEstadoPlanosEditar, binding.tvEstadoPreventaEditar, binding.tvEstadoVentaEditar);
        } else if ("En preventa".equals(status)) {
            aplicarEstado(binding.tvEstadoPreventaEditar, binding.tvEstadoPlanosEditar, binding.tvEstadoPreventaEditar, binding.tvEstadoVentaEditar);
        } else {
            aplicarEstado(binding.tvEstadoVentaEditar, binding.tvEstadoPlanosEditar, binding.tvEstadoPreventaEditar, binding.tvEstadoVentaEditar);
        }
    }

    private void mostrarDialogoTipologia(int position) {
        AdminProjectFormTypologyItem currentItem = position >= 0 ? typologiesAdapter.getItem(position) : null;

        LinearLayout container = createDialogContainer();
        EditText nombreInput = createDialogField("Nombre de tipologia", currentItem != null ? currentItem.getTitle() : getNextTypologyName());
        EditText areaInput = createDialogField("Area. Ej: 70 m2", currentItem != null ? currentItem.getArea() : "70 m2");
        EditText habitacionesInput = createDialogField("Habitaciones. Ej: 2 habs", currentItem != null ? currentItem.getBedrooms() : "2 habs");
        EditText montoInput = createDialogField("Monto total. Ej: 350,000 USD", currentItem != null ? currentItem.getTotalAmount() : "350,000 USD");
        EditText separacionInput = createDialogField("Separacion. Ej: 1,400 USD", currentItem != null ? currentItem.getSeparationAmount() : "1,400 USD");
        CheckBox disponibleInput = new CheckBox(this);
        disponibleInput.setText("Disponible");
        disponibleInput.setChecked(currentItem == null || currentItem.isAvailable());

        addDialogView(container, nombreInput, 8);
        addDialogView(container, areaInput, 8);
        addDialogView(container, habitacionesInput, 8);
        addDialogView(container, montoInput, 8);
        addDialogView(container, separacionInput, 8);
        addDialogView(container, disponibleInput, 0);

        new AlertDialog.Builder(this)
                .setTitle(position >= 0 ? "Editar tipologia" : "Agregar tipologia")
                .setView(container)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(position >= 0 ? "Guardar" : "Agregar", (dialog, which) -> {
                    AdminProjectFormTypologyItem item = new AdminProjectFormTypologyItem(
                            getDialogValue(nombreInput, currentItem != null ? currentItem.getTitle() : getNextTypologyName()),
                            disponibleInput.isChecked(),
                            getDialogValue(areaInput, currentItem != null ? currentItem.getArea() : "70 m2"),
                            getDialogValue(habitacionesInput, currentItem != null ? currentItem.getBedrooms() : "2 habs"),
                            getDialogValue(montoInput, currentItem != null ? currentItem.getTotalAmount() : "350,000 USD"),
                            getDialogValue(separacionInput, currentItem != null ? currentItem.getSeparationAmount() : "1,400 USD")
                    );

                    if (position >= 0) {
                        typologiesAdapter.updateItem(position, item);
                    } else {
                        typologiesAdapter.addItem(item);
                    }
                })
                .show();
    }

    private void mostrarDialogoAmenidad() {
        EditText input = createDialogField("Ej: Coworking", "");

        new AlertDialog.Builder(this)
                .setTitle("Agregar amenidad")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Agregar", (dialog, which) -> {
                    String nombre = getDialogValue(input, "Amenidad " + (amenitiesAdapter.getItemCount() + 1));
                    amenitiesAdapter.addItem(new AdminProjectFormAmenityItem(nombre, resolveAmenityIcon(nombre), true));
                })
                .show();
    }

    private LinearLayout createDialogContainer() {
        LinearLayout container = new LinearLayout(this);
        int padding = dpToPx(8);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padding, padding, padding, 0);
        return container;
    }

    private EditText createDialogField(String hint, String value) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setText(value);
        input.setSingleLine(true);
        input.setBackgroundResource(R.drawable.bg_search_bar);
        input.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));
        return input;
    }

    private void addDialogView(LinearLayout container, View view, int bottomMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dpToPx(bottomMarginDp);
        container.addView(view, params);
    }

    private String getDialogValue(EditText input, String fallback) {
        String value = input.getText().toString().trim();
        return value.isEmpty() ? fallback : value;
    }

    private String getNextTypologyName() {
        int index = typologiesAdapter.getItemCount();
        if (index < 26) {
            return "Tipo " + (char) ('A' + index);
        }
        return "Tipo " + (index + 1);
    }

    private int resolveAmenityIcon(String nombre) {
        String normalized = nombre.toLowerCase(Locale.ROOT);
        if (normalized.contains("cowork") || normalized.contains("gym") || normalized.contains("gim")) {
            return R.drawable.ic_admin_laptop;
        }
        if (normalized.contains("pisc") || normalized.contains("pet")) {
            return R.drawable.ic_admin_pool;
        }
        if (normalized.contains("bbq") || normalized.contains("parr") || normalized.contains("lounge")) {
            return R.drawable.ic_email;
        }
        return R.drawable.ic_home;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
