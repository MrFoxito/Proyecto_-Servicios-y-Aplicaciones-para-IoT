package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;

import java.util.ArrayList;
import java.util.List;

public class UsuarioChatsActivity extends BaseUsuarioActivity {

    private static final int FILTER_ALL = 0;
    private static final int FILTER_UNREAD = 1;
    private static final int FILTER_FAVORITES = 2;

    private final List<UsuarioChatListItem> allItems = new ArrayList<>();
    private UsuarioChatListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_chats);
        setupUserBottomNav(R.id.navUserChats);
        setupChatList();
        setupFilters();
    }

    private void setupChatList() {
        RecyclerView recyclerView = findViewById(R.id.recyclerChats);
        if (recyclerView == null) {
            return;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        allItems.clear();
        allItems.addAll(buildChatItems());
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

    private List<UsuarioChatListItem> buildChatItems() {
        List<UsuarioChatListItem> items = new ArrayList<>();
        items.add(new UsuarioChatListItem(
                getString(R.string.chat_row_2_name),
                getString(R.string.chat_row_2_message),
                getString(R.string.chat_row_2_time),
                R.drawable.sa_profile_asesor_2,
                "",
                false,
                true,
                true
        ));
        items.add(new UsuarioChatListItem(
                getString(R.string.chat_row_3_name),
                getString(R.string.chat_row_3_message),
                getString(R.string.chat_row_3_time),
                R.drawable.sa_profile_user_1,
                "",
                false,
                false,
                true
        ));
        items.add(new UsuarioChatListItem(
                getString(R.string.chat_row_4_name),
                getString(R.string.chat_row_4_message),
                getString(R.string.chat_row_4_time),
                R.drawable.sa_profile_user_2,
                "",
                false,
                true,
                false
        ));
        items.add(new UsuarioChatListItem(
                getString(R.string.chat_row_5_name),
                getString(R.string.chat_row_5_message),
                getString(R.string.chat_row_5_time),
                0,
                getString(R.string.chat_row_5_initials),
                true,
                false,
                false
        ));
        return items;
    }

    private void openChatDetail(UsuarioChatListItem item) {
        Intent intent = new Intent(this, UsuarioChatDetalleActivity.class);
        intent.putExtra(UsuarioChatDetalleActivity.EXTRA_CONTACT_NAME, item.getName());
        startActivity(intent);
    }
}
