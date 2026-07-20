package com.example.proyecto_iot.asesor;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.ProjectImageLoader;
import com.example.proyecto_iot.entity.Proyecto;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.FirebaseFirestore;

/** A read-only project sheet for advisors. It intentionally contains no map or edit actions. */
public class AsesorProyectoDetalleActivity extends BaseAsesorActivity {
    public static final String EXTRA_PROJECT_ID = "extra_project_id";
    private ListenerRegistration projectListener;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_proyecto_detalle);
        setupBackButton();
        String projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        if (projectId == null || projectId.trim().isEmpty()) {
            Toast.makeText(this, "Proyecto no válido.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        listenProject(projectId.trim());
    }

    private void listenProject(String projectId) {
        projectListener = FirebaseFirestore.getInstance().collection("proyectos").document(projectId)
                .addSnapshotListener((document, error) -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (error != null || document == null || !document.exists()) {
                        findViewById(R.id.txtProyectoDetalleError).setVisibility(View.VISIBLE);
                        return;
                    }
                    Proyecto project = document.toObject(Proyecto.class);
                    if (project == null) return;
                    project.setId(document.getId());
                    setText(R.id.txtProyectoDetalleNombre, value(project.getNombre(), "Proyecto"));
                    setText(R.id.txtProyectoDetalleEstado, value(project.getEstado(), "Estado no disponible"));
                    setText(R.id.txtProyectoDetalleDireccion, value(project.getDireccion(), "Dirección no disponible"));
                    setText(R.id.txtProyectoDetalleDistrito, value(project.getDistrito(), "Distrito no disponible"));
                    setText(R.id.txtProyectoDetallePrecio, value(project.getPrecioDesde(), "Precio no disponible"));
                    setText(R.id.txtProyectoDetalleDescripcion, value(project.getDescripcion(), "Sin descripción disponible."));
                    if (project.getImageUrl() != null && !project.getImageUrl().trim().isEmpty()) {
                        ProjectImageLoader.load(findViewById(R.id.imgProyectoDetalle), project.getImageUrl(), R.drawable.as_property_01);
                    }
                    findViewById(R.id.txtProyectoDetalleError).setVisibility(View.GONE);
                });
    }

    private void setText(int id, String text) { ((TextView) findViewById(id)).setText(text); }
    private String value(String value, String fallback) { return value == null || value.trim().isEmpty() ? fallback : value.trim(); }
    @Override protected void onDestroy() { if (projectListener != null) projectListener.remove(); super.onDestroy(); }
}
