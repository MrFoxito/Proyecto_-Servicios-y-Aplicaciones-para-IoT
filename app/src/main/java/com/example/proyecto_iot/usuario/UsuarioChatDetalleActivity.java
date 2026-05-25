package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.entity.MensajeChat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UsuarioChatDetalleActivity extends AppCompatActivity {

    public static final String EXTRA_CONTACT_NAME = "extra_contact_name";
    public static final String EXTRA_CHAT_ID      = "extra_chat_id";

    private String chatId;
    private LinearLayout messagesContainer;
    private ScrollView scrollView;
    private LocalSchemaStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_chat_detalle);
        applyInsets();

        storage = new LocalSchemaStorage(this);
        messagesContainer = findViewById(R.id.chatMessagesContainer);
        scrollView = findViewById(R.id.chatDetailScroll);

        bindDynamicData();
        loadPersistedMessages();
        setupActions();
    }

    private void bindDynamicData() {
        Intent intent = getIntent();
        if (intent == null) return;

        chatId = intent.getStringExtra(EXTRA_CHAT_ID);
        String contactName = intent.getStringExtra(EXTRA_CONTACT_NAME);

        TextView contactNameView = findViewById(R.id.tvChatContactName);
        if (contactNameView != null && contactName != null && !contactName.trim().isEmpty()) {
            contactNameView.setText(contactName);
        }
    }

    /**
     * Carga los mensajes persistidos en el storage y los renderiza.
     * Los mensajes del seed (sin chatId) se muestran solo si no hay chatId específico.
     */
    private void loadPersistedMessages() {
        if (messagesContainer == null) return;
        messagesContainer.removeAllViews(); // limpia el XML estático

        List<MensajeChat> messages = storage.getChatMessages(chatId);
        for (MensajeChat msg : messages) {
            if (msg.isDateHeader()) {
                addDateHeader(msg.getText());
            } else {
                addMessageBubble(msg.getText(), msg.getTime(), msg.isSentByMe());
            }
        }

        // Scroll al final
        if (scrollView != null) {
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        }
    }

    private void setupActions() {
        View back = findViewById(R.id.btnBackChatDetail);
        if (back != null) back.setOnClickListener(v -> finish());

        View details = findViewById(R.id.btnChatPropertyDetails);
        if (details != null) {
            details.setOnClickListener(v -> {
                TextView title    = findViewById(R.id.tvChatPropertyTitle);
                TextView location = findViewById(R.id.tvChatPropertyLocation);
                TextView price    = findViewById(R.id.tvChatPropertyPrice);
                UsuarioPropertyCatalog.PropertyDetail propertyDetail =
                        UsuarioPropertyCatalog.findByTitle(
                                title != null ? title.getText().toString() : null);
                Intent intent = new Intent(this, UsuarioPropiedadDetalleActivity.class);
                if (propertyDetail != null) {
                    intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_ID,
                            propertyDetail.getId());
                }
                if (title    != null) intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_TITLE,    title.getText().toString());
                if (price    != null) intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_PRICE,    price.getText().toString());
                if (location != null) intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_LOCATION, location.getText().toString());
                startActivity(intent);
            });
        }

        View attach = findViewById(R.id.btnAttachChat);
        if (attach != null) {
            attach.setOnClickListener(v ->
                    Toast.makeText(this, R.string.chat_attachment_coming_soon, Toast.LENGTH_SHORT).show());
        }

        View send = findViewById(R.id.btnSendChatMessage);
        if (send != null) send.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        EditText input = findViewById(R.id.chatMessageInput);
        if (input == null || messagesContainer == null) return;

        String message = input.getText() != null ? input.getText().toString().trim() : "";
        if (message.isEmpty()) {
            Toast.makeText(this, R.string.chat_empty_message_error, Toast.LENGTH_SHORT).show();
            return;
        }

        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

        // Persiste en el storage
        storage.addChatMessage(chatId, message, true);

        // Renderiza la burbuja visualmente
        addMessageBubble(message, time, true);

        input.setText("");
        if (scrollView != null) {
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        }
    }

    // -------------------------------------------------------------------------
    // Helpers de renderizado
    // -------------------------------------------------------------------------

    private void addDateHeader(String text) {
        if (messagesContainer == null) return;
        TextView header = new TextView(this);
        header.setText(text);
        header.setTextSize(12f);
        header.setTextColor(ContextCompat.getColor(this, R.color.app_text_soft));
        header.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dpToPx(20), 0, dpToPx(8));
        header.setLayoutParams(params);
        messagesContainer.addView(header);
    }

    private void addMessageBubble(String message, String time, boolean sentByMe) {
        if (messagesContainer == null) return;

        // Burbuja
        TextView bubble = new TextView(this);
        bubble.setText(message);
        bubble.setTextSize(16f);
        bubble.setTextColor(ContextCompat.getColor(this,
                sentByMe ? android.R.color.white : R.color.app_text_primary));
        bubble.setBackgroundResource(sentByMe
                ? R.drawable.user_chat_message_out_bg
                : R.drawable.user_chat_message_in_bg);
        bubble.setMaxWidth(dpToPx(272));
        int p = dpToPx(16);
        bubble.setPadding(p, p, p, p);
        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bubble.setLayoutParams(bubbleParams);

        LinearLayout bubbleRow = new LinearLayout(this);
        bubbleRow.setOrientation(LinearLayout.HORIZONTAL);
        bubbleRow.setGravity(sentByMe ? Gravity.END : Gravity.START);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, dpToPx(24), 0, 0);
        bubbleRow.setLayoutParams(rowParams);
        bubbleRow.addView(bubble);
        messagesContainer.addView(bubbleRow);

        // Hora
        TextView timeView = new TextView(this);
        timeView.setText(time);
        timeView.setTextSize(13f);
        timeView.setTextColor(ContextCompat.getColor(this, R.color.app_text_soft));

        LinearLayout timeRow = new LinearLayout(this);
        timeRow.setOrientation(LinearLayout.HORIZONTAL);
        timeRow.setGravity(sentByMe ? Gravity.END : Gravity.START);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        timeParams.setMargins(0, dpToPx(8), dpToPx(4), 0);
        timeRow.setLayoutParams(timeParams);
        timeRow.addView(timeView);
        messagesContainer.addView(timeRow);
    }

    private void applyInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewGroup content = findViewById(android.R.id.content);
        if (content == null || content.getChildCount() == 0) return;

        View root = content.getChildAt(0);
        final int rootLeft   = root.getPaddingLeft();
        final int rootTop    = root.getPaddingTop();
        final int rootRight  = root.getPaddingRight();
        final int rootBottom = root.getPaddingBottom();

        View inputBar = findViewById(R.id.chatComposerBar);
        final int composerBottom = inputBar != null ? inputBar.getPaddingBottom() : 0;

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(rootLeft + bars.left, rootTop + bars.top,
                    rootRight + bars.right, rootBottom);
            if (inputBar != null) {
                inputBar.setPadding(inputBar.getPaddingLeft(), inputBar.getPaddingTop(),
                        inputBar.getPaddingRight(), composerBottom + bars.bottom);
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
