package com.example.proyecto_iot.admin;

import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.adapter.AdminProjectAmenitiesAdapter;
import com.example.proyecto_iot.admin.adapter.AdminProjectGalleryAdapter;
import com.example.proyecto_iot.admin.adapter.AdminProjectTypologiesAdapter;
import com.example.proyecto_iot.admin.model.AdminProjectAmenityItem;
import com.example.proyecto_iot.admin.model.AdminProjectGalleryItem;
import com.example.proyecto_iot.admin.model.AdminProjectTypologyItem;
import com.example.proyecto_iot.databinding.ActivityAdminDetalleProyectoBinding;

import java.util.Arrays;

/**
 * Vista de detalle de un proyecto inmobiliario.
 */
public class AdminDetalleProyectoActivity extends BaseAdminActivity {

    private ActivityAdminDetalleProyectoBinding binding;
    private AdminProjectGalleryAdapter galleryAdapter;
    private AdminProjectTypologiesAdapter typologiesAdapter;
    private AdminProjectAmenitiesAdapter amenitiesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDetalleProyectoBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBackButton();
        setupLists();
        binding.btnEditarProyecto.setOnClickListener(v -> openScreen(AdminEditarProyectoActivity.class));
    }

    private void setupLists() {
        galleryAdapter = new AdminProjectGalleryAdapter();
        binding.rvProjectGallery.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvProjectGallery.setAdapter(galleryAdapter);
        galleryAdapter.setItems(Arrays.asList(
                new AdminProjectGalleryItem(R.drawable.sa_profile_admin),
                new AdminProjectGalleryItem(R.drawable.sa_profile_admin),
                new AdminProjectGalleryItem(R.drawable.sa_profile_admin),
                new AdminProjectGalleryItem(R.drawable.sa_profile_admin),
                new AdminProjectGalleryItem(R.drawable.sa_profile_admin),
                new AdminProjectGalleryItem(R.drawable.sa_profile_admin)
        ));

        typologiesAdapter = new AdminProjectTypologiesAdapter();
        binding.rvProjectTypologies.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProjectTypologies.setAdapter(typologiesAdapter);
        typologiesAdapter.setItems(Arrays.asList(
                new AdminProjectTypologyItem("Tipo A", "DISPONIBLE", true, "70 m2", "2 habs", "350,000 USD", "1,400 USD"),
                new AdminProjectTypologyItem("Tipo B", "DISPONIBLE", true, "80 m2", "3 habs", "310,000 USD", "1,400 USD"),
                new AdminProjectTypologyItem("Tipo C", "NO DISPONIBLE", false, "60 m2", "1 hab", "280,000 USD", "1,200 USD"),
                new AdminProjectTypologyItem("Tipo D", "DISPONIBLE", true, "95 m2", "3 habs", "410,000 USD", "1,800 USD"),
                new AdminProjectTypologyItem("Penthouse", "NO DISPONIBLE", false, "140 m2", "4 habs", "590,000 USD", "2,500 USD")
        ));

        amenitiesAdapter = new AdminProjectAmenitiesAdapter();
        binding.rvProjectAmenities.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvProjectAmenities.setAdapter(amenitiesAdapter);
        amenitiesAdapter.setItems(Arrays.asList(
                new AdminProjectAmenityItem("Coworking", R.drawable.ic_admin_laptop),
                new AdminProjectAmenityItem("Piscina", R.drawable.ic_admin_pool),
                new AdminProjectAmenityItem("Terraza", R.drawable.ic_home),
                new AdminProjectAmenityItem("Sala lounge", R.drawable.ic_email),
                new AdminProjectAmenityItem("Gym", R.drawable.ic_admin_laptop),
                new AdminProjectAmenityItem("Lobby doble altura", R.drawable.ic_home),
                new AdminProjectAmenityItem("Zona BBQ", R.drawable.ic_email),
                new AdminProjectAmenityItem("Pet zone", R.drawable.ic_admin_pool)
        ));
    }
}
