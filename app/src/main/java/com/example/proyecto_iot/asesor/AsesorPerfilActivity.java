package com.example.proyecto_iot.asesor;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.LoginActivity;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.entity.Proyecto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AsesorPerfilActivity extends BaseAsesorActivity {

    private TextView txtNombreAsesor, txtInmobiliariaAsesor, txtRatingAsesor, txtClientesAtendidos, txtResenasCount;
    private ImageView imgAvatarAsesor;
    private RecyclerView rvProyectosAsignados;
    private ProyectoAsignadoAdapter adapter;
    private List<Proyecto> proyectosList = new ArrayList<>();
    private AuthSessionManager sessionManager;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_perfil);
        setupBottomNavigation(R.id.navPerfil);

        sessionManager = AuthSessionManager.getInstance(this);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Referencias
        txtNombreAsesor = findViewById(R.id.txtNombreAsesor);
        txtInmobiliariaAsesor = findViewById(R.id.txtInmobiliariaAsesor);
        txtRatingAsesor = findViewById(R.id.txtRatingAsesor);
        txtClientesAtendidos = findViewById(R.id.txtClientesAtendidos);
        imgAvatarAsesor = findViewById(R.id.imgAvatarAsesor);

        rvProyectosAsignados = findViewById(R.id.rvProyectosAsignados);
        rvProyectosAsignados.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProyectoAsignadoAdapter(this, proyectosList);
        rvProyectosAsignados.setAdapter(adapter);

        // Cargar datos del usuario desde Firestore
        loadUserProfile();

        // Configurar botones y opciones
        findViewById(R.id.btnEditarPerfilAsesor).setOnClickListener(v -> {
            startActivity(new Intent(this, AsesorEditarPerfilActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        findViewById(R.id.btnCerrarSesionAsesor).setOnClickListener(v -> {
            sessionManager.logout();
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });

        findViewById(R.id.opcionNotificaciones).setOnClickListener(v ->
                Toast.makeText(this, "Notificaciones (por implementar)", Toast.LENGTH_SHORT).show());

        findViewById(R.id.opcionIdioma).setOnClickListener(v ->
                Toast.makeText(this, "Idioma (por implementar)", Toast.LENGTH_SHORT).show());

        findViewById(R.id.opcionCambiarContrasena).setOnClickListener(v -> {
            showChangePasswordDialog();
        });
    }

    private void loadUserProfile() {
        String uid = sessionManager.getUid();
        if (uid.isEmpty()) return;

        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && !isFinishing()) {
                        // Datos personales
                        String nombres = doc.getString("nombres");
                        String apellidos = doc.getString("apellidos");
                        String fullName = (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");
                        txtNombreAsesor.setText(fullName.trim().isEmpty() ? "Asesor" : fullName.trim());

                        // Avatar
                        String avatarKey = doc.getString("avatarKey");
                        if (avatarKey != null) {
                            int resId = getResources().getIdentifier(avatarKey, "drawable", getPackageName());
                            if (resId != 0) imgAvatarAsesor.setImageResource(resId);
                        }

                        // Rating
                        String ratingStr = doc.getString("rating");
                        txtRatingAsesor.setText((ratingStr != null && !ratingStr.isEmpty()) ? ratingStr : "5.0");

                        // Inmobiliaria
                        String inmobiliariaId = doc.getString("inmobiliariaId");
                        if (inmobiliariaId != null && !inmobiliariaId.isEmpty()) {
                            loadInmobiliariaName(inmobiliariaId);
                        } else {
                            txtInmobiliariaAsesor.setText("Independiente");
                        }

                        // Proyectos asignados
                        List<String> proyectosIds = (List<String>) doc.get("proyectos_asignados");
                        if (proyectosIds != null && !proyectosIds.isEmpty()) {
                            loadProyectos(proyectosIds);
                        }

                        // Métricas: Cierres (Citas con hasCierre true)
                        countCierres(uid);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show());
    }

    private void loadInmobiliariaName(String inmobiliariaId) {
        // Probamos con minúscula ya que es el estándar del repositorio
        db.collection("inmobiliarias").document(inmobiliariaId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && !isFinishing()) {
                        String nombre = doc.getString("nombre");
                        txtInmobiliariaAsesor.setText(nombre != null ? nombre : "The Editorial Estate");
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback a mayúscula si falla o intentar cargar por defecto
                    db.collection("empresas").document(inmobiliariaId).get()
                            .addOnSuccessListener(doc2 -> {
                                if (doc2.exists() && !isFinishing()) {
                                    txtInmobiliariaAsesor.setText(doc2.getString("nombre"));
                                }
                            });
                });
    }

    private void loadProyectos(List<String> proyectosIds) {
        db.collection("proyectos")
                .whereIn(FieldPath.documentId(), proyectosIds)
                .get()
                .addOnSuccessListener(query -> {
                    if (!isFinishing()) {
                        proyectosList.clear();
                        for (DocumentSnapshot doc : query) {
                            Proyecto proyecto = doc.toObject(Proyecto.class);
                            if (proyecto != null) {
                                proyecto.setId(doc.getId());
                                proyectosList.add(proyecto);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void countCierres(String asesorId) {
        db.collection("citas")
                .whereEqualTo("asesorId", asesorId)
                .whereEqualTo("hasCierre", true)
                .get()
                .addOnSuccessListener(query -> {
                    if (!isFinishing()) {
                        txtClientesAtendidos.setText(String.valueOf(query.size()));
                    }
                });
    }

    private void showChangePasswordDialog() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seguridad");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cambiar_contrasena, null);
        EditText etNewPassword = view.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        builder.setView(view);

        builder.setPositiveButton("Actualizar", (dialog, which) -> {
            String newPass = etNewPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            if (newPass.length() < 6) {
                Toast.makeText(this, "Contraseña muy corta", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "No coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            user.updatePassword(newPass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Actualizada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
}
