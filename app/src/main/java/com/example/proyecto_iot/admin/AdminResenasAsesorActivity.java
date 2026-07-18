package com.example.proyecto_iot.admin;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.admin.adapter.AdminReviewsAdapter;
import com.example.proyecto_iot.admin.model.AdminReviewItem;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.databinding.ActivityAdminResenasAsesorBinding;

import java.util.List;

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
        loadReviews();
    }

    private void setupRecycler() {
        adapter = new AdminReviewsAdapter();
        binding.rvReviews.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReviews.setAdapter(adapter);
    }

    private void loadReviews() {
        String advisorId = getIntent().getStringExtra(AdminAsignarProyectoAsesorActivity.EXTRA_ADVISOR_ID);
        new FirebaseDataRepository().readAdvisorReviews(advisorId, new FirebaseDataRepository.AdminReviewsCallback() {
            @Override
            public void onSuccess(List<AdminReviewItem> reviews) {
                adapter.setItems(reviews);
            }

            @Override
            public void onError(String message) {
                adapter.setItems(new LocalSchemaStorage(AdminResenasAsesorActivity.this).getAdminReviews());
            }
        });
    }
}
