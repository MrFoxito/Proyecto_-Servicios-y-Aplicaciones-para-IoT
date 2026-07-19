package com.example.proyecto_iot.admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.adapter.AdminProjectAmenitiesAdapter;
import com.example.proyecto_iot.admin.adapter.AdminProjectGalleryAdapter;
import com.example.proyecto_iot.admin.adapter.AdminProjectTypologiesAdapter;
import com.example.proyecto_iot.admin.model.AdminProjectAmenityItem;
import com.example.proyecto_iot.admin.model.AdminProjectFormAmenityItem;
import com.example.proyecto_iot.admin.model.AdminProjectFormTypologyItem;
import com.example.proyecto_iot.admin.model.AdminProjectGalleryItem;
import com.example.proyecto_iot.admin.model.AdminProjectTypologyItem;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.data.ProjectBusinessRules;
import com.example.proyecto_iot.data.QrCodeGenerator;
import com.example.proyecto_iot.data.QrCodeStorage;
import com.example.proyecto_iot.databinding.ActivityAdminDetalleProyectoBinding;
import com.example.proyecto_iot.maps.ProjectMapPreviewController;

import java.util.ArrayList;
import java.util.List;

/**
 * Vista de detalle de un proyecto inmobiliario.
 */
public class AdminDetalleProyectoActivity extends BaseAdminActivity {

