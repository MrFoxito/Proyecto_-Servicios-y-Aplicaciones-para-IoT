package com.example.proyecto_iot.asesor;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.LoginActivity;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class AsesorPerfilActivity extends BaseAsesorActivity {

    private TextView txtNombre, txtBio, txtRating, txtClientesCount, txtRetencion, txtRolLabel;
    private TextView txtProyectoNombre, txtProyectoUbicacion, txtProyectoDesc, txtProyectoProgreso, txtProyectoLanzamiento;
    private ImageView imgPerfil, imgProyecto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_perfil);

        setupBottomNavigation(R.id.navPerfil);
        initViews();
        loadAdvisorData();

        findViewById(R.id.btnEditarPerfilAsesor).setOnClickListener(v -> openScreen(AsesorEditarPerfilActivity.class));
        findViewById(R.id.btnSolicitarUnionProyecto).setOnClickListener(v -> solicitarUnionProyecto());
        findViewById(R.id.btnCerrarSesionAsesor).setOnClickListener(v -> {
            new AuthSessionManager(this).logout();
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void initViews() {
        txtNombre = findViewById(R.id.txtPerfilNombre);
        txtBio = findViewById(R.id.txtPerfilBio);
        txtRating = findViewById(R.id.txtPerfilRating);
        txtClientesCount = findViewById(R.id.txtPerfilClientesCount);
        txtRetencion = findViewById(R.id.txtPerfilRetencion);
        txtRolLabel = findViewById(R.id.txtPerfilRolLabel);
        
        txtProyectoNombre = findViewById(R.id.txtPerfilProyectoNombre);
        txtProyectoUbicacion = findViewById(R.id.txtAsignacionUbicacion);
        txtProyectoDesc = findViewById(R.id.txtAsignacionDesc);
        txtProyectoProgreso = findViewById(R.id.txtAsignacionProgreso);
        txtProyectoLanzamiento = findViewById(R.id.txtAsignacionLanzamiento);
        
        imgPerfil = findViewById(R.id.imgPerfilAsesor);
        imgProyecto = findViewById(R.id.imgAsignacionImg);
    }

    private void loadAdvisorData() {
        AuthSessionManager session = new AuthSessionManager(this);
        LocalSchemaStorage storage = new LocalSchemaStorage(this);
        
        JSONObject user = storage.getUserById(session.getUserId());
        if (user != null) {
            String fullName = user.optString("nombres") + " " + user.optString("apellidos");
            txtNombre.setText(fullName);
            txtRating.setText(user.optString("rating", "4.8"));
            
            String bio = user.optString("bio", "Curando experiencias residenciales de lujo por mas de una decada.");
            txtBio.setText(bio);
            
            String cargo = user.optString("cargo", "ASESOR SENIOR DE BOUTIQUE");
            txtRolLabel.setText(cargo.toUpperCase(Locale.ROOT));

            // Cargar datos de asignación
            JSONArray proyectos = user.optJSONArray("proyectosAsignados");
            if (proyectos != null && proyectos.length() > 0) {
                String firstProjectName = proyectos.optString(0);
                loadProjectDetails(firstProjectName);
            } else {
                txtProyectoNombre.setText("Sin proyecto asignado");
                txtProyectoUbicacion.setText("-");
                txtProyectoDesc.setText("Actualmente no tienes proyectos asignados. Solicita unirte a uno.");
                txtProyectoProgreso.setText("0 / 0");
                txtProyectoLanzamiento.setText("-");
            }
        }
    }

    private void loadProjectDetails(String projectName) {
        LocalSchemaStorage storage = new LocalSchemaStorage(this);
        var draft = storage.getAdminProjectDraftForEdit(projectName);
        if (draft != null) {
            txtProyectoNombre.setText(draft.getProjectName());
            txtProyectoUbicacion.setText(draft.getAddress().toUpperCase(Locale.ROOT));
            txtProyectoDesc.setText(draft.getDescription());
            txtProyectoLanzamiento.setText(draft.getDeliveryDate());
            txtProyectoProgreso.setText("04 / 12"); // Placeholder para progreso real
        }
    }

    private void solicitarUnionProyecto() {
        AuthSessionManager session = new AuthSessionManager(this);
        String advisorName = session.getUserName();
        String advisorEmail = session.getUserEmail();
        
        boolean created = new LocalSchemaStorage(this).addAdvisorProjectJoinRequest(
                advisorName,
                advisorEmail,
                "The Iron Works"
        );
        Toast.makeText(
                this,
                created ? "Solicitud enviada al administrador" : "Ya existe una solicitud pendiente",
                Toast.LENGTH_SHORT
        ).show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadAdvisorData();
    }
}
