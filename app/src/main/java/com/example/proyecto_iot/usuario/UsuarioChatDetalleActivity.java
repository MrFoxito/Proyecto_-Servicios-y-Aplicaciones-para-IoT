package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.core.widget.NestedScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseChatRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;
import java.util.Locale;

public class UsuarioChatDetalleActivity extends AppCompatActivity {
    public static final String EXTRA_CONTACT_NAME = "extra_contact_name";
    public static final String EXTRA_CONVERSATION_ID = "extra_conversation_id";
    public static final String EXTRA_ASESOR_UID = "extra_asesor_uid";

    private final FirebaseChatRepository chatRepository = new FirebaseChatRepository();
    private ListenerRegistration messagesRegistration;
    private String conversationId = "";
    private String asesorUid = "";
    private String clienteUid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_chat_detalle);
        applyInsets();
        bindDynamicData();
        listenMessages();

        View back = findViewById(R.id.btnBackChatDetail);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        View details = findViewById(R.id.btnChatPropertyDetails);
        if (details != null) {
            details.setOnClickListener(v -> {
                TextView title = findViewById(R.id.tvChatPropertyTitle);
                TextView location = findViewById(R.id.tvChatPropertyLocation);
                TextView price = findViewById(R.id.tvChatPropertyPrice);
                UsuarioPropertyCatalog.PropertyDetail propertyDetail = UsuarioPropertyCatalog.findByTitle(
                        title != null ? title.getText().toString() : null
                );
                Intent intent = new Intent(this, UsuarioPropiedadDetalleActivity.class);
                if (propertyDetail != null) {
                    intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_ID, propertyDetail.getId());
                }
                if (title != null) {
                    intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_TITLE, title.getText().toString());
                }
                if (price != null) {
                    intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_PRICE, price.getText().toString());
                }
                if (location != null) {
                    intent.putExtra(UsuarioPropiedadDetalleActivity.EXTRA_PROPERTY_LOCATION, location.getText().toString());
                }
                startActivity(intent);
            });
        }

        View attach = findViewById(R.id.btnAttachChat);
        if (attach != null) {
            attach.setOnClickListener(v ->
                    Toast.makeText(this, R.string.chat_attachment_coming_soon, Toast.LENGTH_SHORT).show());
        }

        View send = findViewById(R.id.btnSendChatMessage);
        if (send != null) {
            send.setOnClickListener(v -> sendMessage());
        }
    }

    private void applyInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        ViewGroup content = findViewById(android.R.id.content);
        if (content == null || content.getChildCount() == 0) {
            return;
        }

        View root = content.getChildAt(0);
        final int rootLeft = root.getPaddingLeft();
        final int rootTop = root.getPaddingTop();
        final int rootRight = root.getPaddingRight();
        final int rootBottom = root.getPaddingBottom();

        View inputBar = findViewById(R.id.chatComposerBar);
        final int composerBottom = inputBar != null ? inputBar.getPaddingBottom() : 0;

        View finalInputBar = inputBar;
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(rootLeft + bars.left, rootTop + bars.top, rootRight + bars.right, rootBottom);
            if (finalInputBar != null) {
                finalInputBar.setPadding(
                        finalInputBar.getPaddingLeft(),
                        finalInputBar.getPaddingTop(),
                        finalInputBar.getPaddingRight(),
                        composerBottom + bars.bottom
                );
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void bindDynamicData() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        conversationId = valueOr(intent.getStringExtra(EXTRA_CONVERSATION_ID));
        asesorUid = valueOr(intent.getStringExtra(EXTRA_ASESOR_UID));
        clienteUid = currentUid();
        String contactName = intent.getStringExtra(EXTRA_CONTACT_NAME);
        TextView contactNameView = findViewById(R.id.tvChatContactName);
        if (contactNameView != null && contactName != null && !contactName.trim().isEmpty()) {
            contactNameView.setText(contactName);
        }
    }

    private void listenMessages() {
        if (conversationId.isEmpty() || clienteUid.isEmpty()) {
            removeStaticMessages();
            return;
        }
        if (messagesRegistration != null) {
            messagesRegistration.remove();
        }
        messagesRegistration = chatRepository.listenMessages(
                conversationId,
                clienteUid,
                new FirebaseChatRepository.MessagesCallback() {
                    @Override
                    public void onSuccess(List<FirebaseChatRepository.Message> messages) {
                        renderMessages(messages);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(UsuarioChatDetalleActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void sendMessage() {
        EditText input = findViewById(R.id.chatMessageInput);
        if (input == null) {
            return;
        }

        String message = input.getText() != null ? input.getText().toString().trim() : "";
        if (message.isEmpty()) {
            Toast.makeText(this, R.string.chat_empty_message_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (conversationId.isEmpty() || clienteUid.isEmpty() || asesorUid.isEmpty()) {
            Toast.makeText(this, "No se pudo identificar la conversacion para enviar.", Toast.LENGTH_LONG).show();
            return;
        }

        input.setEnabled(false);
        chatRepository.sendMessage(
                conversationId,
                clienteUid,
                asesorUid,
                message,
                new FirebaseChatRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        input.setText("");
                        input.setEnabled(true);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        input.setEnabled(true);
                        Toast.makeText(UsuarioChatDetalleActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void renderMessages(List<FirebaseChatRepository.Message> messages) {
        LinearLayout container = findViewById(R.id.chatMessagesContainer);
        NestedScrollView scrollView = findViewById(R.id.chatDetailScroll);
        if (container == null) {
            return;
        }
        removeStaticMessages();
        if (messages == null || messages.isEmpty()) {
            addEmptyState(container);
        } else {
            for (FirebaseChatRepository.Message message : messages) {
                boolean sentByMe = clienteUid.equals(message.senderUid);
                addMessageBubble(container, message.text, formatTime(message.createdAt), sentByMe);
            }
        }
        if (scrollView != null) {
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        }
    }

    private void removeStaticMessages() {
        LinearLayout container = findViewById(R.id.chatMessagesContainer);
        if (container == null) {
            return;
        }
        while (container.getChildCount() > 2) {
            container.removeViewAt(2);
        }
    }

    private void addEmptyState(LinearLayout container) {
        TextView empty = new TextView(this);
        empty.setText("Aun no hay mensajes. Escribe para iniciar la conversacion.");
        empty.setTextSize(15f);
        empty.setTextColor(ContextCompat.getColor(this, R.color.app_text_secondary));
        empty.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dpToPx(24), 0, 0);
        empty.setLayoutParams(params);
        container.addView(empty);
    }

    private void addMessageBubble(LinearLayout container, String text, String timeText, boolean sentByMe) {
        TextView bubble = new TextView(this);
        bubble.setText(text);
        bubble.setTextSize(16f);
        bubble.setTextColor(ContextCompat.getColor(this, sentByMe ? android.R.color.white : R.color.app_text_primary));
        bubble.setBackgroundResource(sentByMe
                ? R.drawable.user_chat_message_out_bg
                : R.drawable.user_chat_message_in_bg);
        bubble.setMaxWidth(dpToPx(272));
        int padding = dpToPx(16);
        bubble.setPadding(padding, padding, padding, padding);
        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        bubble.setLayoutParams(bubbleParams);

        LinearLayout bubbleRow = new LinearLayout(this);
        bubbleRow.setOrientation(LinearLayout.HORIZONTAL);
        bubbleRow.setGravity(sentByMe ? Gravity.END : Gravity.START);
        LinearLayout.LayoutParams bubbleRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        bubbleRowParams.setMargins(0, dpToPx(24), 0, 0);
        bubbleRow.setLayoutParams(bubbleRowParams);
        bubbleRow.addView(bubble);
        container.addView(bubbleRow);

        TextView time = new TextView(this);
        time.setText(timeText);
        time.setTextSize(13f);
        time.setTextColor(ContextCompat.getColor(this, R.color.app_text_soft));

        LinearLayout timeRow = new LinearLayout(this);
        timeRow.setOrientation(LinearLayout.HORIZONTAL);
        timeRow.setGravity(sentByMe ? Gravity.END : Gravity.START);
        LinearLayout.LayoutParams timeRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        timeRowParams.setMargins(dpToPx(4), dpToPx(8), dpToPx(4), 0);
        timeRow.setLayoutParams(timeRowParams);
        timeRow.addView(time);
        container.addView(timeRow);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private String currentUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user == null ? "" : user.getUid();
    }

    private String valueOr(String value) {
        return value == null ? "" : value.trim();
    }

    private String formatTime(long millis) {
        if (millis <= 0L) {
            return "";
        }
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(millis));
    }

    @Override
    protected void onDestroy() {
        if (messagesRegistration != null) {
            messagesRegistration.remove();
        }
        super.onDestroy();
    }
}
