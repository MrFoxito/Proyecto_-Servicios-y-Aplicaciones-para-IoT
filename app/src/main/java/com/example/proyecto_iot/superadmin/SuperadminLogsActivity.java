package com.example.proyecto_iot.superadmin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SuperadminLogsActivity extends BaseSuperadminActivity {

    private static final Locale ES_LOCALE = SuperadminRangeFilterHelper.ES_LOCALE;

    private final List<SuperadminLogEntryItem> allLogs = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView dateFilterText;
    private String severityFilter = "all";
    private String userFilter = "Todos";
    private SuperadminRangeFilterHelper.DateRange currentRange =
            SuperadminRangeFilterHelper.presetRange(SuperadminRangeFilterHelper.Preset.DAYS, 60);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_logs);
        setupCommonNavigation();

        recyclerView = findViewById(R.id.recyclerLogs);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        setupDateFilter();
        setupLogFilters();
        loadAndRenderLogs();

        android.view.View btnDownload = findViewById(R.id.btnDownloadLogs);
        if (btnDownload != null) {
            btnDownload.setOnClickListener(v -> {
                android.widget.Toast.makeText(this, "Descargando registros...", android.widget.Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAndRenderLogs();
    }

    private void setupDateFilter() {
        dateFilterText = findViewById(R.id.textLogsDateFilter);
        updateDateFilterLabel();

        View dateFilter = findViewById(R.id.layoutLogsDateFilter);
        if (dateFilter != null) {
            dateFilter.setOnClickListener(view -> openCustomDateRangePicker());
        }
    }

    private void setupLogFilters() {
        View userFilterLayout = findViewById(R.id.layoutLogsUserFilter);
        TextView userFilterText = findViewById(R.id.textLogsUserFilterValue);
        if (userFilterLayout != null) {
            userFilterLayout.setOnClickListener(view -> {
                android.widget.PopupMenu menu = new android.widget.PopupMenu(this, view);
                menu.getMenu().add("Todos");
                
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("usuarios").get().addOnSuccessListener(snapshot -> {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                        String nombres = doc.getString("nombres");
                        if (nombres == null) nombres = doc.getString("nombre");
                        String apellidos = doc.getString("apellidos");
                        String name = (nombres != null ? nombres : "") + (apellidos != null ? " " + apellidos : "");
                        if (!name.trim().isEmpty()) {
                            menu.getMenu().add(name.trim());
                        }
                    }
                    menu.setOnMenuItemClickListener(item -> {
                        userFilter = item.getTitle().toString();
                        if (userFilterText != null) {
                            userFilterText.setText(userFilter);
                        }
                        renderFilteredLogs();
                        return true;
                    });
                    menu.show();
                });
            });
        }

        TextView chipAll = findViewById(R.id.chipLogsAll);
        TextView chipCritical = findViewById(R.id.chipLogsCritical);
        TextView chipAlerts = findViewById(R.id.chipLogsAlerts);
        TextView chipInfo = findViewById(R.id.chipLogsInfo);
        TextView chipSuccess = findViewById(R.id.chipLogsSuccess);

        if (chipAll != null && chipCritical != null && chipAlerts != null && chipInfo != null && chipSuccess != null) {
            setActiveChip(chipAll, chipCritical, chipAlerts, chipInfo, chipSuccess);
            
            chipAll.setOnClickListener(view -> {
                severityFilter = "all";
                setActiveChip(chipAll, chipCritical, chipAlerts, chipInfo, chipSuccess);
                renderFilteredLogs();
            });
            chipCritical.setOnClickListener(view -> {
                severityFilter = "critico";
                setActiveChip(chipCritical, chipAll, chipAlerts, chipInfo, chipSuccess);
                renderFilteredLogs();
            });
            chipAlerts.setOnClickListener(view -> {
                severityFilter = "alerta";
                setActiveChip(chipAlerts, chipAll, chipCritical, chipInfo, chipSuccess);
                renderFilteredLogs();
            });
            chipInfo.setOnClickListener(view -> {
                severityFilter = "info";
                setActiveChip(chipInfo, chipAll, chipCritical, chipAlerts, chipSuccess);
                renderFilteredLogs();
            });
            chipSuccess.setOnClickListener(view -> {
                severityFilter = "exito";
                setActiveChip(chipSuccess, chipAll, chipCritical, chipAlerts, chipInfo);
                renderFilteredLogs();
            });
        }
    }

    private int getLogColor(String nivel) {
        if ("critico".equalsIgnoreCase(nivel)) return android.graphics.Color.parseColor("#DC2626");
        if ("alerta".equalsIgnoreCase(nivel)) return android.graphics.Color.parseColor("#EAB308");
        return android.graphics.Color.parseColor("#0F172A");
    }

    private int getLogIcon(String tipo) {
        if ("sesion".equalsIgnoreCase(tipo)) return android.R.drawable.ic_menu_recent_history;
        if ("registro".equalsIgnoreCase(tipo)) return android.R.drawable.ic_menu_add;
        return android.R.drawable.ic_menu_info_details;
    }

    private void loadAndRenderLogs() {
        com.google.firebase.firestore.FirebaseFirestore firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        firestore.collection("logs_sistema").get().addOnSuccessListener(snapshot -> {
            allLogs.clear();
            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                android.util.Log.e("FIRESTORE_DUMP", "Doc: " + doc.getId() + " => " + doc.getData());
                String nivel = doc.getString("nivel");
                if (nivel == null) nivel = "info";
                int color = getLogColor(nivel);
                
                String tipo = doc.getString("tipo");
                if (tipo == null) tipo = "sistema";
                String fecha = doc.getString("fecha");
                if (fecha == null) fecha = doc.getString("dateIso");
                if (fecha == null || fecha.trim().isEmpty()) {
                    String id = doc.getId();
                    if (id != null && id.startsWith("log_")) {
                        try {
                            long millis = Long.parseLong(id.substring(4));
                            fecha = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(new Date(millis));
                        } catch (NumberFormatException ignored) {}
                    }
                }
                if (fecha == null) fecha = "";

                allLogs.add(new SuperadminLogEntryItem(
                        color,
                        getLogIcon(tipo),
                        color,
                        fecha,
                        doc.getString("titulo") != null ? doc.getString("titulo") : "",
                        doc.getString("subtitulo") != null ? doc.getString("subtitulo") : "",
                        doc.getString("tiempo") != null ? doc.getString("tiempo") : "",
                        doc.getString("detalle") != null ? doc.getString("detalle") : "",
                        nivel.toUpperCase(Locale.ROOT),
                        color
                ));
            }
            renderFilteredLogs();
        });
    }

    private void renderFilteredLogs() {
        if (recyclerView == null) {
            return;
        }

        List<SuperadminLogEntryItem> filtered = new ArrayList<>();
        for (SuperadminLogEntryItem item : allLogs) {
            if (!SuperadminRangeFilterHelper.withinIsoRange(item.getDateIso(), currentRange)) {
                continue;
            }
            if (!matchesSeverity(item)) {
                continue;
            }
            if (!matchesUser(item)) {
                continue;
            }
            filtered.add(item);
        }

        recyclerView.setAdapter(new SuperadminLogEntryAdapter(filtered));
    }

    private boolean matchesUser(SuperadminLogEntryItem item) {
        if ("Todos".equals(userFilter)) {
            return true;
        }
        String query = userFilter.toLowerCase(Locale.ROOT);
        return (item.getTitle() != null && item.getTitle().toLowerCase(Locale.ROOT).contains(query)) ||
               (item.getSubtitle() != null && item.getSubtitle().toLowerCase(Locale.ROOT).contains(query)) ||
               (item.getDetail() != null && item.getDetail().toLowerCase(Locale.ROOT).contains(query)) ||
               (item.getTime() != null && item.getTime().toLowerCase(Locale.ROOT).contains(query));
    }

    private boolean matchesSeverity(SuperadminLogEntryItem item) {
        if ("all".equals(severityFilter)) {
            return true;
        }
        return severityFilter.equalsIgnoreCase(item.getStatus());
    }

    private void openCustomDateRangePicker() {
        Calendar start = Calendar.getInstance();
        start.setTime(currentRange.start);
        Calendar end = Calendar.getInstance();
        end.setTime(currentRange.end);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = dpToPx(20);
        container.setPadding(padding, padding, padding, 0);

        TextView startValue = createRangeDateRow(container, "Fecha inicio", start.getTime());
        TextView endValue = createRangeDateRow(container, "Fecha fin", end.getTime());

        startValue.setOnClickListener(v -> showLocalizedDatePicker(start.getTime(), date -> {
            start.setTime(date);
            startValue.setText(SuperadminRangeFilterHelper.formatDate(date));
        }));
        endValue.setOnClickListener(v -> showLocalizedDatePicker(end.getTime(), date -> {
            end.setTime(date);
            endValue.setText(SuperadminRangeFilterHelper.formatDate(date));
        }));

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.sa_custom_range_picker_title))
                .setView(container)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aplicar", (dialog, which) -> {
                    currentRange = SuperadminRangeFilterHelper.normalize(start.getTime(), end.getTime());
                    updateDateFilterLabel();
                    renderFilteredLogs();
                })
                .show();
    }

    private TextView createRangeDateRow(LinearLayout container, String label, Date initialValue) {
        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextColor(ContextCompat.getColor(this, R.color.sa_text_secondary));
        labelView.setTextSize(13f);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        labelParams.topMargin = dpToPx(12);
        container.addView(labelView, labelParams);

        TextView valueView = new TextView(this);
        valueView.setText(SuperadminRangeFilterHelper.formatDate(initialValue));
        valueView.setTextColor(ContextCompat.getColor(this, R.color.sa_text_primary));
        valueView.setTextSize(16f);
        valueView.setBackgroundResource(R.drawable.bg_search_bar);
        valueView.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));
        valueView.setClickable(true);
        valueView.setFocusable(true);
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        valueParams.topMargin = dpToPx(4);
        container.addView(valueView, valueParams);

        return valueView;
    }

    private void showLocalizedDatePicker(Date initialDate, OnRangeDateSelected callback) {
        Calendar initial = Calendar.getInstance();
        initial.setTime(initialDate);

        Locale previousLocale = Locale.getDefault();
        Locale.setDefault(ES_LOCALE);
        DatePickerDialog dialog = new DatePickerDialog(
                createSpanishContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth, 0, 0, 0);
                    selected.set(Calendar.MILLISECOND, 0);
                    callback.onDateSelected(selected.getTime());
                },
                initial.get(Calendar.YEAR),
                initial.get(Calendar.MONTH),
                initial.get(Calendar.DAY_OF_MONTH)
        );
        dialog.setOnDismissListener(d -> Locale.setDefault(previousLocale));
        dialog.show();
    }

    private interface OnRangeDateSelected {
        void onDateSelected(Date date);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void updateDateFilterLabel() {
        if (dateFilterText != null) {
            dateFilterText.setText(SuperadminRangeFilterHelper.formatRange(currentRange));
        }
    }

    private void setActiveChip(TextView active, TextView... inactives) {
        active.setBackgroundResource(R.drawable.sa_chip_active);
        active.setTextColor(ContextCompat.getColor(this, R.color.white));
        for (TextView inactive : inactives) {
            inactive.setBackgroundResource(R.drawable.sa_chip_inactive);
            inactive.setTextColor(ContextCompat.getColor(this, R.color.app_chip_inactive_text));
        }
    }

    private Context createSpanishContext() {
        Configuration configuration = new Configuration(getResources().getConfiguration());
        configuration.setLocale(ES_LOCALE);
        Context localizedContext = createConfigurationContext(configuration);
        return new ContextThemeWrapper(this, 0) {
            private Resources.Theme localizedTheme;

            @Override
            public Resources getResources() {
                return localizedContext.getResources();
            }

            @Override
            public Resources.Theme getTheme() {
                if (localizedTheme == null) {
                    localizedTheme = getResources().newTheme();
                    localizedTheme.setTo(SuperadminLogsActivity.this.getTheme());
                }
                return localizedTheme;
            }
        };
    }
}
