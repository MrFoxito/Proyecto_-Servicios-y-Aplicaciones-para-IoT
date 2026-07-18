package com.example.proyecto_iot.superadmin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SuperadminReportesUsuariosActivity extends BaseSuperadminActivity {

    private static final Locale ES_LOCALE = SuperadminRangeFilterHelper.ES_LOCALE;

    private TextView dateFilterText;
    private TextView activeUsersValue;
    private TextView activeUsersGrowthValue;
    private TextView newRegistrationsValue;
    private TextView newRegistrationsGrowthValue;
    
    private TextView roleAdminLabel;
    private TextView roleAsesorLabel;
    private TextView roleClienteLabel;
    
    private View barRoleAdmin, spaceRoleAdmin;
    private View barRoleAsesor, spaceRoleAsesor;
    private View barRoleCliente, spaceRoleCliente;

    private int activeChipId = R.id.chipRange1y;
    private final int customChipId = R.id.chipRangeCustom;
    private SuperadminRangeFilterHelper.DateRange currentRange =
            SuperadminRangeFilterHelper.presetRange(SuperadminRangeFilterHelper.Preset.YEARS, 1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_reportes_usuarios);
        setupCommonNavigation();
        bindViews();
        setupDateFilter();
        setupRangeChips();
        renderMetrics();
    }

    private void bindViews() {
        dateFilterText = findViewById(R.id.textReportesDateFilter);
        activeUsersValue = findViewById(R.id.tvUsersActiveValue);
        activeUsersGrowthValue = findViewById(R.id.tvUsersActiveGrowthValue);
        newRegistrationsValue = findViewById(R.id.tvUsersNewRegistrationsValue);
        newRegistrationsGrowthValue = findViewById(R.id.tvUsersNewRegistrationsGrowthValue);

        roleAdminLabel = findViewById(R.id.tvRoleAdminLabel);
        barRoleAdmin = findViewById(R.id.barRoleAdmin);
        spaceRoleAdmin = findViewById(R.id.spaceRoleAdmin);
        
        roleAsesorLabel = findViewById(R.id.tvRoleAsesorLabel);
        barRoleAsesor = findViewById(R.id.barRoleAsesor);
        spaceRoleAsesor = findViewById(R.id.spaceRoleAsesor);
        
        roleClienteLabel = findViewById(R.id.tvRoleClienteLabel);
        barRoleCliente = findViewById(R.id.barRoleCliente);
        spaceRoleCliente = findViewById(R.id.spaceRoleCliente);
    }

    private void setupDateFilter() {
        updateDateFilterLabel();
        View dateFilter = findViewById(R.id.layoutReportesDateFilter);
        if (dateFilter != null) {
            attachCustomRangeTrigger(dateFilter);
        }
    }

    private void setupRangeChips() {
        bindRangeChip(R.id.chipRange7d, SuperadminRangeFilterHelper.Preset.DAYS, 7);
        bindRangeChip(R.id.chipRange1m, SuperadminRangeFilterHelper.Preset.MONTHS, 1);
        bindRangeChip(R.id.chipRange3m, SuperadminRangeFilterHelper.Preset.MONTHS, 3);
        bindRangeChip(R.id.chipRange1y, SuperadminRangeFilterHelper.Preset.YEARS, 1);

        View customChip = findViewById(R.id.chipRangeCustom);
        if (customChip != null) {
            attachCustomRangeTrigger(customChip);
        }

        updateChipStates(activeChipId);
    }

    private void bindRangeChip(int viewId, SuperadminRangeFilterHelper.Preset preset, int amount) {
        View chip = findViewById(viewId);
        if (chip == null) {
            return;
        }
        chip.setOnClickListener(view -> {
            activeChipId = viewId;
            currentRange = SuperadminRangeFilterHelper.presetRange(preset, amount);
            updateChipStates(activeChipId);
            updateDateFilterLabel();
            renderMetrics();
        });
    }

    private void openCustomDateRangePicker() {
        activeChipId = customChipId;
        updateChipStates(activeChipId);

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
                    renderMetrics();
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

    private void renderMetrics() {
        com.google.firebase.firestore.FirebaseFirestore firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        firestore.collection("usuarios").get().addOnSuccessListener(snapshot -> {
            int activeUsersCount = 0;
            int newRegistrationsCount = 0;
            int previousActiveUsersCount = 0;
            int previousNewRegistrationsCount = 0;
            
            int adminCount = 0;
            int asesorCount = 0;
            int clienteCount = 0;
            int totalRoles = 0;

            SuperadminRangeFilterHelper.DateRange previousRange = previousRange(currentRange);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

            for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments()) {
                Long createdAt = null;
                Object dateObj = doc.get("createdAt");
                if (dateObj instanceof Number) {
                    createdAt = ((Number) dateObj).longValue();
                } else if (dateObj instanceof com.google.firebase.Timestamp) {
                    createdAt = ((com.google.firebase.Timestamp) dateObj).toDate().getTime();
                } else if (dateObj instanceof String) {
                    try {
                        createdAt = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse((String) dateObj).getTime();
                    } catch (Exception ignored) {}
                }
                
                if (createdAt == null) {
                    // Fallback to 1 day ago so existing users without dates appear in the presentation data
                    createdAt = System.currentTimeMillis() - (24L * 60 * 60 * 1000); 
                }

                boolean isActive = true;
                boolean inCurrentRange = createdAt >= currentRange.start.getTime() && createdAt <= currentRange.end.getTime();
                boolean inPreviousRange = createdAt >= previousRange.start.getTime() && createdAt <= previousRange.end.getTime();

                if (inCurrentRange) {
                    newRegistrationsCount++;
                    if (isActive) {
                        activeUsersCount++;
                    }
                    
                    String role = doc.getString("rol");
                    if (role != null) {
                        role = role.toLowerCase(Locale.US);
                        if (role.contains("admin") || role.equals("superadmin")) adminCount++;
                        else if (role.contains("asesor")) asesorCount++;
                        else clienteCount++;
                    } else {
                        clienteCount++; // fallback
                    }
                    totalRoles++;
                }
                if (inPreviousRange) {
                    previousNewRegistrationsCount++;
                    if (isActive) {
                        previousActiveUsersCount++;
                    }
                }
            }

            if (activeUsersValue != null) {
                activeUsersValue.setText(formatCompactNumber(activeUsersCount));
            }
            if (activeUsersGrowthValue != null) {
                activeUsersGrowthValue.setText(formatGrowth(activeUsersCount, previousActiveUsersCount));
                activeUsersGrowthValue.setTextColor(ContextCompat.getColor(
                        this,
                        activeUsersCount >= previousActiveUsersCount ? R.color.sa_success : R.color.sa_danger
                ));
            }
            if (newRegistrationsValue != null) {
                newRegistrationsValue.setText(formatCompactNumber(newRegistrationsCount));
            }
            if (newRegistrationsGrowthValue != null) {
                newRegistrationsGrowthValue.setText(formatGrowth(newRegistrationsCount, previousNewRegistrationsCount));
                newRegistrationsGrowthValue.setTextColor(ContextCompat.getColor(
                        this,
                        newRegistrationsCount >= previousNewRegistrationsCount ? R.color.sa_success : R.color.sa_danger
                ));
            }
            
            if (totalRoles > 0 && roleAdminLabel != null) {
                int adminPct = Math.round((adminCount * 100f) / totalRoles);
                int asesorPct = Math.round((asesorCount * 100f) / totalRoles);
                int clientePct = Math.round((clienteCount * 100f) / totalRoles);

                // Fix rounding errors so it always adds up to 100 (except if all are 0)
                if (adminPct + asesorPct + clientePct != 100) {
                    clientePct = 100 - adminPct - asesorPct; 
                    if (clientePct < 0) clientePct = 0;
                }

                roleAdminLabel.setText(String.format(Locale.US, "Admin (%d%%)", adminPct));
                updateBarWeights(barRoleAdmin, spaceRoleAdmin, adminPct);

                roleAsesorLabel.setText(String.format(Locale.US, "Asesor (%d%%)", asesorPct));
                updateBarWeights(barRoleAsesor, spaceRoleAsesor, asesorPct);

                roleClienteLabel.setText(String.format(Locale.US, "Cliente (%d%%)", clientePct));
                updateBarWeights(barRoleCliente, spaceRoleCliente, clientePct);
            }
        });
    }

    private void updateBarWeights(View bar, View space, int percentage) {
        if (bar != null && space != null) {
            LinearLayout.LayoutParams barParams = (LinearLayout.LayoutParams) bar.getLayoutParams();
            barParams.weight = percentage;
            bar.setLayoutParams(barParams);

            LinearLayout.LayoutParams spaceParams = (LinearLayout.LayoutParams) space.getLayoutParams();
            spaceParams.weight = 100 - percentage;
            space.setLayoutParams(spaceParams);
        }
    }

    // Removed dangling block

    private void updateDateFilterLabel() {
        if (dateFilterText != null) {
            dateFilterText.setText(SuperadminRangeFilterHelper.formatRange(currentRange));
        }
    }

    private void updateChipStates(int activeId) {
        int[] chipIds = {
                R.id.chipRange7d,
                R.id.chipRange1m,
                R.id.chipRange3m,
                R.id.chipRange1y,
                R.id.chipRangeCustom
        };

        for (int chipId : chipIds) {
            TextView chip = findViewById(chipId);
            if (chip == null) {
                continue;
            }

            boolean active = chipId == activeId;
            chip.setBackgroundResource(active ? R.drawable.sa_chip_active : R.drawable.sa_chip_inactive);
            chip.setTextColor(ContextCompat.getColor(this, active ? R.color.white : R.color.app_chip_inactive_text));
        }
    }

    private void attachCustomRangeTrigger(View view) {
        view.setClickable(true);
        view.setFocusable(true);
        view.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN && v.getParent() != null) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
            }
            if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                openCustomDateRangePicker();
                return true;
            }
            return false;
        });
        view.setOnClickListener(v -> openCustomDateRangePicker());
    }

    private SuperadminRangeFilterHelper.DateRange previousRange(SuperadminRangeFilterHelper.DateRange range) {
        long spanMillis = range.end.getTime() - range.start.getTime();
        Calendar previousEnd = Calendar.getInstance();
        previousEnd.setTime(range.start);
        previousEnd.add(Calendar.DATE, -1);
        Calendar previousStart = (Calendar) previousEnd.clone();
        previousStart.setTimeInMillis(previousEnd.getTimeInMillis() - spanMillis);
        return new SuperadminRangeFilterHelper.DateRange(previousStart.getTime(), previousEnd.getTime());
    }

    private String formatCompactNumber(int value) {
        if (value >= 1_000) {
            return String.format(Locale.US, "%.1fk", value / 1_000d);
        }
        return String.valueOf(value);
    }

    private String formatGrowth(int current, int previous) {
        if (previous <= 0) {
            return current > 0 ? "+100%" : "+0%";
        }
        double delta = ((current - previous) * 100d) / previous;
        return String.format(Locale.US, "%s%.1f%%", delta >= 0 ? "+" : "", delta);
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
                    localizedTheme.setTo(SuperadminReportesUsuariosActivity.this.getTheme());
                }
                return localizedTheme;
            }
        };
    }
}
