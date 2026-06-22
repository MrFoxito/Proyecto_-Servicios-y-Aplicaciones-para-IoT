package com.example.proyecto_iot.admin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.data.SupabaseStorageRepository;
import com.example.proyecto_iot.databinding.ActivityAdminEditarProyectoBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdminEditarProyectoActivity extends BaseAdminActivity {

    private static final String[] LIMA_DISTRICTS = {
            "Miraflores", "San Isidro", "Santiago de Surco", "Barranco", "Pueblo Libre",
            "San Miguel", "Jesus Maria", "Magdalena del Mar", "La Molina", "Lince",
            "Cercado de Lima", "San Borja", "Surquillo", "Chorrillos", "Los Olivos"
    };

    private static final String[] TYPOLOGY_TYPES = {"Tipo A", "Tipo B", "Tipo C", "Tipo D", "Flat", "Duplex", "Loft"};
    private static final String[] BEDROOM_OPTIONS = {"1 hab", "2 habs", "3 habs", "4+ habs"};
    private static final String[] BATHROOM_OPTIONS = {"1 bano", "2 banos", "3 banos", "4+ banos"};

    private ActivityAdminEditarProyectoBinding binding;
    private AdminProjectVisualEditorAdapter visualAdapter;
    private AdminProjectFormTypologiesAdapter typologiesAdapter;
    private AdminProjectFormAmenitiesAdapter amenitiesAdapter;
    private AdminLocalStorage adminLocalStorage;
    private ActivityResultLauncher<String[]> imagePickerLauncher;
    private String originalProjectTitle = "";
    private String selectedStatus = "En venta";
    private int pendingVisualPosition = -1;
    private double selectedLatitude = -12.0464;
    private double selectedLongitude = -77.0428;

    private interface ImageUploadCallback {
        void onSuccess(List<SupabaseStorageRepository.UploadResult> images);
        void onError(String message);
    }

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

        setupImagePicker();
        setupDistritoSelector();
        setupVisualGallery();
        setupProjectCollections();

        binding.btnBack.setOnClickListener(v -> saveDraftAndFinish());
        binding.btnCancelar.setOnClickListener(v -> saveDraftAndFinish());
        binding.btnGuardar.setOnClickListener(v -> saveProjectChanges());

        setupEstadoSelector(
                binding.tvEstadoVentaEditar,
                binding.tvEstadoPlanosEditar,
                binding.tvEstadoPreventaEditar,
                binding.tvEstadoVentaEditar
        );

        binding.etDireccionProyectoEditar.setOnClickListener(v -> mostrarDialogoMapa());
        binding.mapaProyectoEditar.setOnClickListener(v -> mostrarDialogoMapa());
        binding.tvMapaProyectoEditar.setOnClickListener(v -> mostrarDialogoMapa());
        binding.etFechaEntregaEditar.setOnClickListener(v -> mostrarCalendarioEntrega());
        binding.btnGestionarTipologiasEditar.setOnClickListener(v -> mostrarDialogoTipologia(-1));
        binding.btnAgregarAreaComunEditar.setOnClickListener(v -> mostrarDialogoAmenidad());

        restoreDraftIfAvailable();
        renderEditHistory();
    }

    @Override
    public void onBackPressed() {
        saveDraftAndFinish();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri == null || pendingVisualPosition < 0) {
                return;
            }
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) {
                // Some providers grant only session access.
            }
            visualAdapter.setDeviceImage(pendingVisualPosition, uri.toString());
            pendingVisualPosition = -1;
        });
    }

    private void setupDistritoSelector() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                LIMA_DISTRICTS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spDistritoProyectoEditar.setAdapter(adapter);
    }

    private void setupVisualGallery() {
        visualAdapter = new AdminProjectVisualEditorAdapter((item, position) -> mostrarOpcionesFoto(position, item.hasImage()));
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
                confirmarEliminacionTipologia(item, position);
            }
        });
        binding.rvTipologiasEditar.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTipologiasEditar.setAdapter(typologiesAdapter);
        typologiesAdapter.setItems(getSeedTypologies());

        amenitiesAdapter = new AdminProjectFormAmenitiesAdapter((item, position) -> amenitiesAdapter.removeItem(position));
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

    private void mostrarOpcionesFoto(int position, boolean hasImage) {
        if (!hasImage) {
            abrirSelectorFoto(position);
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Foto del proyecto")
                .setItems(new String[]{"Cambiar foto", "Eliminar foto"}, (dialog, which) -> {
                    if (which == 0) {
                        abrirSelectorFoto(position);
                    } else {
                        visualAdapter.clearImage(position);
                    }
                })
                .show();
    }

    private void abrirSelectorFoto(int position) {
        pendingVisualPosition = position;
        imagePickerLauncher.launch(new String[]{"image/*"});
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

    private void mostrarDialogoMapa() {
        LinearLayout container = createDialogContainer();
        TextView description = createDialogText("Toca el mapa para mover el marcador. Al confirmar, la direccion exacta se llenara con el punto elegido.");
        Spinner districtSpinner = createDialogSpinner(LIMA_DISTRICTS, selectedDistrito());
        EditText addressInput = createDialogField("Referencia exacta. Ej: Av. Larco 812", binding.etDireccionProyectoEditar.getText().toString());
        TextView coordinateText = createDialogText(formatCoordinates());
        FrameLayout mapArea = createMapPickerArea(coordinateText);
        TextView googleMapsAction = createDialogAction("Buscar direccion en Google Maps");

        addDialogView(container, description, 12);
        addDialogView(container, districtSpinner, 10);
        addDialogView(container, addressInput, 10);
        addDialogView(container, mapArea, 10);
        addDialogView(container, coordinateText, 8);
        addDialogView(container, googleMapsAction, 0);

        googleMapsAction.setOnClickListener(v -> openGoogleMapsSearch(
                getDialogValue(addressInput, ""),
                districtSpinner.getSelectedItem().toString()
        ));

        districtSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                moveCoordinatesNearDistrict(position);
                coordinateText.setText(formatCoordinates());
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Keeps current district.
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("Asignar ubicacion exacta")
                .setView(wrapDialogContent(container))
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar ubicacion", (dialog, which) -> {
                    String district = districtSpinner.getSelectedItem().toString();
                    String address = getDialogValue(addressInput, "Punto seleccionado en " + district);
                    setDistritoSelection(district);
                    binding.etDireccionProyectoEditar.setText(address);
                    binding.tvMapaProyectoEditar.setText("Mapa: " + district + " | " + formatCoordinates());
                })
                .show();
    }

    private FrameLayout createMapPickerArea(TextView coordinateText) {
        FrameLayout mapArea = new FrameLayout(this);
        mapArea.setBackgroundResource(R.drawable.user_detail_map_area_bg);
        mapArea.setMinimumHeight(dpToPx(190));

        ImageView marker = new ImageView(this);
        marker.setImageResource(android.R.drawable.ic_menu_mylocation);
        marker.setColorFilter(android.graphics.Color.parseColor("#8A6D3B"));
        FrameLayout.LayoutParams markerParams = new FrameLayout.LayoutParams(dpToPx(36), dpToPx(36));
        markerParams.leftMargin = dpToPx(142);
        markerParams.topMargin = dpToPx(76);
        mapArea.addView(marker, markerParams);

        mapArea.setOnTouchListener((view, event) -> {
            if (event.getAction() != MotionEvent.ACTION_DOWN && event.getAction() != MotionEvent.ACTION_MOVE) {
                return true;
            }
            int width = Math.max(1, view.getWidth());
            int height = Math.max(1, view.getHeight());
            float x = clamp(event.getX(), 0, width);
            float y = clamp(event.getY(), 0, height);

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) marker.getLayoutParams();
            params.leftMargin = Math.round(x - dpToPx(18));
            params.topMargin = Math.round(y - dpToPx(18));
            marker.setLayoutParams(params);

            selectedLatitude = -12.16 + (0.26 * (1f - (y / height)));
            selectedLongitude = -77.12 + (0.20 * (x / width));
            coordinateText.setText(formatCoordinates());
            return true;
        });

        return mapArea;
    }

    private void moveCoordinatesNearDistrict(int position) {
        selectedLatitude = -12.0464 - (position * 0.004);
        selectedLongitude = -77.0428 + (position * 0.003);
    }

    private void mostrarCalendarioEntrega() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    binding.etFechaEntregaEditar.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selected.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void mostrarDialogoTipologia(int position) {
        AdminProjectFormTypologyItem currentItem = position >= 0 ? typologiesAdapter.getItem(position) : null;

        LinearLayout container = createDialogContainer();
        TextView description = createDialogText("Completa la configuracion del departamento. Usa formato coherente para area, cantidades y montos.");
        Spinner tipoSpinner = createDialogSpinner(TYPOLOGY_TYPES, currentItem != null ? currentItem.getTitle() : getNextTypologyName());
        EditText areaInput = createDialogField("Area en m2. Ej: 70", currentItem != null ? stripUnit(currentItem.getArea(), " m2") : "70");
        Spinner habitacionesInput = createDialogSpinner(BEDROOM_OPTIONS, currentItem != null ? currentItem.getBedrooms() : "2 habs");
        Spinner banosInput = createDialogSpinner(BATHROOM_OPTIONS, currentItem != null ? currentItem.getBathrooms() : "2 banos");
        EditText montoInput = createDialogField("Precio total. Ej: 350000 USD", currentItem != null ? currentItem.getTotalAmount() : "350,000 USD");
        EditText separacionInput = createDialogField("Monto de separacion. Ej: 1500 USD", currentItem != null ? currentItem.getSeparationAmount() : "1,500 USD");
        CheckBox disponibleInput = new CheckBox(this);
        disponibleInput.setText("Disponible");
        disponibleInput.setChecked(currentItem == null || currentItem.isAvailable());

        areaInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        montoInput.setInputType(InputType.TYPE_CLASS_TEXT);
        separacionInput.setInputType(InputType.TYPE_CLASS_TEXT);

        addDialogView(container, description, 10);
        addDialogView(container, createLabeledDialogView("Tipologia", tipoSpinner), 10);
        addDialogView(container, createLabeledDialogView("Metraje o area de departamento", areaInput), 10);
        addDialogView(container, createLabeledDialogView("Habitaciones", habitacionesInput), 10);
        addDialogView(container, createLabeledDialogView("Banos", banosInput), 10);
        addDialogView(container, createLabeledDialogView("Monto total del departamento", montoInput), 10);
        addDialogView(container, createLabeledDialogView("Monto de separacion", separacionInput), 8);
        addDialogView(container, disponibleInput, 0);

        new AlertDialog.Builder(this)
                .setTitle(position >= 0 ? "Editar tipologia" : "Agregar tipologia")
                .setView(wrapDialogContent(container))
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(position >= 0 ? "Guardar" : "Agregar", (dialog, which) -> {
                    AdminProjectFormTypologyItem item = new AdminProjectFormTypologyItem(
                            tipoSpinner.getSelectedItem().toString(),
                            disponibleInput.isChecked(),
                            normalizeArea(getDialogValue(areaInput, currentItem != null ? currentItem.getArea() : "70")),
                            habitacionesInput.getSelectedItem().toString(),
                            banosInput.getSelectedItem().toString(),
                            normalizeUsdAmount(getDialogValue(montoInput, currentItem != null ? currentItem.getTotalAmount() : "350,000 USD")),
                            normalizeUsdAmount(getDialogValue(separacionInput, currentItem != null ? currentItem.getSeparationAmount() : "1,500 USD"))
                    );

                    if (position >= 0) {
                        typologiesAdapter.updateItem(position, item);
                        Toast.makeText(this, "Tipologia actualizada correctamente", Toast.LENGTH_SHORT).show();
                    } else {
                        typologiesAdapter.addItem(item);
                        Toast.makeText(this, "Tipologia agregada correctamente", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void confirmarEliminacionTipologia(AdminProjectFormTypologyItem item, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar tipologia")
                .setMessage("Quieres eliminar la configuracion " + item.getTitle() + "?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> typologiesAdapter.removeItem(position))
                .show();
    }

    private void mostrarDialogoAmenidad() {
        LinearLayout container = createDialogContainer();
        TextView description = createDialogText("Elige una amenidad predefinida. Cada una se agregara con su icono y podras tocarla para deshabilitarla.");
        Spinner amenitySpinner = createDialogSpinner(getAmenityNames(), getAmenityNames()[0]);

        addDialogView(container, description, 12);
        addDialogView(container, amenitySpinner, 0);

        new AlertDialog.Builder(this)
                .setTitle("Agregar amenidad")
                .setView(container)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Agregar", (dialog, which) -> {
                    String amenity = amenitySpinner.getSelectedItem().toString();
                    if (amenitiesAdapter.containsTitle(amenity)) {
                        Toast.makeText(this, "La amenidad ya esta agregada", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    amenitiesAdapter.addItem(new AdminProjectFormAmenityItem(amenity, resolveAmenityIcon(amenity), true));
                })
                .show();
    }

    private void saveProjectChanges() {
        AdminProjectDraft draft = buildDraftFromUi();
        if (draft.getProjectName().isEmpty() || draft.getDescription().isEmpty()) {
            Toast.makeText(this, "Completa nombre y descripcion del proyecto", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Guardar cambios")
                .setMessage("Deseas guardar los cambios de \"" + draft.getProjectName() + "\"?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    FirebaseDataRepository firebaseRepository = new FirebaseDataRepository();
                    String projectId = firebaseRepository.projectIdForDraft(draft, originalProjectTitle);
                    List<String> newImageUris = visualAdapter.getDeviceImageUris();
                    if (newImageUris.isEmpty()) {
                        saveProjectAfterOptionalUpload(firebaseRepository, draft, null);
                    } else {
                        uploadProjectImages(projectId, new ImageUploadCallback() {
                            @Override
                            public void onSuccess(List<SupabaseStorageRepository.UploadResult> images) {
                                saveProjectAfterOptionalUpload(firebaseRepository, draft, images);
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(AdminEditarProyectoActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                })
                .show();
    }

    private void saveProjectAfterOptionalUpload(
            FirebaseDataRepository firebaseRepository,
            AdminProjectDraft draft,
            List<SupabaseStorageRepository.UploadResult> images
    ) {
        firebaseRepository.saveProjectWithImages(draft, originalProjectTitle, images, new FirebaseDataRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                new LocalSchemaStorage(AdminEditarProyectoActivity.this).updateAdminProject(originalProjectTitle, draft);
                adminLocalStorage.saveEditedProject(draft);
                adminLocalStorage.clearEditProjectDraft();
                AdminNotificationHelper.showProjectEditedNotification(AdminEditarProyectoActivity.this, draft.getProjectName());
                Toast.makeText(AdminEditarProyectoActivity.this, "Cambios guardados en Firebase y Supabase", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AdminEditarProyectoActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void uploadProjectImages(String projectId, ImageUploadCallback callback) {
        List<String> uris = visualAdapter.getDeviceImageUris();
        if (uris.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        List<SupabaseStorageRepository.UploadResult> uploadedImages = new ArrayList<>();
        SupabaseStorageRepository storageRepository = new SupabaseStorageRepository(this);
        uploadProjectImageAt(projectId, uris, 0, uploadedImages, callback, storageRepository);
    }

    private void uploadProjectImageAt(
            String projectId,
            List<String> uris,
            int index,
            List<SupabaseStorageRepository.UploadResult> uploadedImages,
            ImageUploadCallback callback,
            SupabaseStorageRepository storageRepository
    ) {
        if (index >= uris.size()) {
            callback.onSuccess(uploadedImages);
            return;
        }

        storageRepository.uploadProjectImage(projectId, Uri.parse(uris.get(index)), new SupabaseStorageRepository.UploadCallback() {
            @Override
            public void onSuccess(SupabaseStorageRepository.UploadResult result) {
                uploadedImages.add(result);
                uploadProjectImageAt(projectId, uris, index + 1, uploadedImages, callback, storageRepository);
            }

            @Override
            public void onError(String message) {
                callback.onError("No se guardaron los cambios porque Supabase rechazo una imagen: " + message);
            }
        });
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
                selectedDistrito(),
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
        setDistritoSelection(draft.getCity());
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
        Toast.makeText(this, "Datos del proyecto cargados", Toast.LENGTH_SHORT).show();
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

    private LinearLayout createDialogContainer() {
        LinearLayout container = new LinearLayout(this);
        int padding = dpToPx(8);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(padding, padding, padding, 0);
        return container;
    }

    private TextView createDialogText(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(android.graphics.Color.parseColor("#6B7280"));
        view.setTextSize(12);
        return view;
    }

    private EditText createDialogField(String hint, String value) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setText(value);
        input.setSingleLine(true);
        input.setSelectAllOnFocus(true);
        input.setFocusable(true);
        input.setFocusableInTouchMode(true);
        input.setBackgroundResource(R.drawable.bg_search_bar);
        input.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));
        input.setMinHeight(dpToPx(52));
        input.setTextSize(14);
        return input;
    }

    private ScrollView wrapDialogContent(View content) {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(false);
        scrollView.addView(content);
        return scrollView;
    }

    private TextView createDialogAction(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(android.graphics.Color.parseColor("#8A6D3B"));
        view.setTextSize(12);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setGravity(android.view.Gravity.CENTER);
        view.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
        view.setBackgroundResource(R.drawable.bg_btn_light_grey);
        return view;
    }

    private Spinner createDialogSpinner(String[] values, String selectedValue) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        int selectedIndex = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equalsIgnoreCase(selectedValue)) {
                selectedIndex = i;
                break;
            }
        }
        spinner.setSelection(selectedIndex);
        spinner.setBackgroundResource(R.drawable.bg_search_bar);
        spinner.setPadding(dpToPx(10), 0, dpToPx(10), 0);
        spinner.setMinimumHeight(dpToPx(52));
        return spinner;
    }

    private LinearLayout createLabeledDialogView(String label, View field) {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(this);
        title.setText(label);
        title.setTextColor(android.graphics.Color.parseColor("#5E6A72"));
        title.setTextSize(10);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setAllCaps(true);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.bottomMargin = dpToPx(6);
        wrapper.addView(title, titleParams);

        wrapper.addView(field, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(52)
        ));
        return wrapper;
    }

    private void addDialogView(LinearLayout container, View view, int bottomMarginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                view instanceof FrameLayout ? dpToPx(190) : LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dpToPx(bottomMarginDp);
        container.addView(view, params);
    }

    private void openGoogleMapsSearch(String address, String district) {
        String query = address.trim().isEmpty()
                ? district + ", Lima, Peru"
                : address + ", " + district + ", Lima, Peru";
        Uri uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(query));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private String getDialogValue(EditText input, String fallback) {
        String value = input.getText().toString().trim();
        return value.isEmpty() ? fallback : value;
    }

    private String getNextTypologyName() {
        int index = typologiesAdapter.getItemCount();
        if (index < 4) {
            return TYPOLOGY_TYPES[index];
        }
        return TYPOLOGY_TYPES[0];
    }

    private String selectedDistrito() {
        Object selected = binding.spDistritoProyectoEditar.getSelectedItem();
        return selected == null ? LIMA_DISTRICTS[0] : selected.toString();
    }

    private void setDistritoSelection(String district) {
        for (int i = 0; i < LIMA_DISTRICTS.length; i++) {
            if (LIMA_DISTRICTS[i].equalsIgnoreCase(district)) {
                binding.spDistritoProyectoEditar.setSelection(i);
                return;
            }
        }
    }

    private String[] getAmenityNames() {
        return new String[]{
                "Piscina", "Gimnasio", "Coworking", "Zona BBQ", "Sala lounge",
                "Juegos para ninos", "Pet zone", "Estacionamiento", "Bicicletario",
                "Terraza", "Seguridad 24/7", "Lobby"
        };
    }

    private int resolveAmenityIcon(String nombre) {
        String normalized = nombre.toLowerCase(Locale.ROOT);
        if (normalized.contains("cowork")) {
            return R.drawable.ic_admin_laptop;
        }
        if (normalized.contains("pisc")) {
            return R.drawable.ic_admin_pool;
        }
        if (normalized.contains("gim")) {
            return R.drawable.ic_amenity_gym;
        }
        if (normalized.contains("bbq") || normalized.contains("parr")) {
            return R.drawable.ic_amenity_bbq;
        }
        if (normalized.contains("pet")) {
            return R.drawable.ic_amenity_pet;
        }
        if (normalized.contains("seguridad")) {
            return R.drawable.ic_amenity_security;
        }
        if (normalized.contains("estacion")) {
            return R.drawable.ic_amenity_parking;
        }
        if (normalized.contains("bici")) {
            return R.drawable.ic_amenity_bike;
        }
        if (normalized.contains("terraza")) {
            return R.drawable.ic_amenity_terrace;
        }
        if (normalized.contains("juegos")) {
            return R.drawable.ic_amenity_playground;
        }
        if (normalized.contains("lobby") || normalized.contains("lounge")) {
            return R.drawable.ic_amenity_lobby;
        }
        return R.drawable.ic_home;
    }

    private String normalizeArea(String rawArea) {
        String value = rawArea.trim();
        return value.toLowerCase(Locale.ROOT).contains("m2") ? value : value + " m2";
    }

    private String normalizeUsdAmount(String rawAmount) {
        String value = rawAmount.trim();
        return value.toUpperCase(Locale.ROOT).contains("USD") ? value : value + " USD";
    }

    private String stripUnit(String value, String unit) {
        return value == null ? "" : value.replace(unit, "").trim();
    }

    private String formatCoordinates() {
        return String.format(Locale.US, "Lat %.5f, Lng %.5f", selectedLatitude, selectedLongitude);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
