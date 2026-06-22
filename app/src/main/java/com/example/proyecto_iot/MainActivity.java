package com.example.proyecto_iot;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializa los canales de notificación (requerido desde Android 8.0)
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
