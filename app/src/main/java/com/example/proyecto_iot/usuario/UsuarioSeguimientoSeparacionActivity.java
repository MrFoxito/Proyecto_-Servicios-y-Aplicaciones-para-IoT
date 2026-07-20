package com.example.proyecto_iot.usuario;

import android.app.DatePickerDialog;
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
    private final ProjectMediaRepository mediaRepository = new ProjectMediaRepository(this);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ListenerRegistration separationListener;
    private FirebaseSeparationRepository.TemporarySeparation current;
    private Uri receiptUri;
    private TextView selectedReceiptText;
    private boolean submitting;

    private final ActivityResultLauncher<String[]> receiptPicker = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                receiptUri = uri;
                try {
                    getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) { }
                if (selectedReceiptText != null) {
                    selectedReceiptText.setText("Comprobante adjuntado");
                }
            });

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
        findViewById(R.id.btnTrackingPay).setOnClickListener(v -> explainOnlinePayment());
        findViewById(R.id.btnTrackingExternal).setOnClickListener(v -> showExternalPaymentDialog());
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

        View pay = findViewById(R.id.btnTrackingPay);
        View external = findViewById(R.id.btnTrackingExternal);
        View cancel = findViewById(R.id.btnTrackingCancel);
        boolean awaitingPayment = TemporarySeparationPolicy.PENDING_PAYMENT.equals(current.status);
        pay.setVisibility(awaitingPayment ? View.VISIBLE : View.GONE);
        external.setVisibility(current.canSubmitExternalPayment ? View.VISIBLE : View.GONE);
        cancel.setVisibility(current.canCancel ? View.VISIBLE : View.GONE);
    }

    private void renderCountdown() {
        TextView message = findViewById(R.id.tvTrackingExpiryMessage);
        TextView counter = findViewById(R.id.tvTrackingCountdown);
        if (current == null) {
            message.setText("Cargando separación…");
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

    private void explainOnlinePayment() {
        new AlertDialog.Builder(this)
                .setTitle("Pago en línea no configurado")
                .setMessage("Todavía no hay una pasarela de pago conectada. Registra un pago externo para enviar el comprobante sin marcar el pago como confirmado.")
                .setNegativeButton("Cerrar", null)
                .setPositiveButton("Registrar pago externo", (dialog, which) -> showExternalPaymentDialog())
                .show();
    }

    private void showExternalPaymentDialog() {
        if (current == null || !current.canSubmitExternalPayment || submitting) return;
        View content = getLayoutInflater().inflate(R.layout.dialog_usuario_pago_externo, null, false);
        Spinner method = content.findViewById(R.id.spExternalMethod);
        method.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Transferencia", "Yape / Plin", "WhatsApp", "Oficina física", "Otro"}));
        EditText operation = content.findViewById(R.id.etExternalOperation);
        EditText paidAt = content.findViewById(R.id.etExternalDate);
        EditText comment = content.findViewById(R.id.etExternalComment);
        selectedReceiptText = content.findViewById(R.id.tvExternalReceipt);
        content.findViewById(R.id.btnExternalReceipt).setOnClickListener(v ->
                receiptPicker.launch(new String[]{"image/*", "application/pdf"}));
        paidAt.setOnClickListener(v -> showDatePicker(paidAt));
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Registrar pago externo")
                .setView(content)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Enviar a verificación", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> submitExternalPayment(dialog,
                        String.valueOf(method.getSelectedItem()), operation, paidAt, comment)));
        dialog.show();
    }

    private void showDatePicker(EditText field) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> field.setText(String.format(Locale.US,
                "%02d/%02d/%04d", day, month + 1, year)), calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void submitExternalPayment(AlertDialog dialog, String method, EditText operation,
                                       EditText paidAt, EditText comment) {
        if (receiptUri == null) {
            Toast.makeText(this, "Adjunta el comprobante de pago.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (value(operation.getText().toString()).isEmpty() || value(paidAt.getText().toString()).isEmpty()) {
            Toast.makeText(this, "Indica el número de operación y la fecha de pago.", Toast.LENGTH_SHORT).show();
            return;
        }
        submitting = true;
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        mediaRepository.uploadSeparationReceipt(current.id, receiptUri, new SupabaseStorageRepository.UploadCallback() {
            @Override public void onSuccess(SupabaseStorageRepository.UploadResult result) {
                if (!active()) return;
                FirebaseSeparationRepository.ExternalPaymentDraft draft = new FirebaseSeparationRepository.ExternalPaymentDraft();
                draft.separationId = current.id;
                draft.method = method;
                draft.operationNumber = value(operation.getText().toString());
                draft.paidAt = value(paidAt.getText().toString());
                draft.receiptUrl = result.publicUrl;
                draft.comment = value(comment.getText().toString());
                separationRepository.submitExternalTemporaryPayment(draft, new FirebaseSeparationRepository.SimpleCallback() {
                    @Override public void onSuccess(String separationId) {
                        if (!active()) return;
                        submitting = false;
                        dialog.dismiss();
                        Toast.makeText(UsuarioSeguimientoSeparacionActivity.this,
                                "Pago enviado para verificación.", Toast.LENGTH_LONG).show();
                    }
                    @Override public void onError(String message) {
                        if (!active()) return;
                        submitting = false;
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        Toast.makeText(UsuarioSeguimientoSeparacionActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
            }
            @Override public void onError(String message) {
                if (!active()) return;
                submitting = false;
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                Toast.makeText(UsuarioSeguimientoSeparacionActivity.this, message, Toast.LENGTH_LONG).show();
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
