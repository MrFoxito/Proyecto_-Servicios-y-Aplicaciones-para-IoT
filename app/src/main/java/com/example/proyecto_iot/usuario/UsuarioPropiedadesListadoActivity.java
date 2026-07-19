package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import java.util.ArrayList;
import java.util.List;

public class UsuarioPropiedadesListadoActivity extends BaseUsuarioActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_propiedades_listado);
        setupUserBottomNav(R.id.navUserExplore);
        setupPropertyList();
        setupActions();
    }

    private void setupPropertyList() {
        RecyclerView recyclerView = findViewById(R.id.recyclerPropertyList);
        if (recyclerView == null) {
            return;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        UsuarioPropertyListAdapter adapter = new UsuarioPropertyListAdapter(new ArrayList<>(), this::openPropertyDetail);
        recyclerView.setAdapter(adapter);
        new FirebaseDataRepository().readUserPropertyListItems(new FirebaseDataRepository.UserPropertyListCallback() {
            @Override
            public void onSuccess(List<UsuarioPropertyListItem> projects) {
                if (isFinishing() || isDestroyed()) return;
                adapter.setItems(projects);
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                adapter.setItems(new ArrayList<>());
            }
        });
    }

    private void setupActions() {
        View back = findViewById(R.id.btnBackPropertiesList);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        View openMap = findViewById(R.id.btnOpenMapFromList);
        if (openMap != null) {
            openMap.setOnClickListener(v ->
                    startActivity(new Intent(this, UsuarioMapaExploracionActivity.class)));
        }

        View scanQr = findViewById(R.id.btnScanProjectQr);
        if (scanQr != null) {
            scanQr.setOnClickListener(v ->
                    startActivity(new Intent(this, QrScannerActivity.class)));
        }
    }

    private void openPropertyDetail(UsuarioPropertyListItem item) {
        startActivity(UsuarioPropiedadDetalleActivity.newIntent(this, item));
    }
}
