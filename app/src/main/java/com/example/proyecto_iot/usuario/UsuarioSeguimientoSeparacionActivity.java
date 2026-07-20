package com.example.proyecto_iot.usuario;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseSeparationRepository;
import com.example.proyecto_iot.data.ProjectMediaRepository;
import com.example.proyecto_iot.data.SupabaseStorageRepository;
import com.example.proyecto_iot.data.TemporarySeparationPolicy;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Live digital ticket for a client-created temporary unit separation. */
public class UsuarioSeguimientoSeparacionActivity extends AppCompatActivity {
    public static final String EXTRA_SEPARATION_ID = "extra_temporary_separation_id";

    private final FirebaseSeparationRepository separationRepository = new FirebaseSeparationRepository();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ListenerRegistration separationListener;
    private FirebaseSeparationRepository.TemporarySeparation current;
    private boolean submitting;

    private final Runnable countdownTick = new Runnable() {
        @Override public void run() {
            renderCountdown();
            handler.postDelayed(this, 30_000L);
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_seguimiento_separacion);
        applyInsets();
        findViewById(R.id.btnTrackingBack).setOnClickListener(v -> finish());
        
        setupCopyButton(R.id.btnCopyBcp, R.id.tvAccountBcp);
        setupCopyButton(R.id.btnCopyInterbank, R.id.tvAccountInterbank);
        setupCopyButton(R.id.btnCopyCci, R.id.tvAccountCci);
        
        findViewById(R.id.btnTrackingCancel).setOnClickListener(v -> confirmCancellation());
        String separationId = value(getIntent() == null ? null : getIntent().getStringExtra(EXTRA_SEPARATION_ID));
        if (separationId.isEmpty()) {
            Toast.makeText(this, "No se pudo identificar la separación.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        separationListener = separationRepository.listenTemporarySeparation(separationId,
                new FirebaseSeparationRepository.TemporarySeparationListener() {
                    @Override public void onChanged(FirebaseSeparationRepository.TemporarySeparation separation) {
                        if (!active()) return;
                        current = separation;
                        render();
                    }
                    @Override public void onError(String message) {
                        if (active()) Toast.makeText(UsuarioSeguimientoSeparacionActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override protected void onStart() {
        super.onStart();
        handler.post(countdownTick);
    }

    @Override protected void onStop() {
        handler.removeCallbacks(countdownTick);
        super.onStop();
    }

    @Override protected void onDestroy() {
        if (separationListener != null) separationListener.remove();
        super.onDestroy();
    }

    private void render() {
        if (current == null) return;
        ((TextView) findViewById(R.id.tvTrackingStatus)).setText(
                TemporarySeparationPolicy.displayLabel(current.status));
        ((TextView) findViewById(R.id.tvTrackingProject)).setText(current.projectName);
        ((TextView) findViewById(R.id.tvTrackingUnit)).setText(current.unitName);
        ((TextView) findViewById(R.id.tvTrackingAmount)).setText(formatPen(current.separationAmount));
        ((TextView) findViewById(R.id.tvTrackingCode)).setText("Código de separación: " + current.id);
        ImageView image = findViewById(R.id.ivTrackingProject);
        if (!value(current.imageUrl).isEmpty()) Glide.with(this).load(current.imageUrl).centerCrop().into(image);
        else image.setImageResource(R.drawable.user_property_hero_real);
        renderCountdown();

        View cancel = findViewById(R.id.btnTrackingCancel);
        if (cancel != null) cancel.setVisibility(current.canCancel ? View.VISIBLE : View.GONE);
        cancel.setVisibility(current.canCancel ? View.VISIBLE : View.GONE);
    }

    private void renderCountdown() {
        TextView message = findViewById(R.id.tvTrackingExpiryMessage);
        TextView counter = findViewById(R.id.tvTrackingCountdown);
        if (current == null) {
            message.setText("");
            counter.setText("");
            return;
        }
        if (current.expiresAtMillis <= 0) {
            message.setText(TemporarySeparationPolicy.displayLabel(current.status));
            counter.setText("");
            return;
        }
        Date expiration = new Date(current.expiresAtMillis);
        String date = new SimpleDateFormat("d 'de' MMMM 'de' yyyy 'a las' h:mm", new Locale("es", "PE")).format(expiration);
        String marker = new SimpleDateFormat("a", Locale.US).format(expiration).equalsIgnoreCase("PM") ? "p. m." : "a. m.";
        message.setText("Tienes hasta el " + date + " " + marker + " para completar tu separación.");
        long remaining = current.expiresAtMillis - System.currentTimeMillis();
        if (remaining <= 0 && TemporarySeparationPolicy.PENDING_PAYMENT.equals(current.status)) {
            counter.setText("El vencimiento se está verificando…");
        } else if (TemporarySeparationPolicy.PAYMENT_VERIFICATION.equals(current.status)) {
            counter.setText("Comprobante recibido. El pago está en verificación.");
        } else {
            long hours = Math.max(0, remaining) / 3_600_000L;
            long minutes = (Math.max(0, remaining) % 3_600_000L) / 60_000L;
            counter.setText("Tiempo restante: " + hours + " h " + minutes + " min");
        }
    }

    private void setupCopyButton(int buttonId, int textViewId) {
        View button = findViewById(buttonId);
        TextView textView = findViewById(textViewId);
        if (button == null || textView == null) return;
        button.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Cuenta Bancaria", textView.getText().toString());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Copiado al portapapeles", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmCancellation() {
        if (current == null || !current.canCancel || submitting) return;
        new AlertDialog.Builder(this)
                .setTitle("Cancelar separación")
                .setMessage("La unidad se liberará inmediatamente para otros usuarios.")
                .setNegativeButton("Volver", null)
                .setPositiveButton("Cancelar separación", (dialog, which) -> cancelSeparation())
                .show();
    }

    private void cancelSeparation() {
        if (current == null) return;
        submitting = true;
        separationRepository.cancelTemporarySeparation(current.id, new FirebaseSeparationRepository.SimpleCallback() {
            @Override public void onSuccess(String separationId) {
                if (!active()) return;
                submitting = false;
                Toast.makeText(UsuarioSeguimientoSeparacionActivity.this, "Separación cancelada. La unidad fue liberada.", Toast.LENGTH_LONG).show();
            }
            @Override public void onError(String message) {
                if (!active()) return;
                submitting = false;
                Toast.makeText(UsuarioSeguimientoSeparacionActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String formatPen(double amount) {
        return "S/ " + NumberFormat.getNumberInstance(new Locale("es", "PE")).format(amount);
    }

    private String value(String input) { return input == null ? "" : input.trim(); }
    private boolean active() { return !isFinishing() && !isDestroyed(); }

    private void applyInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View root = findViewById(R.id.separationTrackingRoot);
        int left = root.getPaddingLeft(), top = root.getPaddingTop(), right = root.getPaddingRight(), bottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }
}