    private ActivityAdminDetalleProyectoBinding binding;
    private AdminProjectGalleryAdapter galleryAdapter;
    private AdminProjectTypologiesAdapter typologiesAdapter;
    private AdminProjectAmenitiesAdapter amenitiesAdapter;
    private String projectId = "";
    private String projectTitle = "";
    private Bitmap qrBitmap;
    private ProjectMapPreviewController mapPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDetalleProyectoBinding.inflate(getLayoutInflater());
        setContentView(binding);
        Intent sourceIntent = getIntent();
        projectId = sourceIntent == null ? "" : valueOr(sourceIntent.getStringExtra("project_id"));
        projectTitle = sourceIntent == null ? "" : sourceIntent.getStringExtra("project_title");
        if (projectTitle == null) {
            projectTitle = "";
        }
        if (valueOr(projectId, projectTitle).isEmpty()) {
            Toast.makeText(this, "No se recibió un proyecto válido.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setupBackButton();
        setupLists();
        binding.btnEditarProyecto.setEnabled(false);
        binding.btnEditarProyecto.setAlpha(0.55f);
        mapPreview = new ProjectMapPreviewController(
                this,
                R.id.mapaPreviewDetalle,
                Double.NaN,
                Double.NaN,
                null
        );
        loadProjectDetail();
        binding.btnDownloadQrProject.setOnClickListener(v -> downloadQr());
        binding.btnEditarProyecto.setOnClickListener(v -> {
            if (valueOr(projectId, projectTitle).isEmpty()) {
                Toast.makeText(this, "Espera a que termine de cargar el proyecto.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, AdminEditarProyectoActivity.class);
            intent.putExtra("project_id", projectId);
            intent.putExtra("project_title", projectTitle);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void setupLists() {
        galleryAdapter = new AdminProjectGalleryAdapter();
        binding.rvProjectGallery.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvProjectGallery.setAdapter(galleryAdapter);
        galleryAdapter.setItems(new ArrayList<>());

        typologiesAdapter = new AdminProjectTypologiesAdapter();
        binding.rvProjectTypologies.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProjectTypologies.setAdapter(typologiesAdapter);
        typologiesAdapter.setItems(new ArrayList<>());

        amenitiesAdapter = new AdminProjectAmenitiesAdapter();
        binding.rvProjectAmenities.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvProjectAmenities.setAdapter(amenitiesAdapter);
        amenitiesAdapter.setItems(new ArrayList<>());
    }

    private void loadProjectDetail() {
        String lookup = projectId.isEmpty() ? projectTitle : projectId;
        if (lookup.isEmpty()) return;
        new FirebaseDataRepository().readProjectDetailByReference(lookup, new FirebaseDataRepository.ProjectDetailCallback() {
            @Override
            public void onSuccess(FirebaseDataRepository.ProjectDetail detail) {
                if (isFinishing() || isDestroyed()) return;
                projectId = detail.projectId;
                projectTitle = detail.nombre;
                binding.btnEditarProyecto.setEnabled(true);
                binding.btnEditarProyecto.setAlpha(1f);
                binding.tvDetalleProjectName.setText(detail.nombre);
                binding.tvDetalleProjectDescription.setText(detail.descripcion);
                binding.tvDetalleProjectAddress.setText(detail.direccion + " - " + detail.distrito);
                binding.tvDetalleDeliveryDate.setText(detail.fechaEntrega.isEmpty() ? "Pendiente" : detail.fechaEntrega);
                binding.tvDetalleQrValue.setText(detail.qrValue);
                bindStatus(detail.estadoProyecto);
                mapPreview.showLocation(detail.lat, detail.lng);
                qrBitmap = QrCodeGenerator.create(detail.qrValue, 768);
                if (qrBitmap != null) {
                    binding.imgDetalleQrProyecto.setImageBitmap(qrBitmap);
                }
                loadProjectAssets(detail.projectId, detail.imageUrl);
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                bindStatus("");
                binding.btnEditarProyecto.setEnabled(false);
                binding.btnEditarProyecto.setAlpha(0.55f);
                Toast.makeText(AdminDetalleProyectoActivity.this,
                        "No se pudo cargar el proyecto: " + message,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindStatus(String status) {
        TextView planos = binding.tvStatusPlanosDetalle;
        TextView preventa = binding.tvStatusPreventaDetalle;
        TextView venta = binding.tvStatusVentaDetalle;
        applyStatusPill(planos, ProjectBusinessRules.STATUS_PLANOS.equals(ProjectBusinessRules.normalizeStatus(status)));
        applyStatusPill(preventa, ProjectBusinessRules.STATUS_PREVENTA.equals(ProjectBusinessRules.normalizeStatus(status)));
        applyStatusPill(venta, ProjectBusinessRules.STATUS_VENTA.equals(ProjectBusinessRules.normalizeStatus(status)));
    }

    private void applyStatusPill(TextView view, boolean selected) {
        view.setBackgroundResource(selected ? R.drawable.admin_detail_dark_pill : android.R.color.transparent);
        view.setTextColor(selected ? Color.WHITE : Color.parseColor("#464D53"));
    }

    private String valueOr(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private void loadProjectAssets(String id, String primaryImageUrl) {
        new FirebaseDataRepository().readProjectAssets(id, new FirebaseDataRepository.ProjectAssetsCallback() {
            @Override
            public void onSuccess(FirebaseDataRepository.ProjectAssets assets) {
                if (isFinishing() || isDestroyed()) return;
                List<AdminProjectGalleryItem> gallery = new ArrayList<>();
                if (primaryImageUrl != null && !primaryImageUrl.isEmpty()) {
                    gallery.add(new AdminProjectGalleryItem(0, primaryImageUrl));
                }
                for (String url : assets.imageUrls) {
                    if (!url.equals(primaryImageUrl)) {
                        gallery.add(new AdminProjectGalleryItem(0, url));
                    }
                }
                galleryAdapter.setItems(gallery);

                List<AdminProjectTypologyItem> typologies = new ArrayList<>();
                for (AdminProjectFormTypologyItem item : assets.typologies) {
                    typologies.add(new AdminProjectTypologyItem(
                            item.getTitle(),
                            item.isAvailable() ? "DISPONIBLE" : "NO DISPONIBLE",
                            item.isAvailable(),
                            item.getArea(),
                            item.getBedrooms(),
                            item.getBathrooms(),
                            item.getTotalAmount(),
                            item.getSeparationAmount()
                    ));
                }
                typologiesAdapter.setItems(typologies);

                List<AdminProjectAmenityItem> amenities = new ArrayList<>();
                for (AdminProjectFormAmenityItem item : assets.amenities) {
                    amenities.add(new AdminProjectAmenityItem(item.getTitle(), item.getIconRes()));
                }
                amenitiesAdapter.setItems(amenities);
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                if (primaryImageUrl != null && !primaryImageUrl.isEmpty()) {
                    galleryAdapter.setItems(java.util.Collections.singletonList(
                            new AdminProjectGalleryItem(0, primaryImageUrl)
                    ));
                }
            }
        });
    }

    private void downloadQr() {
        if (qrBitmap == null || projectId.isEmpty()) {
            Toast.makeText(this, "Espera a que termine de cargar el proyecto.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            QrCodeStorage.saveToPictures(this, qrBitmap, projectId);
            Toast.makeText(this, "QR guardado en Imágenes/ProyectoIoT", Toast.LENGTH_LONG).show();
        } catch (Exception error) {
            Toast.makeText(this, "No se pudo guardar el QR: " + error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (mapPreview != null) mapPreview.release();
        super.onDestroy();
    }
}
