package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseChatRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UsuarioChatsActivity extends BaseUsuarioActivity {

    private static final int FILTER_ALL = 0;
    private static final int FILTER_UNREAD = 1;
    private static final int FILTER_FAVORITES = 2;

    private final List<UsuarioChatListItem> allItems = new ArrayList<>();
    private final FirebaseChatRepository chatRepository = new FirebaseChatRepository();
    private UsuarioChatListAdapter adapter;
    private AuthSessionManager sessionManager;
    private ListenerRegistration conversationsRegistration;
    private String clienteUid = "";
    private int activeFilter = FILTER_ALL;
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_chats);
        sessionManager = AuthSessionManager.getInstance(this);
        clienteUid = currentUid();
        setupUserBottomNav(R.id.navUserChats);
        setupChatList();
        setupFilters();
        setupConversationSearch();
        listenConversations();
    }

    private void setupChatList() {
        RecyclerView recyclerView = findViewById(R.id.recyclerChats);
        if (recyclerView == null) {
            return;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UsuarioChatListAdapter(new ArrayList<>(allItems), this::openChatDetail);
        recyclerView.setAdapter(adapter);
    }

    private void setupFilters() {
        setupFilterChip(R.id.chipChatsAll, FILTER_ALL);
        setupFilterChip(R.id.chipChatsUnread, FILTER_UNREAD);
        setupFilterChip(R.id.chipChatsFavorites, FILTER_FAVORITES);
        applyFilter(FILTER_ALL);
    }

    private void setupFilterChip(int viewId, int filter) {
        View view = findViewById(viewId);
        if (view == null) {
            return;
        }
        view.setOnClickListener(v -> applyFilter(filter));
    }

    private void applyFilter(int filter) {
        activeFilter = filter;
        if (adapter == null) {
            return;
        }

        setChipSelected(R.id.chipChatsAll, filter == FILTER_ALL);
        setChipSelected(R.id.chipChatsUnread, filter == FILTER_UNREAD);
        setChipSelected(R.id.chipChatsFavorites, filter == FILTER_FAVORITES);

        List<UsuarioChatListItem> filtered = new ArrayList<>();
        for (UsuarioChatListItem item : allItems) {
            if (filter == FILTER_UNREAD && !item.isUnread()) {
                continue;
            }
            if (filter == FILTER_FAVORITES && !item.isFavorite()) {
                continue;
            }
            if (!matchesSearch(item)) {
                continue;
            }
            filtered.add(item);
        }
        adapter.submitItems(filtered);
    }

    private void setChipSelected(int viewId, boolean selected) {
        TextView chip = findViewById(viewId);
        if (chip == null) {
            return;
        }
        chip.setBackgroundResource(selected
                ? R.drawable.user_filter_chip_active_bg
                : R.drawable.user_filter_chip_inactive_bg);
        chip.setTextColor(getColor(selected
                ? R.color.app_nav_active_text
                : R.color.app_chip_inactive_text));
        chip.setTypeface(chip.getTypeface(), selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    private void setupConversationSearch() {
        EditText search = findViewById(R.id.inputSearchAdvisors);
        if (search == null) {
            return;
        }
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s == null ? "" : s.toString().trim().toLowerCase(Locale.ROOT);
                applyFilter(activeFilter);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void listenConversations() {
        if (clienteUid.isEmpty()) {
            Toast.makeText(this, "No hay sesion Firebase activa para cargar chats.", Toast.LENGTH_LONG).show();
            return;
        }
        if (conversationsRegistration != null) {
            conversationsRegistration.remove();
        }
        conversationsRegistration = chatRepository.listenClientConversations(
                clienteUid,
                new FirebaseChatRepository.ConversationsCallback() {
                    @Override
                    public void onSuccess(List<FirebaseChatRepository.Conversation> conversations) {
                        allItems.clear();
                        for (FirebaseChatRepository.Conversation conversation : conversations) {
                            allItems.add(conversationToItem(conversation));
                        }
                        applyFilter(activeFilter);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(UsuarioChatsActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void openChatDetail(UsuarioChatListItem item) {
        if (item.getConversationId() != null && !item.getConversationId().trim().isEmpty()) {
            openChatDetail(item.getConversationId(), item.getAsesorUid(), item.getSecondaryText(),
                    item.getProjectId(), item.getProjectName(), item.getProjectLocation(),
                    item.getProjectPrice(), item.getProjectImageUrl());
            return;
        }
        Toast.makeText(this, "Inicia una conversación desde el detalle de un proyecto.", Toast.LENGTH_LONG).show();
    }

    private void openChatDetail(String conversationId, String asesorUid, String contactName,
                                String projectId, String projectName, String projectLocation,
                                String projectPrice, String projectImageUrl) {
        Intent intent = new Intent(this, UsuarioChatDetalleActivity.class);
        intent.putExtra(UsuarioChatDetalleActivity.EXTRA_CONVERSATION_ID, conversationId);
        intent.putExtra(UsuarioChatDetalleActivity.EXTRA_ASESOR_UID, asesorUid);
        intent.putExtra(UsuarioChatDetalleActivity.EXTRA_CONTACT_NAME, contactName);
        intent.putExtra(UsuarioChatDetalleActivity.EXTRA_PROJECT_ID, projectId);
        intent.putExtra(UsuarioChatDetalleActivity.EXTRA_PROJECT_NAME, projectName);
        intent.putExtra(UsuarioChatDetalleActivity.EXTRA_PROJECT_LOCATION, projectLocation);
        intent.putExtra(UsuarioChatDetalleActivity.EXTRA_PROJECT_PRICE, projectPrice);
        intent.putExtra(UsuarioChatDetalleActivity.EXTRA_PROJECT_IMAGE_URL, projectImageUrl);
        startActivity(intent);
    }

    private UsuarioChatListItem conversationToItem(FirebaseChatRepository.Conversation conversation) {
        return new UsuarioChatListItem(
                conversation.projectName.isEmpty() ? "Proyecto" : conversation.projectName,
                "Asesor: " + conversation.asesorNombre,
                conversation.lastMessage,
                formatTime(conversation.lastMessageAt),
                R.drawable.sa_profile_asesor_1,
                initials(conversation.projectName),
                false,
                conversation.unreadForCliente,
                false,
                conversation.id,
                conversation.asesorUid,
                conversation.lastMessageAt,
                conversation.projectId,
                conversation.projectName,
                conversation.projectLocation,
                conversation.projectPrice,
                conversation.projectImageUrl
        );
    }

    private boolean matchesSearch(UsuarioChatListItem item) {
        if (searchQuery.isEmpty()) return true;
        return contains(item.getName())
                || contains(item.getSecondaryText())
                || contains(item.getPreviewText());
    }

    private boolean contains(String value) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(searchQuery);
    }

    private String currentUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user == null ? "" : user.getUid();
    }

    private String initials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "AS";
        }
        String[] parts = name.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String second = parts.length > 1 ? parts[1].substring(0, 1) : "";
        return (first + second).toUpperCase(Locale.ROOT);
    }

    private String formatTime(long millis) {
        if (millis <= 0L) {
            return "";
        }
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(millis));
    }

    @Override
    protected void onDestroy() {
        if (conversationsRegistration != null) {
            conversationsRegistration.remove();
        }
        super.onDestroy();
    }
}
