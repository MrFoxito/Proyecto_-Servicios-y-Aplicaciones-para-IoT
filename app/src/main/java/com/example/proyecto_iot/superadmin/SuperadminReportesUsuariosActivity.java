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
    private TextView conversionValue;
    private TextView conversionStatusValue;

    private int activeChipId = R.id.chipRange7d;
    private final int customChipId = R.id.chipRangeCustom;
    private SuperadminRangeFilterHelper.DateRange currentRange =
            SuperadminRangeFilterHelper.presetRange(SuperadminRangeFilterHelper.Preset.DAYS, 7);

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
        conversionValue = findViewById(R.id.tvUsersConversionValue);
        conversionStatusValue = findViewById(R.id.tvUsersConversionStatusValue);
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
        LocalSchemaStorage storage = new LocalSchemaStorage(this);
        List<SuperadminGestionUsuarioItem> users = storage.getSuperadminUsers();
        List<SuperadminSolicitudAsesorItem> requests = storage.getSuperadminAdvisorRequests();

        int activeUsersCount = 0;
        int newRegistrationsCount = 0;
        int approvedRequestsCount = 0;
        int totalRequestsCount = 0;

        SuperadminRangeFilterHelper.DateRange previousRange = previousRange(currentRange);

        int previousActiveUsersCount = 0;
        int previousNewRegistrationsCount = 0;

        for (SuperadminGestionUsuarioItem user : users) {
            if (SuperadminRangeFilterHelper.withinIsoRange(user.getDateIso(), currentRange)) {
                newRegistrationsCount++;
                if (user.isActive()) {
                    activeUsersCount++;
                }
            }
            if (SuperadminRangeFilterHelper.withinIsoRange(user.getDateIso(), previousRange)) {
                previousNewRegistrationsCount++;
                if (user.isActive()) {
                    previousActiveUsersCount++;
                }
            }
        }

        for (SuperadminSolicitudAsesorItem request : requests) {
            if (!SuperadminRangeFilterHelper.withinIsoRange(request.getDateIso(), currentRange)) {
                continue;
            }
            totalRequestsCount++;
            if ("ACEPTADA".equalsIgnoreCase(request.getStatus())) {
                approvedRequestsCount++;
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
        if (conversionValue != null) {
            conversionValue.setText(formatPercentage(approvedRequestsCount, totalRequestsCount));
        }
        if (conversionStatusValue != null) {
            conversionStatusValue.setText(conversionLabel(approvedRequestsCount, totalRequestsCount));
        }
    }

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

    private String formatPercentage(int approved, int total) {
        if (total <= 0) {
            return "0%";
        }
        double value = (approved * 100d) / total;
        return String.format(Locale.US, "%.1f%%", value);
    }

    private String conversionLabel(int approved, int total) {
        if (total <= 0) {
            return "Sin datos";
        }
        double value = (approved * 100d) / total;
        if (value >= 70d) {
            return "Alto";
        }
        if (value >= 40d) {
            return "Estable";
        }
        return "Bajo";
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
