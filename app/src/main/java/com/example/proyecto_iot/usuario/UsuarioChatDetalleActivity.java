package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UsuarioChatDetalleActivity extends AppCompatActivity {
    public static final String EXTRA_CONTACT_NAME = "extra_contact_name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_chat_detalle);
        applyInsets();
        bindDynamicData();

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
                Intent intent = new Intent(this, UsuarioPropiedadDetalleActivity.class);
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

        String contactName = intent.getStringExtra(EXTRA_CONTACT_NAME);
        TextView contactNameView = findViewById(R.id.tvChatContactName);
        if (contactNameView != null && contactName != null && !contactName.trim().isEmpty()) {
            contactNameView.setText(contactName);
        }
    }

    private void sendMessage() {
        EditText input = findViewById(R.id.chatMessageInput);
        LinearLayout container = findViewById(R.id.chatMessagesContainer);
        ScrollView scrollView = findViewById(R.id.chatDetailScroll);
        if (input == null || container == null) {
            return;
        }

        String message = input.getText() != null ? input.getText().toString().trim() : "";
        if (message.isEmpty()) {
            Toast.makeText(this, R.string.chat_empty_message_error, Toast.LENGTH_SHORT).show();
            return;
        }

        TextView bubble = new TextView(this);
        bubble.setText(message);
        bubble.setTextSize(16f);
        bubble.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        bubble.setBackgroundResource(R.drawable.user_chat_message_out_bg);
        int padding = dpToPx(16);
        bubble.setPadding(padding, padding, padding, padding);
        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        bubbleParams.setMargins(dpToPx(92), dpToPx(20), 0, 0);
        bubble.setLayoutParams(bubbleParams);
        container.addView(bubble);

        TextView time = new TextView(this);
        time.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date()));
        time.setTextSize(13f);
        time.setTextColor(ContextCompat.getColor(this, R.color.app_text_soft));
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        timeParams.setMargins(0, dpToPx(8), dpToPx(8), 0);
        time.setLayoutParams(timeParams);
        time.setGravity(android.view.Gravity.END);
        container.addView(time);

        input.setText("");
        if (scrollView != null) {
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
