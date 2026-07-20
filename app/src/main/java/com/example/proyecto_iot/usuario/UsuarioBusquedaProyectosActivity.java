package com.example.proyecto_iot.usuario;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/** Full-screen project discovery search. Results never overlap the Explore home screen. */
public class UsuarioBusquedaProyectosActivity extends AppCompatActivity {
    private static final long SEARCH_DEBOUNCE_MS = 300L;
    private static final int MAX_SEARCH_RESULTS = 50;

    private final FirebaseDataRepository dataRepository = new FirebaseDataRepository();
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private final List<FirebaseDataRepository.UserProjectSearchItem> searchCatalog = new ArrayList<>();

    private EditText input;
    private ImageButton clearButton;
    private UsuarioBusquedaProyectosAdapter adapter;
    private RecentProjectSearchStore recentStore;
    private boolean catalogLoading;
    private boolean catalogLoaded;
    private String catalogError = "";
    private Runnable pendingSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_busqueda_proyectos);
        applyInsets();
        setupList();
        setupSearchInput();
        String uid = FirebaseAuth.getInstance().getCurrentUser() == null
                ? "local"
                : FirebaseAuth.getInstance().getCurrentUser().getUid();
        recentStore = new RecentProjectSearchStore(this, uid);
        showEmptyState();
        loadCatalog(false);
        focusAndShowKeyboard();
    }

    private void applyInsets() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        View root = findViewById(R.id.projectSearchRoot);
        if (root == null) return;
        final int left = root.getPaddingLeft();
        final int top = root.getPaddingTop();
        final int right = root.getPaddingRight();
        final int bottom = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void setupList() {
        RecyclerView list = findViewById(R.id.recyclerProjectSearchResults);
        if (list == null) return;
        adapter = new UsuarioBusquedaProyectosAdapter(new UsuarioBusquedaProyectosAdapter.Listener() {
            @Override
            public void onProjectClick(UsuarioPropertyListItem item, String query) {
                rememberQuery(query);
                hideKeyboard();
                startActivity(UsuarioPropiedadDetalleActivity.newIntent(
                        UsuarioBusquedaProyectosActivity.this, item.getPropertyId()));
            }

            @Override
            public void onRecentClick(String query) {
                input.setText(query);
                input.setSelection(query.length());
            }

            @Override
            public void onRetryClick() {
                loadCatalog(true);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(layoutManager);
        list.setAdapter(adapter);
        list.setItemAnimator(null);
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy != 0) hideKeyboard();
            }
        });
    }

    private void setupSearchInput() {
        input = findViewById(R.id.inputProjectSearch);
        clearButton = findViewById(R.id.btnClearProjectSearch);
        View back = findViewById(R.id.btnBackProjectSearch);
        if (back != null) back.setOnClickListener(v -> finishAfterTransition());
        if (clearButton != null) clearButton.setOnClickListener(v -> input.setText(""));
        if (input == null) return;

        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable editable) {
                String query = RecentProjectSearchPolicy.clean(editable == null ? "" : editable.toString());
                if (clearButton != null) clearButton.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                scheduleSearch(query);
            }
        });
        input.setOnEditorActionListener((view, actionId, event) -> {
            boolean searchAction = actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            if (!searchAction) return false;
            rememberQuery(currentQuery());
            hideKeyboard();
            return true;
        });
    }

    private void loadCatalog(boolean forceRetry) {
        if (catalogLoading || (catalogLoaded && !forceRetry)) return;
        catalogLoading = true;
        catalogError = "";
        if (!currentQuery().isEmpty()) adapter.showLoading(currentQuery());
        dataRepository.readUserProjectSearchItems(new FirebaseDataRepository.UserProjectSearchCallback() {
            @Override
            public void onSuccess(List<FirebaseDataRepository.UserProjectSearchItem> projects) {
                if (isFinishing() || isDestroyed()) return;
                catalogLoading = false;
                catalogLoaded = true;
                catalogError = "";
                searchCatalog.clear();
                if (projects != null) searchCatalog.addAll(projects);
                scheduleSearch(currentQuery());
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                catalogLoading = false;
                catalogLoaded = false;
                catalogError = message == null || message.trim().isEmpty()
                        ? "No se pudo conectar para buscar proyectos."
                        : message;
                if (!currentQuery().isEmpty()) adapter.showError(currentQuery(), catalogError);
            }
        });
    }

    private void scheduleSearch(String query) {
        if (pendingSearch != null) searchHandler.removeCallbacks(pendingSearch);
        if (query.isEmpty()) {
            showEmptyState();
            return;
        }
        adapter.showLoading(query);
        pendingSearch = () -> executeSearch(query);
        searchHandler.postDelayed(pendingSearch, SEARCH_DEBOUNCE_MS);
    }

    private void executeSearch(String scheduledQuery) {
        if (!scheduledQuery.equals(currentQuery())) return;
        if (catalogLoading) {
            adapter.showLoading(scheduledQuery);
            return;
        }
        if (!catalogLoaded) {
            adapter.showError(scheduledQuery, catalogError);
            return;
        }
        List<FirebaseDataRepository.UserProjectSearchItem> matches = ProjectSearchPolicy.filter(
                searchCatalog,
                scheduledQuery,
                MAX_SEARCH_RESULTS,
                item -> item.searchableText
        );
        List<UsuarioPropertyListItem> items = new ArrayList<>();
        for (FirebaseDataRepository.UserProjectSearchItem match : matches) items.add(match.project);
        if (items.isEmpty()) adapter.showNoResults(scheduledQuery);
        else adapter.showResults(scheduledQuery, items);
    }

    private void showEmptyState() {
        if (adapter != null && recentStore != null) adapter.showEmpty(recentStore.get());
    }

    private void rememberQuery(String query) {
        String clean = RecentProjectSearchPolicy.clean(query);
        if (clean.isEmpty() || recentStore == null) return;
        recentStore.record(clean);
    }

    private String currentQuery() {
        return input == null ? "" : RecentProjectSearchPolicy.clean(input.getText() == null ? "" : input.getText().toString());
    }

    private void focusAndShowKeyboard() {
        if (input == null) return;
        input.requestFocus();
        input.postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (keyboard != null) keyboard.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        }, 180L);
    }

    private void hideKeyboard() {
        if (input == null) return;
        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

    @Override
    protected void onDestroy() {
        if (pendingSearch != null) searchHandler.removeCallbacks(pendingSearch);
        super.onDestroy();
    }
}
