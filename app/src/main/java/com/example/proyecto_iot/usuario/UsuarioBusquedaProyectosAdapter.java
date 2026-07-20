package com.example.proyecto_iot.usuario;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.ProjectImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UsuarioBusquedaProyectosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_RESULT = 1;
    private static final int TYPE_RECENT_HEADER = 2;
    private static final int TYPE_RECENT = 3;
    private static final int TYPE_STATE = 4;

    public interface Listener {
        void onProjectClick(UsuarioPropertyListItem item, String query);
        void onRecentClick(String query);
        void onRetryClick();
    }

    private enum Mode { EMPTY, LOADING, RESULTS, NO_RESULTS, ERROR }

    private final Listener listener;
    private final List<UsuarioPropertyListItem> results = new ArrayList<>();
    private final List<String> recentQueries = new ArrayList<>();
    private Mode mode = Mode.EMPTY;
    private String query = "";
    private String errorMessage = "";

    public UsuarioBusquedaProyectosAdapter(Listener listener) {
        this.listener = listener;
    }

    public void showEmpty(List<String> recent) {
        mode = Mode.EMPTY;
        query = "";
        errorMessage = "";
        results.clear();
        recentQueries.clear();
        if (recent != null) recentQueries.addAll(recent);
        notifyDataSetChanged();
    }

    public void showLoading(String currentQuery) {
        mode = Mode.LOADING;
        query = currentQuery == null ? "" : currentQuery;
        results.clear();
        notifyDataSetChanged();
    }

    public void showResults(String currentQuery, List<UsuarioPropertyListItem> items) {
        mode = Mode.RESULTS;
        query = currentQuery == null ? "" : currentQuery;
        errorMessage = "";
        results.clear();
        if (items != null) results.addAll(items);
        notifyDataSetChanged();
    }

    public void showNoResults(String currentQuery) {
        mode = Mode.NO_RESULTS;
        query = currentQuery == null ? "" : currentQuery;
        results.clear();
        notifyDataSetChanged();
    }

    public void showError(String currentQuery, String message) {
        mode = Mode.ERROR;
        query = currentQuery == null ? "" : currentQuery;
        errorMessage = message == null || message.trim().isEmpty()
                ? "No se pudieron cargar los proyectos."
                : message;
        results.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (mode == Mode.RESULTS) return TYPE_RESULT;
        if (mode == Mode.EMPTY && !recentQueries.isEmpty()) {
            return position == 0 ? TYPE_RECENT_HEADER : TYPE_RECENT;
        }
        return TYPE_STATE;
    }

    @Override
    public int getItemCount() {
        if (mode == Mode.RESULTS) return results.size();
        if (mode == Mode.EMPTY && !recentQueries.isEmpty()) return recentQueries.size() + 1;
        return 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_RESULT) return new ResultHolder(inflater.inflate(
                R.layout.item_usuario_busqueda_resultado, parent, false));
        if (viewType == TYPE_RECENT_HEADER) return new SimpleHolder(inflater.inflate(
                R.layout.item_usuario_busqueda_recent_header, parent, false));
        if (viewType == TYPE_RECENT) return new RecentHolder(inflater.inflate(
                R.layout.item_usuario_busqueda_recent, parent, false));
        return new StateHolder(inflater.inflate(R.layout.item_usuario_busqueda_state, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder rawHolder, int position) {
        int type = getItemViewType(position);
        if (type == TYPE_RESULT) {
            bindResult((ResultHolder) rawHolder, results.get(position));
        } else if (type == TYPE_RECENT) {
            String recent = recentQueries.get(position - 1);
            RecentHolder holder = (RecentHolder) rawHolder;
            holder.query.setText(recent);
            holder.itemView.setOnClickListener(v -> listener.onRecentClick(recent));
        } else if (type == TYPE_STATE) {
            bindState((StateHolder) rawHolder);
        }
    }

    private void bindResult(ResultHolder holder, UsuarioPropertyListItem item) {
        ProjectImageLoader.load(holder.image, item.getImageUrl(), item.getImageResId());
        holder.title.setText(highlight(holder.title, item.getTitle(), query));
        holder.address.setText(highlight(holder.address, item.getLocation(), query));
        holder.itemView.setOnClickListener(v -> listener.onProjectClick(item, query));
    }

    private CharSequence highlight(TextView target, String value, String needle) {
        String text = value == null ? "" : value;
        String queryText = RecentProjectSearchPolicy.clean(needle);
        if (queryText.isEmpty()) return text;
        int start = text.toLowerCase(Locale.ROOT).indexOf(queryText.toLowerCase(Locale.ROOT));
        if (start < 0) return text;
        SpannableString result = new SpannableString(text);
        int end = Math.min(text.length(), start + queryText.length());
        int color = ContextCompat.getColor(target.getContext(), R.color.explore_accent);
        result.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        result.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return result;
    }

    private void bindState(StateHolder holder) {
        holder.progress.setVisibility(View.GONE);
        holder.message.setVisibility(View.VISIBLE);
        holder.retry.setVisibility(View.GONE);
        switch (mode) {
            case LOADING:
                holder.message.setText("Buscando proyectos…");
                holder.progress.setVisibility(View.VISIBLE);
                break;
            case NO_RESULTS:
                holder.message.setText("No encontramos proyectos para “" + query + "”.");
                break;
            case ERROR:
                holder.message.setText(errorMessage);
                holder.retry.setVisibility(View.VISIBLE);
                holder.retry.setOnClickListener(v -> listener.onRetryClick());
                break;
            case EMPTY:
            default:
                holder.message.setText("Busca por proyecto, distrito, dirección o característica.");
                break;
        }
    }

    static class SimpleHolder extends RecyclerView.ViewHolder {
        SimpleHolder(@NonNull View itemView) { super(itemView); }
    }

    static class ResultHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView title;
        final TextView address;
        ResultHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ivProjectSearchResult);
            title = itemView.findViewById(R.id.tvProjectSearchResultTitle);
            address = itemView.findViewById(R.id.tvProjectSearchResultAddress);
        }
    }

    static class RecentHolder extends RecyclerView.ViewHolder {
        final TextView query;
        RecentHolder(@NonNull View itemView) {
            super(itemView);
            query = itemView.findViewById(R.id.tvRecentSearchQuery);
        }
    }

    static class StateHolder extends RecyclerView.ViewHolder {
        final ProgressBar progress;
        final TextView message;
        final TextView retry;
        StateHolder(@NonNull View itemView) {
            super(itemView);
            progress = itemView.findViewById(R.id.progressProjectSearchState);
            message = itemView.findViewById(R.id.tvProjectSearchState);
            retry = itemView.findViewById(R.id.btnProjectSearchRetry);
        }
    }
}
