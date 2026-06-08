package com.example.proyecto_iot.admin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.admin.adapter.AdminProjectAmenitiesAdapter;
import com.example.proyecto_iot.admin.adapter.AdminProjectGalleryAdapter;
import com.example.proyecto_iot.admin.adapter.AdminProjectTypologiesAdapter;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.data.LocalSchemaStorage;
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
        projectId = getIntent().getStringExtra("project_id");
        if (projectId == null) {
            projectId = "";
        }
        projectTitle = getIntent().getStringExtra("project_title");
        if (projectTitle == null) {
            projectTitle = "";
        }

        setupBackButton();
        setupLists();
        loadProjectDetailFromFirestore();
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

    private void loadProjectDetailFromFirestore() {
        new FirebaseDataRepository().readAdminProjectDetail(projectId, projectTitle, new FirebaseDataRepository.AdminProjectDetailCallback() {
            @Override
            public void onSuccess(FirebaseDataRepository.AdminProjectDetail detail) {
                projectId = detail.projectId;
                projectTitle = detail.title;
                binding.tvProjectDetailName.setText(detail.title);
                binding.tvProjectDetailDescription.setText(detail.description);
                binding.tvProjectDetailLocation.setText(detail.location);
                binding.tvProjectDetailDeliveryDate.setText(detail.deliveryDate);
                applyStatus(detail.status);
                if (!detail.gallery.isEmpty()) {
                    galleryAdapter.setItems(detail.gallery);
                }
                if (!detail.typologies.isEmpty()) {
                    typologiesAdapter.setItems(detail.typologies);
                }
                if (!detail.amenities.isEmpty()) {
                    amenitiesAdapter.setItems(detail.amenities);
                }
            }

            @Override
            public void onError(String message) {
                if (!projectTitle.trim().isEmpty()) {
                    binding.tvProjectDetailName.setText(projectTitle);
                }
                Toast.makeText(AdminDetalleProyectoActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void applyStatus(String status) {
        resetStatus(binding.tvStatusPlanosDetalle);
        resetStatus(binding.tvStatusPreventaDetalle);
        resetStatus(binding.tvStatusVentaDetalle);
        String normalized = status == null ? "" : status.toUpperCase(java.util.Locale.ROOT);
        if (normalized.contains("VENTA") && !normalized.contains("PREVENTA")) {
            selectStatus(binding.tvStatusVentaDetalle);
        } else if (normalized.contains("PREVENTA")) {
            selectStatus(binding.tvStatusPreventaDetalle);
        } else {
            selectStatus(binding.tvStatusPlanosDetalle);
        }
    }

    private void resetStatus(TextView view) {
        view.setBackgroundResource(android.R.color.transparent);
        view.setTextColor(Color.parseColor("#464D53"));
    }

    private void selectStatus(TextView view) {
        view.setBackgroundResource(com.example.proyecto_iot.R.drawable.admin_detail_dark_pill);
        view.setTextColor(Color.WHITE);
    }
}
