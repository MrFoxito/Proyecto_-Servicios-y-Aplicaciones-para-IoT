package com.example.proyecto_iot.admin;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.admin.adapter.AdminReviewsAdapter;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.databinding.ActivityAdminResenasAsesorBinding;

/**
 * Vista de resenas y comentarios de clientes sobre un asesor.
 */
public class AdminResenasAsesorActivity extends BaseAdminActivity {

    private ActivityAdminResenasAsesorBinding binding;
    private AdminReviewsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminResenasAsesorBinding.inflate(getLayoutInflater());
        setContentView(binding);

        setupBackButton();
        setupRecycler();
    }

    private void setupRecycler() {
        adapter = new AdminReviewsAdapter();
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReviews.setAdapter(adapter);
        adapter.setItems(new LocalSchemaStorage(this).getAdminReviews());
    }
}
