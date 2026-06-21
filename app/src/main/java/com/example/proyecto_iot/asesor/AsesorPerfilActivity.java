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
import com.example.proyecto_iot.adapters.ProyectoAsignadoAdapter;
import com.example.proyecto_iot.entity.Proyecto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AsesorPerfilActivity extends BaseAsesorActivity {

    private TextView txtNombreAsesor, txtInmobiliariaAsesor, txtRatingAsesor, txtClientesAtendidos;
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
        // Pasar contexto al adaptador
        adapter = new ProyectoAsignadoAdapter(this, proyectosList);
        rvProyectosAsignados.setAdapter(adapter);

        // Cargar datos del usuario desde Firestore
        loadUserProfile();

        // Configurar botones y opciones
        findViewById(R.id.btnEditarPerfilAsesor).setOnClickListener(v -> {
            // Abrir actividad de editar perfil (lo harás después)
            Toast.makeText(this, "Editar perfil (por implementar)", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnCerrarSesionAsesor).setOnClickListener(v -> {
            sessionManager.logout();
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });

        // Opciones de configuración
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
        if (uid.isEmpty()) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
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

                        // Rating (con estrella)
                        String ratingStr = doc.getString("rating");
                        if (ratingStr != null && !ratingStr.isEmpty() && !ratingStr.equals("0")) {
                            txtRatingAsesor.setText(ratingStr + " ★");
                        } else {
                            txtRatingAsesor.setText("Sin calificaciones");
                        }

                        // Obtener inmobiliariaId y luego el nombre desde colección Inmobiliarias
                        String inmobiliariaId = doc.getString("inmobiliariaId");
                        if (inmobiliariaId != null && !inmobiliariaId.isEmpty()) {
                            loadInmobiliariaName(inmobiliariaId);
                        } else {
                            txtInmobiliariaAsesor.setText("Sin inmobiliaria asignada");
                        }

                        // Obtener proyectos asignados (suponiendo un array de IDs)
                        List<String> proyectosIds = (List<String>) doc.get("proyectos_asignados");
                        if (proyectosIds != null && !proyectosIds.isEmpty()) {
                            loadProyectos(proyectosIds);
                        } else {
                            proyectosList.clear();
                            adapter.notifyDataSetChanged();
                        }

                        // Contar clientes atendidos (ej: desde separaciones donde asesorId = uid)
                        countClientesAtendidos(uid);
                    } else {
                        Toast.makeText(this, "Perfil no encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadInmobiliariaName(String inmobiliariaId) {
        db.collection("Inmobiliarias").document(inmobiliariaId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String nombre = doc.getString("nombre");
                        txtInmobiliariaAsesor.setText(nombre != null ? nombre : "Inmobiliaria");
                    } else {
                        txtInmobiliariaAsesor.setText("Inmobiliaria no encontrada");
                    }
                })
                .addOnFailureListener(e -> txtInmobiliariaAsesor.setText("Error al cargar inmobiliaria"));
    }

    private void loadProyectos(List<String> proyectosIds) {
        // Usamos whereIn con el campo "id" (que es el ID del documento)
        db.collection("Proyectos")
                .whereIn("id", proyectosIds)
                .get()
                .addOnSuccessListener(query -> {
                    proyectosList.clear();
                    for (DocumentSnapshot doc : query) {
                        Proyecto proyecto = doc.toObject(Proyecto.class);
                        if (proyecto != null) {
                            proyecto.setId(doc.getId());
                            proyectosList.add(proyecto);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar proyectos", Toast.LENGTH_SHORT).show());
    }

    private void countClientesAtendidos(String asesorId) {
        db.collection("Separaciones")
                .whereEqualTo("asesorId", asesorId)
                .get()
                .addOnSuccessListener(query -> {
                    int count = query.size();
                    txtClientesAtendidos.setText(String.valueOf(count));
                })
                .addOnFailureListener(e -> txtClientesAtendidos.setText("0"));
    }

    /**
     * Diálogo para cambiar la contraseña del usuario autenticado.
     */
    private void showChangePasswordDialog() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cambiar contraseña");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cambiar_contrasena, null);
        EditText etNewPassword = view.findViewById(R.id.etNewPassword);
        EditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        builder.setView(view);

        builder.setPositiveButton("Actualizar", (dialog, which) -> {
            String newPass = etNewPassword.getText().toString().trim();
            String confirmPass = etConfirmPassword.getText().toString().trim();

            if (newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Completa ambos campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPass.length() < 6) {
                Toast.makeText(this, "La contraseña debe tener mínimo 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            user.updatePassword(newPass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Error al actualizar";
                            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                        }
                    });
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
}