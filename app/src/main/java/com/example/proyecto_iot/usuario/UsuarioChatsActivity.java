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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UsuarioChatsActivity extends BaseUsuarioActivity {

    private static final int FILTER_ALL = 0;
    private static final int FILTER_UNREAD = 1;
    private static final int FILTER_FAVORITES = 2;

    private final List<UsuarioChatListItem> allItems = new ArrayList<>();
    private final Map<String, FirebaseChatRepository.Advisor> advisorsByUid = new HashMap<>();
    private final FirebaseChatRepository chatRepository = new FirebaseChatRepository();
    private UsuarioChatListAdapter adapter;
    private AuthSessionManager sessionManager;
    private ListenerRegistration conversationsRegistration;
    private String clienteUid = "";
    private int activeFilter = FILTER_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_chats);
        sessionManager = new AuthSessionManager(this);
        clienteUid = currentUid();
        setupUserBottomNav(R.id.navUserChats);
        setupChatList();
        setupFilters();
        setupAdvisorSearch();
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

    private void setupAdvisorSearch() {
        EditText search = findViewById(R.id.inputSearchAdvisors);
        if (search == null) {
            return;
        }
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s == null ? "" : s.toString().trim();
                if (query.isEmpty()) {
                    advisorsByUid.clear();
                    applyFilter(activeFilter);
                    return;
                }
                searchAdvisors(query);
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

    private void searchAdvisors(String query) {
        chatRepository.searchActiveAdvisors(query, new FirebaseChatRepository.AdvisorsCallback() {
            @Override
            public void onSuccess(List<FirebaseChatRepository.Advisor> advisors) {
                advisorsByUid.clear();
                List<UsuarioChatListItem> searchItems = new ArrayList<>();
                for (FirebaseChatRepository.Advisor advisor : advisors) {
                    advisorsByUid.put(advisor.uid, advisor);
                    searchItems.add(advisorToItem(advisor));
                }
                if (adapter != null) {
                    adapter.submitItems(searchItems);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(UsuarioChatsActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openChatDetail(UsuarioChatListItem item) {
        if (item.getConversationId() != null && !item.getConversationId().trim().isEmpty()) {
            openChatDetail(item.getConversationId(), item.getAsesorUid(), item.getName());
            return;
        }
        FirebaseChatRepository.Advisor advisor = advisorsByUid.get(item.getAsesorUid());
        if (advisor == null) {
            Toast.makeText(this, "No se pudo identificar al asesor seleccionado.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (clienteUid.isEmpty()) {
            Toast.makeText(this, "No hay sesion Firebase activa para iniciar chat.", Toast.LENGTH_LONG).show();
            return;
        }
        chatRepository.findOrCreateConversation(
                clienteUid,
                sessionManager.getUserName(),
                advisor,
                new FirebaseChatRepository.ConversationCallback() {
                    @Override
                    public void onSuccess(FirebaseChatRepository.Conversation conversation) {
                        openChatDetail(conversation.id, conversation.asesorUid, conversation.asesorNombre);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(UsuarioChatsActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void openChatDetail(String conversationId, String asesorUid, String contactName) {
        Intent intent = new Intent(this, UsuarioChatDetalleActivity.class);
        intent.putExtra(UsuarioChatDetalleActivity.EXTRA_CONVERSATION_ID, conversationId);
        intent.putExtra(UsuarioChatDetalleActivity.EXTRA_ASESOR_UID, asesorUid);
        intent.putExtra(UsuarioChatDetalleActivity.EXTRA_CONTACT_NAME, contactName);
        startActivity(intent);
    }

    private UsuarioChatListItem conversationToItem(FirebaseChatRepository.Conversation conversation) {
        return new UsuarioChatListItem(
                conversation.asesorNombre,
                conversation.lastMessage,
                formatTime(conversation.lastMessageAt),
                R.drawable.sa_profile_asesor_1,
                initials(conversation.asesorNombre),
                false,
                conversation.unreadForCliente,
                false,
                conversation.id,
                conversation.asesorUid,
                conversation.lastMessageAt
        );
    }

    private UsuarioChatListItem advisorToItem(FirebaseChatRepository.Advisor advisor) {
        return new UsuarioChatListItem(
                advisor.name,
                advisor.email.isEmpty() ? "Toca para iniciar conversacion" : advisor.email,
                "",
                R.drawable.sa_profile_asesor_1,
                initials(advisor.name),
                false,
                false,
                false,
                "",
                advisor.uid,
                0L
        );
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
