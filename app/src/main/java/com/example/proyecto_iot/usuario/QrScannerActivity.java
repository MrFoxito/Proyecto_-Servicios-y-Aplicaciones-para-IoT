package com.example.proyecto_iot.usuario;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

public class QrScannerActivity extends AppCompatActivity {
    private boolean resolvingProject;

    private final ActivityResultLauncher<ScanOptions> scannerLauncher = registerForActivityResult(
            new ScanContract(),
            this::handleScanResult
    );

    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) {
                    launchScanner();
                } else {
                    Toast.makeText(this,
                            "Se necesita permiso de camara para escanear codigos QR.",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestCameraAndLaunch();
    }

    private void requestCameraAndLaunch() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(this,
                    "Este dispositivo no tiene una camara disponible para escanear QR.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchScanner();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchScanner() {
        ScanOptions options = new ScanOptions()
                .setPrompt("Escanea el codigo QR de un proyecto")
                .setBeepEnabled(true)
                .setOrientationLocked(false)
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        scannerLauncher.launch(options);
    }

    private void handleScanResult(ScanIntentResult result) {
        String contents = result == null ? null : result.getContents();
        if (contents == null) {
            // El usuario cancelo: se cierra solo esta actividad y se vuelve a Explorar.
            finish();
            return;
        }

        String projectId = ProjectQrParser.extractProjectId(contents);
        if (projectId.isEmpty()) {
            Toast.makeText(this,
                    "El codigo QR no pertenece a un proyecto valido.",
                    Toast.LENGTH_LONG).show();
            retryScanning();
            return;
        }

        if (resolvingProject) {
            return;
        }
        resolvingProject = true;
        new FirebaseDataRepository().readProjectDetailById(projectId, new FirebaseDataRepository.ProjectDetailCallback() {
            @Override
            public void onSuccess(FirebaseDataRepository.ProjectDetail detail) {
                if (isFinishing()) {
                    return;
                }
                startActivity(UsuarioPropiedadDetalleActivity.newIntent(QrScannerActivity.this, projectId));
                finish();
            }

            @Override
            public void onError(String message) {
                resolvingProject = false;
                Toast.makeText(QrScannerActivity.this,
                        "No se encontro el proyecto o no hay conexion: " + message,
                        Toast.LENGTH_LONG).show();
                retryScanning();
            }
        });
    }

    private void retryScanning() {
        if (!isFinishing() && !isDestroyed()) {
            requestCameraAndLaunch();
        }
    }
}
