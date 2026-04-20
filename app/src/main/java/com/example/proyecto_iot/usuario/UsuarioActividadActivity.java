package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.proyecto_iot.R;

public class UsuarioActividadActivity extends BaseUsuarioActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_actividad);
        setupUserBottomNav(R.id.navUserActivity);
        setupSeparationDetails();
    }

    private void setupSeparationDetails() {
        View cardOne = findViewById(R.id.activitySeparationCardOne);
        View cardTwo = findViewById(R.id.activitySeparationCardTwo);
        View detailsOne = findViewById(R.id.btnActivityViewDetailOne);

        View.OnClickListener openFirst = v -> openTramiteDetail(
                getString(R.string.activity_sep_1_title),
                getString(R.string.activity_sep_1_id),
                getString(R.string.activity_sep_1_state),
                getString(R.string.activity_sep_1_note),
                getString(R.string.activity_sep_1_due),
                true
        );

        View.OnClickListener openSecond = v -> openTramiteDetail(
                getString(R.string.activity_sep_2_title),
                getString(R.string.activity_sep_2_id),
                getString(R.string.activity_sep_2_state),
                getString(R.string.activity_sep_2_note),
                getString(R.string.activity_tramite_secondary_due),
                false
        );

        if (cardOne != null) {
            cardOne.setOnClickListener(openFirst);
        }
        if (detailsOne != null) {
            detailsOne.setOnClickListener(openFirst);
        }
        if (cardTwo != null) {
            cardTwo.setOnClickListener(openSecond);
        }
    }

    private void openTramiteDetail(
            String title,
            String id,
            String status,
            String note,
            String due,
            boolean canPay
    ) {
        Intent intent = new Intent(this, UsuarioTramiteDetalleActivity.class);
        intent.putExtra(UsuarioTramiteDetalleActivity.EXTRA_TRAMITE_TITLE, title);
        intent.putExtra(UsuarioTramiteDetalleActivity.EXTRA_TRAMITE_ID, id);
        intent.putExtra(UsuarioTramiteDetalleActivity.EXTRA_TRAMITE_STATUS, status);
        intent.putExtra(UsuarioTramiteDetalleActivity.EXTRA_TRAMITE_NOTE, note);
        intent.putExtra(UsuarioTramiteDetalleActivity.EXTRA_TRAMITE_DUE, due);
        intent.putExtra(UsuarioTramiteDetalleActivity.EXTRA_TRAMITE_CAN_PAY, canPay);
        startActivity(intent);
    }
}
