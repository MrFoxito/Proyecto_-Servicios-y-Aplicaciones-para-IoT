package com.example.proyecto_iot.admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.adapter.AdminProjectAmenitiesAdapter;
import com.example.proyecto_iot.admin.adapter.AdminProjectGalleryAdapter;
import com.example.proyecto_iot.admin.adapter.AdminProjectTypologiesAdapter;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.data.ProjectBusinessRules;
import com.example.proyecto_iot.data.QrCodeGenerator;
import com.example.proyecto_iot.databinding.ActivityAdminDetalleProyectoBinding;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDetalleProyectoBinding.inflate(getLayoutInflater());
        setContentView(binding);
        projectId = valueOr(getIntent().getStringExtra("project_id"));
        projectTitle = getIntent().getStringExtra("project_title");
        if (projectTitle == null) {
            projectTitle = "";
        }

        setupBackButton();
        setupLists();
        loadProjectDetail();
        binding.btnEditarProyecto.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminEditarProyectoActivity.class);
            intent.putExtra("project_title", projectTitle);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void setupLists() {
        LocalSchemaStorage storage = new LocalSchemaStorage(this);

        galleryAdapter = new AdminProjectGalleryAdapter();
        binding.rvProjectGallery.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvProjectGallery.setAdapter(galleryAdapter);
        galleryAdapter.setItems(storage.getAdminProjectGallery());

        typologiesAdapter = new AdminProjectTypologiesAdapter();
        binding.rvProjectTypologies.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProjectTypologies.setAdapter(typologiesAdapter);
        typologiesAdapter.setItems(storage.getAdminProjectTypologies());

        amenitiesAdapter = new AdminProjectAmenitiesAdapter();
        binding.rvProjectAmenities.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvProjectAmenities.setAdapter(amenitiesAdapter);
        amenitiesAdapter.setItems(storage.getAdminProjectAmenities());
    }

    private void loadProjectDetail() {
        String lookup = projectId.isEmpty() ? projectTitle : projectId;
        if (lookup.isEmpty()) {
            return;
        }
        new FirebaseDataRepository().readProjectDetail(lookup, new FirebaseDataRepository.ProjectDetailCallback() {
            @Override
            public void onSuccess(FirebaseDataRepository.ProjectDetail detail) {
                projectId = detail.projectId;
                projectTitle = detail.nombre;
                binding.tvDetalleProjectName.setText(detail.nombre);
                binding.tvDetalleProjectDescription.setText(detail.descripcion);
                binding.tvDetalleProjectAddress.setText(detail.direccion + " - " + detail.distrito);
                binding.tvDetalleDeliveryDate.setText(detail.fechaEntrega.isEmpty() ? "Pendiente" : detail.fechaEntrega);
                binding.tvDetalleQrValue.setText(detail.qrValue);
                bindStatus(detail.estadoProyecto);
                Bitmap qrBitmap = QrCodeGenerator.create(detail.qrValue, 256);
                if (qrBitmap != null) {
                    binding.imgDetalleQrProyecto.setImageBitmap(qrBitmap);
                }
            }

            @Override
            public void onError(String message) {
                bindStatus("");
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

    private String valueOr(String value) {
        return value == null ? "" : value.trim();
    }
}
