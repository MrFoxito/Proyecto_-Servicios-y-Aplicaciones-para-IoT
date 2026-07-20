package com.example.proyecto_iot.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseDataRepository;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class UsuarioHomeActivity extends BaseUsuarioActivity {
    private static final int EXPLORE_PAGE_SIZE = 5;

    private final FirebaseDataRepository dataRepository = new FirebaseDataRepository();
    private RecyclerView exploreRecycler;
    private UsuarioExploreCatalogAdapter exploreAdapter;
    private DocumentSnapshot exploreNextCursor;
    private boolean exploreInitialLoadCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_home);
        setupUserBottomNav(R.id.navUserExplore);
        setupExploreCatalog();
        setupHeaderActions();
        setupExploreActions();
        setupProjectSearchEntry();
    }

    private void setupExploreCatalog() {
        exploreRecycler = findViewById(R.id.recyclerHomeExploreCatalog);
        if (exploreRecycler == null) return;
        exploreAdapter = new UsuarioExploreCatalogAdapter(new UsuarioExploreCatalogAdapter.Listener() {
            @Override
            public void onProjectClick(UsuarioPropertyListItem item) {
                openPropertyDetail(item);
            }

            @Override
            public void onSeeAllClick() {
                startActivity(new Intent(UsuarioHomeActivity.this, UsuarioPropiedadesListadoActivity.class));
            }

            @Override
            public void onRetryClick() {
                if (exploreInitialLoadCompleted) loadNextExplorePage();
                else loadInitialExplorePage();
            }
        });
        exploreRecycler.setLayoutManager(new LinearLayoutManager(this));
        exploreRecycler.setAdapter(exploreAdapter);
        exploreRecycler.setItemAnimator(null);
        exploreRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy <= 0 || exploreAdapter == null || !exploreAdapter.canLoadMore()) return;
                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                if (!(manager instanceof LinearLayoutManager)) return;
                int lastVisible = ((LinearLayoutManager) manager).findLastVisibleItemPosition();
                if (ExploreProjectPresentationPolicy.shouldLoadMore(
                        exploreAdapter.isLoading(), exploreAdapter.canLoadMore(),
                        lastVisible, exploreAdapter.getItemCount())) {
                    loadNextExplorePage();
                }
            }
        });
        loadInitialExplorePage();
    }

    private void loadInitialExplorePage() {
        if (exploreAdapter == null) return;
        exploreAdapter.showInitialLoading();
        exploreNextCursor = null;
        exploreInitialLoadCompleted = false;
        dataRepository.readUserExplorePage(null, EXPLORE_PAGE_SIZE,
                new FirebaseDataRepository.UserExplorePageCallback() {
            @Override
            public void onSuccess(FirebaseDataRepository.UserExplorePage page) {
                if (isFinishing() || isDestroyed()) return;
                exploreInitialLoadCompleted = true;
                exploreNextCursor = page.nextCursor;
                exploreAdapter.showFirstPage(page.projects, page.hasMore);
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                exploreAdapter.showInitialError(message);
            }
        });
    }

    private void loadNextExplorePage() {
        if (exploreAdapter == null || !exploreAdapter.canLoadMore() || exploreNextCursor == null) return;
        exploreAdapter.showNextPageLoading();
        dataRepository.readUserExplorePage(exploreNextCursor, EXPLORE_PAGE_SIZE,
                new FirebaseDataRepository.UserExplorePageCallback() {
            @Override
            public void onSuccess(FirebaseDataRepository.UserExplorePage page) {
                if (isFinishing() || isDestroyed()) return;
                exploreNextCursor = page.nextCursor;
                exploreAdapter.appendPage(page.projects, page.hasMore);
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                exploreAdapter.showNextPageError(message);
            }
        });
    }

    private void openPropertyDetail(UsuarioPropertyListItem item) {
        if (item == null || item.getPropertyId() == null || item.getPropertyId().trim().isEmpty()) return;
        startActivity(UsuarioPropiedadDetalleActivity.newIntent(this, item));
    }

    private void setupHeaderActions() {
        View notifications = findViewById(R.id.btnExploreNotifications);
        if (notifications != null) notifications.setOnClickListener(v ->
                startActivity(new Intent(this, UsuarioNotificacionesActivity.class)));
    }

    private void setupExploreActions() {
        View scanQr = findViewById(R.id.btnHomeQrScan);
        if (scanQr != null) scanQr.setOnClickListener(v ->
                startActivity(new Intent(this, QrScannerActivity.class)));
    }

    private void setupProjectSearchEntry() {
        View searchBar = findViewById(R.id.cardHomeProjectSearch);
        if (searchBar == null) return;
        View.OnClickListener listener = v -> openProjectSearch(searchBar);
        searchBar.setOnClickListener(listener);
        View hint = findViewById(R.id.inputHomeProjectSearch);
        if (hint != null) hint.setOnClickListener(listener);
    }

    private void openProjectSearch(View sharedSearchBar) {
        Intent intent = new Intent(this, UsuarioBusquedaProyectosActivity.class);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, sharedSearchBar, "project_search_bar"
        );
        startActivity(intent, options.toBundle());
    }
}
