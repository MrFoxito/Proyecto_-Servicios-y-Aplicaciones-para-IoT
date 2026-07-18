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
import com.example.proyecto_iot.entity.Separacion;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SuperadminReportesGlobalesActivity extends BaseSuperadminActivity {

    private static final Locale ES_LOCALE = SuperadminRangeFilterHelper.ES_LOCALE;

    private TextView dateFilterText;
    private TextView globalReservationsValue;
    private TextView globalGrowthValue;

    private int activeChipId = R.id.chipRange7d;
    private final int customChipId = R.id.chipRangeCustom;
    private SuperadminRangeFilterHelper.DateRange currentRange =
            SuperadminRangeFilterHelper.presetRange(SuperadminRangeFilterHelper.Preset.DAYS, 7);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_reportes_globales);
        setupCommonNavigation();
        bindViews();
        setupDateFilter();
        setupRangeChips();
        renderMetrics();
    }

    private void bindViews() {
        dateFilterText = findViewById(R.id.textReportesDateFilter);
        globalReservationsValue = findViewById(R.id.tvGlobalReservasValue);
        globalGrowthValue = findViewById(R.id.tvGlobalGrowthValue);
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
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        SuperadminRangeFilterHelper.DateRange previousRange = previousRange(currentRange);

        firestore.collection("separaciones").get().addOnSuccessListener(snapshot -> {
            if (isFinishing() || isDestroyed()) return;

            double currentTotal = 0d;
            double previousTotal = 0d;

            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Date createdAt = dateFromDocument(doc);
                if (createdAt == null) {
                    continue;
                }

                double amount = amountFromDocument(doc);

                if (currentRange.contains(createdAt)) {
                    currentTotal += amount;
                } else if (previousRange.contains(createdAt)) {
                    previousTotal += amount;
                }
            }

            if (globalReservationsValue != null) {
                globalReservationsValue.setText(formatMoney(currentTotal));
            }
            if (globalGrowthValue != null) {
                globalGrowthValue.setText(formatGrowth(currentTotal, previousTotal));
                globalGrowthValue.setTextColor(ContextCompat.getColor(this, 
                        currentTotal >= previousTotal ? R.color.sa_success : R.color.sa_danger));
            }
        });
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

    private double parseMoney(String value) {
        if (value == null) {
            return 0d;
        }
        String normalized = value.replace("S/", "")
                .replace("PEN", "")
                .replace("$", "")
                .replace(",", "")
                .replace(" ", "")
                .trim();
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ignored) {
            return 0d;
        }
    }

    private Date dateFromDocument(DocumentSnapshot doc) {
        Object dateObj = doc.get("fechaIso");
        Date date = parseDateObject(dateObj, "yyyy-MM-dd'T'HH:mm:ss");
        if (date != null) {
            return date;
        }
        return parseDateObject(doc.get("createdAt"), "yyyy-MM-dd'T'HH:mm:ss'Z'");
    }

    private Date parseDateObject(Object value, String pattern) {
        if (value instanceof Number) {
            return new Date(((Number) value).longValue());
        }
        if (value instanceof com.google.firebase.Timestamp) {
            return ((com.google.firebase.Timestamp) value).toDate();
        }
        if (value instanceof String) {
            try {
                return new java.text.SimpleDateFormat(pattern, Locale.US).parse((String) value);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private double amountFromDocument(DocumentSnapshot doc) {
        for (String field : new String[]{"amount", "monto", "montoTexto", "montoSeparacion"}) {
            Object value = doc.get(field);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            if (value instanceof String) {
                double parsed = parseMoney((String) value);
                if (parsed > 0d) {
                    return parsed;
                }
            }
        }
        return 0d;
    }

    private String formatMoney(double amount) {
        if (amount >= 1_000_000d) {
            return String.format(Locale.US, "S/ %.1fM", amount / 1_000_000d);
        }
        if (amount >= 1_000d) {
            return String.format(Locale.US, "S/ %.1fK", amount / 1_000d);
        }
        return String.format(Locale.US, "S/ %.0f", amount);
    }

    private String formatCompactNumber(int value) {
        if (value >= 1_000) {
            return String.format(Locale.US, "%.1fk", value / 1_000d);
        }
        return String.valueOf(value);
    }

    private String formatGrowth(double current, double previous) {
        if (previous <= 0d) {
            return current > 0d ? "+100%" : "+0%";
        }
        double delta = ((current - previous) / previous) * 100d;
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
                    localizedTheme.setTo(SuperadminReportesGlobalesActivity.this.getTheme());
                }
                return localizedTheme;
            }
        };
    }
}
