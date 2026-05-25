package com.example.proyecto_iot.superadmin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.google.android.material.datepicker.MaterialDatePicker;

import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Locale;
public class SuperadminLogsActivity extends BaseSuperadminActivity {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_superadmin_logs);
        setupCommonNavigation();

        RecyclerView recyclerView = findViewById(R.id.recyclerLogs);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new SuperadminLogEntryAdapter(
                    new LocalSchemaStorage(this).getSuperadminLogs()
            ));
        }

        setupDateFilter();
        setupLogFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RecyclerView recyclerView = findViewById(R.id.recyclerLogs);
        if (recyclerView != null) {
            recyclerView.setAdapter(new SuperadminLogEntryAdapter(
                    new LocalSchemaStorage(this).getSuperadminLogs()
            ));
        }
    }

    private void setupDateFilter() {
        TextView dateFilterText = findViewById(R.id.textLogsDateFilter);
        findViewById(R.id.layoutLogsDateFilter).setOnClickListener(view -> {
            MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Seleccionar rango de fechas")
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                String formatted = formatDateRange(selection);
                if (dateFilterText != null) {
                    dateFilterText.setText(formatted);
                }
            });
            picker.show(getSupportFragmentManager(), "logs_date_range");
        });
    }

    private void setupLogFilters() {
        View userFilter = findViewById(R.id.layoutLogsUserFilter);
        if (userFilter != null) {
            userFilter.setOnClickListener(view ->
                    Toast.makeText(this, "Filtro por usuario (mock)", Toast.LENGTH_SHORT).show());
        }

        TextView chipAll = findViewById(R.id.chipLogsAll);
        TextView chipCritical = findViewById(R.id.chipLogsCritical);
        TextView chipAlerts = findViewById(R.id.chipLogsAlerts);
        if (chipAll != null && chipCritical != null && chipAlerts != null) {
            setActiveChip(chipAll, chipCritical, chipAlerts);
            chipAll.setOnClickListener(view -> {
                setActiveChip(chipAll, chipCritical, chipAlerts);
                Toast.makeText(this, "Filtro: Todos", Toast.LENGTH_SHORT).show();
            });
            chipCritical.setOnClickListener(view -> {
                setActiveChip(chipCritical, chipAll, chipAlerts);
                Toast.makeText(this, "Filtro: Criticos", Toast.LENGTH_SHORT).show();
            });
            chipAlerts.setOnClickListener(view -> {
                setActiveChip(chipAlerts, chipAll, chipCritical);
                Toast.makeText(this, "Filtro: Alertas", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setActiveChip(TextView active, TextView inactiveOne, TextView inactiveTwo) {
        active.setBackgroundResource(R.drawable.sa_chip_active);
        active.setTextColor(ContextCompat.getColor(this, R.color.white));
        inactiveOne.setBackgroundResource(R.drawable.sa_chip_inactive);
        inactiveOne.setTextColor(ContextCompat.getColor(this, R.color.app_chip_inactive_text));
        inactiveTwo.setBackgroundResource(R.drawable.sa_chip_inactive);
        inactiveTwo.setTextColor(ContextCompat.getColor(this, R.color.app_chip_inactive_text));
    }

    private String formatDateRange(Pair<Long, Long> selection) {
        if (selection == null || selection.first == null || selection.second == null) {
            return getString(R.string.sa_date_range_placeholder);
        }
        return dateFormat.format(selection.first) + " - " + dateFormat.format(selection.second);
    }

}
