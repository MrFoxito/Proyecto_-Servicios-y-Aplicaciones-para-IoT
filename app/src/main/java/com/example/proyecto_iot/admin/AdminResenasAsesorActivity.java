package com.example.proyecto_iot.admin;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.admin.adapter.AdminReviewsAdapter;
import com.example.proyecto_iot.admin.model.AdminReviewItem;
import com.example.proyecto_iot.databinding.ActivityAdminResenasAsesorBinding;

import java.util.Arrays;

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
        adapter.setItems(Arrays.asList(
                new AdminReviewItem(
                        "Carlos Ruiz",
                        "24 Oct 2024",
                        "Residencial Sky Garden",
                        "Excelente atencion y asesoria profesional. El proyecto supero mis expectativas y el seguimiento fue muy claro durante todo el proceso.",
                        "5.0 / 5",
                        R.drawable.sa_profile_user_1
                ),
                new AdminReviewItem(
                        "Elena Martinez",
                        "12 Oct 2024",
                        "Ocean Breeze Tower",
                        "Una experiencia de compra impecable. El equipo siempre estuvo disponible para resolver dudas sobre financiamiento y tiempos de entrega.",
                        "4.5 / 5",
                        R.drawable.sa_profile_user_2
                ),
                new AdminReviewItem(
                        "Javier Solis",
                        "05 Oct 2024",
                        "Residencial Sky Garden",
                        "La calidad de construccion y la claridad de la presentacion comercial fueron decisivas para cerrar la compra.",
                        "5.0 / 5",
                        R.drawable.sa_profile_user_1
                ),
                new AdminReviewItem(
                        "Lucia Ferrer",
                        "28 Sep 2024",
                        "Catalina Sky View",
                        "Muy buena gestion postventa y excelente manejo de expectativas desde la primera visita al proyecto.",
                        "4.8 / 5",
                        R.drawable.sa_profile_user_2
                ),
                new AdminReviewItem(
                        "Mariana Tello",
                        "21 Sep 2024",
                        "Paseo del Golf",
                        "El asesoramiento fue muy ordenado. Hubo seguimiento comercial, simulacion financiera y cierre con tiempos bastante claros.",
                        "4.9 / 5",
                        R.drawable.sa_profile_user_1
                ),
                new AdminReviewItem(
                        "Daniel Vega",
                        "14 Sep 2024",
                        "Bosque Real",
                        "Se nota experiencia en el manejo de clientes inversionistas. La presentacion del proyecto fue muy convincente y completa.",
                        "4.7 / 5",
                        R.drawable.sa_profile_user_2
                ),
                new AdminReviewItem(
                        "Sofia Navarro",
                        "03 Sep 2024",
                        "Residencial Nova",
                        "Buena comunicacion, respuestas rapidas y soporte luego de la separacion. La experiencia general fue muy positiva.",
                        "4.6 / 5",
                        R.drawable.sa_profile_user_1
                ),
                new AdminReviewItem(
                        "Renato Perez",
                        "29 Ago 2024",
                        "Catalina Sky View",
                        "Destaco la paciencia para explicar tipologias, beneficios de ubicacion y comparativos frente a otros proyectos cercanos.",
                        "5.0 / 5",
                        R.drawable.sa_profile_user_2
                ),
                new AdminReviewItem(
                        "Valeria Campos",
                        "18 Ago 2024",
                        "The Obsidian Estate",
                        "La experiencia comercial fue elegante y muy bien cuidada. Todo el proceso se sintio profesional de principio a fin.",
                        "4.8 / 5",
                        R.drawable.sa_profile_user_1
                )
        ));
    }
}
