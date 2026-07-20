package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.example.proyecto_iot.data.ProjectImageLoader;

import java.util.ArrayList;
import java.util.List;

public class UsuarioHomeActivity extends BaseUsuarioActivity {
    private static final int MAX_PROJECT_SUGGESTIONS = 10;

    private final FirebaseDataRepository dataRepository = new FirebaseDataRepository();
    private final List<FirebaseDataRepository.UserProjectSearchItem> searchCatalog = new ArrayList<>();
    private UsuarioProjectSuggestionAdapter suggestionsAdapter;
    private EditText searchInput;
    private View suggestionsCard;
    private View searchProgress;
    private View searchMessageLayout;
    private TextView searchMessage;
    private View searchRetry;
    private RecyclerView suggestionsRecycler;
    private boolean searchCatalogLoading;
    private boolean searchCatalogLoaded;
    private String searchLoadError = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_home);
        setupUserBottomNav(R.id.navUserExplore);
        setupProjectClicks();
        setupHeaderActions();
        setupExploreActions();
        setupProjectSearch();
    }

    private void setupProjectClicks() {
        renderProjectCards(new ArrayList<>());
        dataRepository.readUserPropertyListItems(new FirebaseDataRepository.UserPropertyListCallback() {
            @Override
            public void onSuccess(List<UsuarioPropertyListItem> projects) {
                if (isFinishing() || isDestroyed()) return;
                renderProjectCards(projects);
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                renderProjectCards(new ArrayList<>());
            }
        });
    }

    private void renderProjectCards(List<UsuarioPropertyListItem> properties) {
        bindFeaturedCard(R.id.featuredPrimaryCard, properties, 0);
        bindFeaturedCard(R.id.featuredSecondaryCard, properties, 2);

        bindPopularRow(
                R.id.popularRow1,
                R.id.tvPopularLabel1, R.id.tvPopularTitle1,
                R.id.tvPopularMeta1, R.id.tvPopularPrice1,
                properties, 1
        );
        bindPopularRow(
                R.id.popularRow2,
                R.id.tvPopularLabel2, R.id.tvPopularTitle2,
                R.id.tvPopularMeta2, R.id.tvPopularPrice2,
                properties, 3
        );
    }

    private void bindFeaturedCard(int cardId, List<UsuarioPropertyListItem> properties, int index) {
        View card = findViewById(cardId);
        if (card == null) return;

        if (index < properties.size()) {
            card.setVisibility(View.VISIBLE);
            UsuarioPropertyListItem item = properties.get(index);
            ImageView img = card.findViewWithTag("heroImage");
            if (img == null && card instanceof android.view.ViewGroup) {
                img = findFirstImageView((android.view.ViewGroup) card);
            }
            if (img != null) {
                bindImage(img, item);
            }
            card.setOnClickListener(v -> openPropertyDetail(item));
        } else {
            card.setVisibility(View.GONE);
            card.setOnClickListener(null);
        }
    }

    private void bindPopularRow(int rowId, int labelId, int titleId, int metaId, int priceId,
                                 List<UsuarioPropertyListItem> properties, int index) {
        View row = findViewById(rowId);
        if (row == null) return;

        if (index < properties.size()) {
            row.setVisibility(View.VISIBLE);
            UsuarioPropertyListItem item = properties.get(index);

            setText(labelId, item.getLabel());
            setText(titleId, item.getTitle());
            setText(metaId, item.getLocation());
            setText(priceId, item.getPrice());

            ImageView img = null;
            if (row instanceof android.view.ViewGroup) {
                img = findFirstImageView((android.view.ViewGroup) row);
            }
            if (img != null) {
                bindImage(img, item);
            }

            row.setOnClickListener(v -> openPropertyDetail(item));
        } else {
            row.setVisibility(View.GONE);
            row.setOnClickListener(null);
        }
    }

    private void openPropertyDetail(UsuarioPropertyListItem item) {
        startActivity(UsuarioPropiedadDetalleActivity.newIntent(this, item));
    }

    private void bindImage(ImageView imageView, UsuarioPropertyListItem item) {
        ProjectImageLoader.load(imageView, item.getImageUrl(), item.getImageResId());
    }

    private void setText(int viewId, String value) {
        TextView tv = findViewById(viewId);
        if (tv != null && value != null && !value.isEmpty()) {
            tv.setText(value);
        }
    }

    private ImageView findFirstImageView(android.view.ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof ImageView) return (ImageView) child;
            if (child instanceof android.view.ViewGroup) {
                ImageView found = findFirstImageView((android.view.ViewGroup) child);
                if (found != null) return found;
            }
        }
        return null;
    }

    private void setupHeaderActions() {
        View notifications = findViewById(R.id.btnExploreNotifications);
        if (notifications != null) {
            notifications.setOnClickListener(v ->
                    startActivity(new Intent(this, UsuarioNotificacionesActivity.class)));
        }
    }

    private void setupExploreActions() {
        View seeAll = findViewById(R.id.btnSeeAllProperties);
        if (seeAll != null) {
            seeAll.setOnClickListener(v ->
                    startActivity(new Intent(this, UsuarioPropiedadesListadoActivity.class)));
        }

        View scanQr = findViewById(R.id.btnHomeQrScan);
        if (scanQr != null) {
            scanQr.setOnClickListener(v ->
                    startActivity(new Intent(this, QrScannerActivity.class)));
        }
    }

    private void setupProjectSearch() {
        searchInput = findViewById(R.id.inputHomeProjectSearch);
        suggestionsCard = findViewById(R.id.cardHomeProjectSuggestions);
        searchProgress = findViewById(R.id.progressHomeProjectSearch);
        searchMessageLayout = findViewById(R.id.layoutHomeProjectSearchMessage);
        searchMessage = findViewById(R.id.tvHomeProjectSearchMessage);
        searchRetry = findViewById(R.id.btnHomeProjectSearchRetry);
        suggestionsRecycler = findViewById(R.id.recyclerHomeProjectSuggestions);

        if (searchInput == null || suggestionsCard == null || suggestionsRecycler == null) return;

        suggestionsAdapter = new UsuarioProjectSuggestionAdapter(this::openSearchSuggestion);
        suggestionsRecycler.setLayoutManager(new LinearLayoutManager(this));
        suggestionsRecycler.setAdapter(suggestionsAdapter);

        searchInput.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                loadSearchCatalogIfNeeded();
                renderSearchSuggestions();
            } else if (suggestionsCard != null) {
                suggestionsCard.setVisibility(View.GONE);
            }
        });
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if (searchInput.getText().length() > 0) loadSearchCatalogIfNeeded();
                renderSearchSuggestions();
            }
        });
        if (searchRetry != null) {
            searchRetry.setOnClickListener(v -> loadSearchCatalog(true));
        }
    }

    private void loadSearchCatalogIfNeeded() {
        if (searchCatalogLoading || searchCatalogLoaded) return;
        loadSearchCatalog(false);
    }

    private void loadSearchCatalog(boolean forceRetry) {
        if (searchCatalogLoading || (!forceRetry && searchCatalogLoaded)) return;
        searchCatalogLoading = true;
        searchLoadError = "";
        renderSearchSuggestions();
        dataRepository.readUserProjectSearchItems(new FirebaseDataRepository.UserProjectSearchCallback() {
            @Override
            public void onSuccess(List<FirebaseDataRepository.UserProjectSearchItem> projects) {
                if (isFinishing() || isDestroyed()) return;
                searchCatalogLoading = false;
                searchCatalogLoaded = true;
                searchLoadError = "";
                searchCatalog.clear();
                searchCatalog.addAll(projects);
                renderSearchSuggestions();
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                searchCatalogLoading = false;
                searchCatalogLoaded = false;
                searchLoadError = message == null || message.trim().isEmpty()
                        ? "No se pudieron cargar los proyectos."
                        : message;
                renderSearchSuggestions();
            }
        });
    }

    private void renderSearchSuggestions() {
        if (searchInput == null || suggestionsCard == null || suggestionsAdapter == null) return;
        String query = searchInput.getText() == null ? "" : searchInput.getText().toString();
        if (ProjectSearchPolicy.normalize(query).isEmpty()) {
            suggestionsAdapter.setItems(new ArrayList<>());
            suggestionsCard.setVisibility(View.GONE);
            return;
        }

        suggestionsCard.setVisibility(View.VISIBLE);
        if (searchCatalogLoading) {
            setSearchContentVisibility(View.VISIBLE, View.GONE, View.GONE);
            return;
        }
        if (!searchLoadError.isEmpty()) {
            showSearchMessage(searchLoadError, true);
            return;
        }

        List<FirebaseDataRepository.UserProjectSearchItem> matchingItems = ProjectSearchPolicy.filter(
                searchCatalog,
                query,
                MAX_PROJECT_SUGGESTIONS,
                item -> item.searchableText
        );
        List<UsuarioPropertyListItem> suggestions = new ArrayList<>();
        for (FirebaseDataRepository.UserProjectSearchItem item : matchingItems) {
            suggestions.add(item.project);
        }
        if (suggestions.isEmpty()) {
            showSearchMessage("No encontramos proyectos que coincidan con tu búsqueda.", false);
            return;
        }
        suggestionsAdapter.setItems(suggestions);
        setSearchContentVisibility(View.GONE, View.GONE, View.VISIBLE);
    }

    private void showSearchMessage(String message, boolean showRetry) {
        if (searchMessage != null) searchMessage.setText(message);
        if (searchRetry != null) searchRetry.setVisibility(showRetry ? View.VISIBLE : View.GONE);
        setSearchContentVisibility(View.GONE, View.VISIBLE, View.GONE);
    }

    private void setSearchContentVisibility(int progress, int message, int suggestions) {
        if (searchProgress != null) searchProgress.setVisibility(progress);
        if (searchMessageLayout != null) searchMessageLayout.setVisibility(message);
        if (suggestionsRecycler != null) suggestionsRecycler.setVisibility(suggestions);
    }

    private void openSearchSuggestion(UsuarioPropertyListItem item) {
        if (item == null || item.getPropertyId() == null || item.getPropertyId().trim().isEmpty()) return;
        searchInput.clearFocus();
        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        startActivity(UsuarioPropiedadDetalleActivity.newIntent(this, item.getPropertyId()));
    }
}
