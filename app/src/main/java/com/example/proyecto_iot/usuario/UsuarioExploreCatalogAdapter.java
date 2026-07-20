package com.example.proyecto_iot.usuario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.ProjectImageLoader;

import java.util.ArrayList;
import java.util.List;

/** The single vertical scroll owner for the client Explore content. */
public class UsuarioExploreCatalogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_FEATURED = 1;
    private static final int TYPE_CATALOG_HEADER = 2;
    private static final int TYPE_PROJECT = 3;
    private static final int TYPE_SKELETON = 4;
    private static final int TYPE_STATE = 5;
    private static final int INITIAL_SKELETON_COUNT = 3;

    public interface Listener {
        void onProjectClick(UsuarioPropertyListItem item);
        void onSeeAllClick();
        void onRetryClick();
    }

    private final Listener listener;
    private final UsuarioExploreFeaturedAdapter featuredAdapter;
    private final List<UsuarioPropertyListItem> projects = new ArrayList<>();
    private final List<UsuarioPropertyListItem> featured = new ArrayList<>();
    private boolean initialLoading = true;
    private boolean loadingMore;
    private boolean endReached;
    private String errorMessage = "";

    public UsuarioExploreCatalogAdapter(Listener listener) {
        this.listener = listener;
        featuredAdapter = new UsuarioExploreFeaturedAdapter(listener::onProjectClick);
    }

    public void showInitialLoading() {
        initialLoading = true;
        loadingMore = false;
        endReached = false;
        errorMessage = "";
        projects.clear();
        featured.clear();
        featuredAdapter.setItems(featured, true);
        notifyDataSetChanged();
    }

    public void showInitialError(String message) {
        initialLoading = false;
        loadingMore = false;
        errorMessage = safeMessage(message);
        featuredAdapter.setItems(featured, false);
        notifyDataSetChanged();
    }

    public void showFirstPage(List<UsuarioPropertyListItem> page, boolean hasMore) {
        initialLoading = false;
        loadingMore = false;
        errorMessage = "";
        endReached = !hasMore;
        projects.clear();
        if (page != null) projects.addAll(page);
        featured.clear();
        for (int i = 0; i < Math.min(5, projects.size()); i++) featured.add(projects.get(i));
        featuredAdapter.setItems(featured, false);
        notifyDataSetChanged();
    }

    public void showNextPageLoading() {
        if (initialLoading || loadingMore || endReached) return;
        loadingMore = true;
        errorMessage = "";
        notifyItemChanged(2 + projects.size());
    }

    public void appendPage(List<UsuarioPropertyListItem> page, boolean hasMore) {
        loadingMore = false;
        errorMessage = "";
        endReached = !hasMore;
        int start = projects.size();
        if (page != null) projects.addAll(page);
        if (page != null && !page.isEmpty()) {
            notifyItemRangeInserted(2 + start, page.size());
        }
        notifyItemChanged(2 + projects.size());
    }

    public void showNextPageError(String message) {
        loadingMore = false;
        errorMessage = safeMessage(message);
        notifyItemChanged(2 + projects.size());
    }

    public boolean isLoading() {
        return initialLoading || loadingMore;
    }

    public boolean canLoadMore() {
        return !initialLoading && !loadingMore && !endReached && errorMessage.isEmpty();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return TYPE_FEATURED;
        if (position == 1) return TYPE_CATALOG_HEADER;
        int projectStart = 2;
        if (initialLoading) return TYPE_SKELETON;
        if (projects.isEmpty()) return TYPE_STATE;
        if (position < projectStart + projects.size()) return TYPE_PROJECT;
        return TYPE_STATE;
    }

    @Override
    public int getItemCount() {
        if (initialLoading) return 2 + INITIAL_SKELETON_COUNT;
        if (projects.isEmpty()) return 3;
        return 2 + projects.size() + 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_FEATURED) {
            return new FeaturedSectionHolder(inflater.inflate(
                    R.layout.item_usuario_explore_featured_section, parent, false));
        }
        if (viewType == TYPE_CATALOG_HEADER) {
            return new SimpleHolder(inflater.inflate(
                    R.layout.item_usuario_explore_catalog_header, parent, false));
        }
        if (viewType == TYPE_PROJECT) {
            return new ProjectHolder(inflater.inflate(
                    R.layout.item_usuario_explore_project, parent, false));
        }
        if (viewType == TYPE_SKELETON) {
            return new SimpleHolder(inflater.inflate(
                    R.layout.item_usuario_explore_skeleton, parent, false));
        }
        return new StateHolder(inflater.inflate(R.layout.item_usuario_explore_state, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder rawHolder, int position) {
        int type = getItemViewType(position);
        if (type == TYPE_FEATURED) {
            FeaturedSectionHolder holder = (FeaturedSectionHolder) rawHolder;
            if (holder.recycler.getAdapter() == null) {
                holder.recycler.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext(),
                        LinearLayoutManager.HORIZONTAL, false));
                holder.recycler.setAdapter(featuredAdapter);
            }
            holder.seeAll.setOnClickListener(v -> listener.onSeeAllClick());
            return;
        }
        if (type == TYPE_PROJECT) {
            bindProject((ProjectHolder) rawHolder, projects.get(position - 2));
            return;
        }
        if (type == TYPE_STATE) bindState((StateHolder) rawHolder);
    }

    private void bindProject(ProjectHolder holder, UsuarioPropertyListItem item) {
        ProjectImageLoader.load(holder.image, item.getImageUrl(), item.getImageResId());
        holder.title.setText(item.getTitle());
        String location = item.getExploreLocation();
        holder.location.setText(location.isEmpty() ? "Ubicación por definir" : location);
        holder.price.setText(item.getPrice());
        String summary = item.getTypologiesSummary();
        holder.summary.setText(summary);
        holder.summary.setVisibility(summary == null || summary.trim().isEmpty() ? View.GONE : View.VISIBLE);
        String badge = item.getLabel();
        holder.badge.setText(badge);
        holder.badge.setVisibility(badge == null || badge.trim().isEmpty() ? View.GONE : View.VISIBLE);
        holder.itemView.setOnClickListener(v -> listener.onProjectClick(item));
    }

    private void bindState(StateHolder holder) {
        holder.progress.setVisibility(View.GONE);
        holder.message.setVisibility(View.GONE);
        holder.retry.setVisibility(View.GONE);
        if (loadingMore) {
            holder.progress.setVisibility(View.VISIBLE);
            return;
        }
        if (!errorMessage.isEmpty()) {
            holder.message.setText(errorMessage);
            holder.message.setVisibility(View.VISIBLE);
            holder.retry.setVisibility(View.VISIBLE);
            holder.retry.setOnClickListener(v -> listener.onRetryClick());
            return;
        }
        holder.message.setText(projects.isEmpty()
                ? "Aún no hay proyectos disponibles."
                : "Ya viste todos los proyectos disponibles.");
        holder.message.setVisibility(View.VISIBLE);
    }

    private String safeMessage(String message) {
        return message == null || message.trim().isEmpty()
                ? "No se pudieron cargar los proyectos."
                : message;
    }

    static class SimpleHolder extends RecyclerView.ViewHolder {
        SimpleHolder(@NonNull View itemView) { super(itemView); }
    }

    static class FeaturedSectionHolder extends RecyclerView.ViewHolder {
        final RecyclerView recycler;
        final TextView seeAll;

        FeaturedSectionHolder(@NonNull View itemView) {
            super(itemView);
            recycler = itemView.findViewById(R.id.recyclerExploreFeatured);
            seeAll = itemView.findViewById(R.id.btnExploreSeeAll);
        }
    }

    static class ProjectHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView badge;
        final TextView title;
        final TextView location;
        final TextView price;
        final TextView summary;

        ProjectHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ivExploreProjectImage);
            badge = itemView.findViewById(R.id.tvExploreProjectBadge);
            title = itemView.findViewById(R.id.tvExploreProjectTitle);
            location = itemView.findViewById(R.id.tvExploreProjectLocation);
            price = itemView.findViewById(R.id.tvExploreProjectPrice);
            summary = itemView.findViewById(R.id.tvExploreProjectSummary);
        }
    }

    static class StateHolder extends RecyclerView.ViewHolder {
        final ProgressBar progress;
        final TextView message;
        final TextView retry;

        StateHolder(@NonNull View itemView) {
            super(itemView);
            progress = itemView.findViewById(R.id.progressExploreState);
            message = itemView.findViewById(R.id.tvExploreState);
            retry = itemView.findViewById(R.id.btnExploreStateRetry);
        }
    }
}
