package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

public class QrScannerActivity extends AppCompatActivity {
    private final ActivityResultLauncher<ScanOptions> scannerLauncher = registerForActivityResult(
            new ScanContract(),
            this::handleScanResult
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launchScanner();
    }

    private void launchScanner() {
        ScanOptions options = new ScanOptions()
                .setPrompt("Escanea el código QR de un proyecto")
                .setBeepEnabled(true)
                .setOrientationLocked(false)
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        scannerLauncher.launch(options);
    }

    private void handleScanResult(ScanIntentResult result) {
        String contents = result == null ? null : result.getContents();
        if (contents == null) {
            finish();
            return;
        }
        Uri uri = Uri.parse(contents.trim());
        if (!"app".equalsIgnoreCase(uri.getScheme())
                || !"proyecto".equalsIgnoreCase(uri.getHost())
                || uri.getLastPathSegment() == null
                || uri.getLastPathSegment().trim().isEmpty()) {
            Toast.makeText(this, "El código QR no pertenece a un proyecto válido.", Toast.LENGTH_LONG).show();
            launchScanner();
            return;
        }
        String projectId = uri.getLastPathSegment().trim();
        new FirebaseDataRepository().readProjectDetail(projectId, new FirebaseDataRepository.ProjectDetailCallback() {
            @Override
            public void onSuccess(FirebaseDataRepository.ProjectDetail detail) {
                Intent intent = new Intent(QrScannerActivity.this, UsuarioPropiedadDetalleActivity.class);
                intent.setData(uri);
                intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_ID, projectId);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(QrScannerActivity.this,
                        "No se encontró el proyecto o no hay conexión: " + message,
                        Toast.LENGTH_LONG).show();
                launchScanner();
            }
        });
    }
}
