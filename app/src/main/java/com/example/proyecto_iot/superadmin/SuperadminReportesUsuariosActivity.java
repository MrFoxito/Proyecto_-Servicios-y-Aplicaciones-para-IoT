package com.example.proyecto_iot.superadmin;

import com.example.proyecto_iot.R;
import com.google.android.material.datepicker.MaterialDatePicker;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.util.Pair;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SuperadminReportesUsuariosActivity extends BaseSuperadminActivity {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_reportes_usuarios);
        setupCommonNavigation();
        setupDateFilter();
        setupRangeChips();
    }

    private void setupDateFilter() {
        TextView dateFilterText = findViewById(R.id.textReportesDateFilter);
        findViewById(R.id.layoutReportesDateFilter).setOnClickListener(view -> {
            MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Seleccionar rango de fechas")
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                String formatted = formatDateRange(selection);
                if (dateFilterText != null) {
                    dateFilterText.setText(formatted);
                }
            });
            picker.show(getSupportFragmentManager(), "reportes_usuarios_date_range");
        });
    }

    private void setupRangeChips() {
        bindRangeChip(R.id.chipRange7d, "7D");
        bindRangeChip(R.id.chipRange1m, "1M");
        bindRangeChip(R.id.chipRange3m, "3M");
        bindRangeChip(R.id.chipRange1y, "1Y");
    }

    private void bindRangeChip(int viewId, String label) {
        View chip = findViewById(viewId);
        if (chip != null) {
            chip.setOnClickListener(view ->
                    Toast.makeText(this, "Rango: " + label, Toast.LENGTH_SHORT).show());
        }
    }

    private String formatDateRange(Pair<Long, Long> selection) {
        if (selection == null || selection.first == null || selection.second == null) {
            return getString(R.string.sa_date_range_placeholder);
        }
        return dateFormat.format(selection.first) + " - " + dateFormat.format(selection.second);
    }
}
