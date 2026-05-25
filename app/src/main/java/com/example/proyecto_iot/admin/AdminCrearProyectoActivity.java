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
import com.example.proyecto_iot.admin.model.AdminProjectFormAmenityItem;
import com.example.proyecto_iot.admin.model.AdminProjectFormTypologyItem;
import com.example.proyecto_iot.admin.model.AdminProjectDraft;
import com.example.proyecto_iot.admin.notifications.AdminNotificationHelper;
import com.example.proyecto_iot.admin.storage.AdminLocalStorage;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.databinding.ActivityAdminCrearProyectoBinding;

import java.util.List;
import java.util.Locale;

public class AdminCrearProyectoActivity extends BaseAdminActivity {

    private ActivityAdminCrearProyectoBinding binding;
    private AdminProjectVisualEditorAdapter visualAdapter;
    private AdminProjectFormTypologiesAdapter typologiesAdapter;
    private AdminProjectFormAmenitiesAdapter amenitiesAdapter;
    private AdminLocalStorage adminLocalStorage;
    private String selectedStatus = "En planos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminCrearProyectoBinding.inflate(getLayoutInflater());
        setContentView(binding);
        adminLocalStorage = new AdminLocalStorage(this);
        AdminNotificationHelper.setup(this);

        binding.btnBack.setOnClickListener(v -> saveDraftAndFinish());
        binding.btnCancelar.setOnClickListener(v -> saveDraftAndFinish());
        binding.btnPublicar.setOnClickListener(v -> {
            AdminProjectDraft draft = buildDraftFromUi();
            String projectName = draft.getProjectName().isEmpty() ? "Proyecto sin nombre" : draft.getProjectName();
            new LocalSchemaStorage(this).addAdminProject(draft);
            adminLocalStorage.clearCreateProjectDraft();
            AdminNotificationHelper.showProjectPublishedNotification(this, projectName);
            Toast.makeText(this, "Proyecto publicado y guardado localmente", Toast.LENGTH_SHORT).show();
            finish();
        });

        setupVisualGallery();
        setupProjectCollections();

        setupEstadoSelector(
                binding.tvEstadoPlanosCrear,
                binding.tvEstadoPlanosCrear,
                binding.tvEstadoPreventaCrear,
                binding.tvEstadoVentaCrear
        );

        binding.mapaProyectoCrear.setOnClickListener(v -> mostrarDialogoMapa(binding.tvMapaProyectoCrear));
        binding.tvMapaProyectoCrear.setOnClickListener(v -> mostrarDialogoMapa(binding.tvMapaProyectoCrear));
        binding.btnAgregarTipologiaCrear.setOnClickListener(v -> mostrarDialogoTipologia(-1));
        binding.btnAgregarAreaComunCrear.setOnClickListener(v -> mostrarDialogoAmenidad());

        restoreDraftIfAvailable();
    }

    @Override
    public void onBackPressed() {
        saveDraftAndFinish();
    }

    private void setupVisualGallery() {
        visualAdapter = new AdminProjectVisualEditorAdapter(item ->
                Toast.makeText(this, item.getActionLabel() + ": " + item.getTitle(), Toast.LENGTH_SHORT).show()
        );
        binding.rvMaterialVisualCrear.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvMaterialVisualCrear.setAdapter(visualAdapter);
        visualAdapter.setItems(new LocalSchemaStorage(this).getAdminProjectCreateVisualSlots());
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
        binding.rvTipologiasCrear.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTipologiasCrear.setAdapter(typologiesAdapter);
        typologiesAdapter.setItems(getSeedTypologies());

        amenitiesAdapter = new AdminProjectFormAmenitiesAdapter();
        binding.rvAmenidadesCrear.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvAmenidadesCrear.setAdapter(amenitiesAdapter);
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
            opcion.setTextColor(activo ? android.graphics.Color.WHITE : android.graphics.Color.parseColor("#666666"));
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

    private void saveDraftAndFinish() {
        adminLocalStorage.saveCreateProjectDraft(buildDraftFromUi());
        Toast.makeText(this, "Borrador de proyecto guardado localmente", Toast.LENGTH_SHORT).show();
        finish();
    }

    private AdminProjectDraft buildDraftFromUi() {
        return new AdminProjectDraft(
                binding.etNombreProyectoCrear.getText().toString().trim(),
                binding.etDescripcionProyectoCrear.getText().toString().trim(),
                binding.etDireccionProyectoCrear.getText().toString().trim(),
                binding.etCiudadProyectoCrear.getText().toString().trim(),
                binding.tvMapaProyectoCrear.getText().toString(),
                selectedStatus,
                binding.etFechaEntregaCrear.getText().toString().trim(),
                typologiesAdapter.getItems(),
                amenitiesAdapter.getItems()
        );
    }

    private void restoreDraftIfAvailable() {
        AdminProjectDraft draft = adminLocalStorage.getCreateProjectDraft();
        if (draft == null) {
            return;
        }

        binding.etNombreProyectoCrear.setText(draft.getProjectName());
        binding.etDescripcionProyectoCrear.setText(draft.getDescription());
        binding.etDireccionProyectoCrear.setText(draft.getAddress());
        binding.etCiudadProyectoCrear.setText(draft.getCity());
        binding.etFechaEntregaCrear.setText(draft.getDeliveryDate());
        if (!draft.getMapLabel().isEmpty()) {
            binding.tvMapaProyectoCrear.setText(draft.getMapLabel());
        }
        if (!draft.getTypologies().isEmpty()) {
            typologiesAdapter.setItems(draft.getTypologies());
        }
        if (!draft.getAmenities().isEmpty()) {
            amenitiesAdapter.setItems(draft.getAmenities());
        }
        applyStatusValue(draft.getStatus());
        Toast.makeText(this, "Borrador local restaurado", Toast.LENGTH_SHORT).show();
    }

    private void applyStatusValue(String status) {
        if ("En preventa".equals(status)) {
            aplicarEstado(binding.tvEstadoPreventaCrear, binding.tvEstadoPlanosCrear, binding.tvEstadoPreventaCrear, binding.tvEstadoVentaCrear);
        } else if ("En venta".equals(status)) {
            aplicarEstado(binding.tvEstadoVentaCrear, binding.tvEstadoPlanosCrear, binding.tvEstadoPreventaCrear, binding.tvEstadoVentaCrear);
        } else {
            aplicarEstado(binding.tvEstadoPlanosCrear, binding.tvEstadoPlanosCrear, binding.tvEstadoPreventaCrear, binding.tvEstadoVentaCrear);
        }
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
