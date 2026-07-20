package com.example.proyecto_iot.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proyecto_iot.admin.storage.AdminLocalStorage;
import com.example.proyecto_iot.data.FirebaseReportRepository;
import com.example.proyecto_iot.databinding.ActivityAdminReportesBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AdminReportesActivity extends BaseAdminActivity {
    private static final String FILTER_SCREEN_KEY = "admin_reports";
    private static final String ALL_ADVISORS = "Todos los asesores";
    private static final String ALL_PROJECTS = "Todos los proyectos";
    private static final String NO_ASSIGNED_PROJECTS = "Sin proyectos asignados";
    private static final String PERIOD_TODAY = "Diario";
    private static final String PERIOD_MONTH = "Mensual";
    private static final String PERIOD_YEAR = "Anual";

    private ActivityAdminReportesBinding binding;
    private AdminLocalStorage localStorage;
    private final List<FirebaseReportRepository.SeparationRecord> records = new ArrayList<>();
    private final Map<String, String> projectNames = new LinkedHashMap<>();
    private final Map<String, String> advisorNames = new LinkedHashMap<>();
    private final Map<String, Set<String>> activeProjectIdsByAdvisor = new LinkedHashMap<>();
    private final Map<String, String> advisorIdsByOption = new LinkedHashMap<>();
    private final Map<String, String> projectIdsByOption = new LinkedHashMap<>();
    private final List<String> periodOptions = new ArrayList<>();
    private final List<String> advisorOptions = new ArrayList<>();
    private final List<String> projectOptions = new ArrayList<>();
    private boolean restoringFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminReportesBinding.inflate(getLayoutInflater());
        setContentView(binding);
        localStorage = new AdminLocalStorage(this);
        setupBottomNavigation();
        setupBackButton();
        initializeOptions();
        setupFilters();
        loadRealData();
    }

    private void initializeOptions() {
        periodOptions.clear();
        Collections.addAll(periodOptions, PERIOD_TODAY, PERIOD_MONTH, PERIOD_YEAR);
        advisorOptions.clear();
        advisorOptions.add(ALL_ADVISORS);
        projectOptions.clear();
        projectOptions.add(ALL_PROJECTS);
        binding.spProyectoReportes.setEnabled(false);
    }

    private void loadRealData() {
        binding.tvFiltroResumenReportes.setText("Cargando datos reales desde Firestore…");
        new FirebaseReportRepository().loadCurrentAdminReport(new FirebaseReportRepository.Callback() {
            @Override
            public void onSuccess(FirebaseReportRepository.ReportData data) {
                records.clear();
                records.addAll(data.separations);
                projectNames.clear();
                projectNames.putAll(data.projects);
                advisorNames.clear();
                advisorNames.putAll(data.advisors);
                activeProjectIdsByAdvisor.clear();
                activeProjectIdsByAdvisor.putAll(data.activeProjectIdsByAdvisor);
                rebuildFilterOptions();
                restoreLastFilter();
                renderReport(false);
            }

            @Override
            public void onError(String message) {
                records.clear();
                projectNames.clear();
                advisorNames.clear();
                activeProjectIdsByAdvisor.clear();
                rebuildFilterOptions();
                renderReport(false);
                binding.tvFiltroResumenReportes.setText("No se pudieron cargar datos reales.");
                Toast.makeText(AdminReportesActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void rebuildFilterOptions() {
        advisorOptions.clear();
        advisorOptions.add(ALL_ADVISORS);
        advisorIdsByOption.clear();
        List<Map.Entry<String, String>> advisors = new ArrayList<>(advisorNames.entrySet());
        advisors.sort(Map.Entry.comparingByValue(String.CASE_INSENSITIVE_ORDER));
        for (Map.Entry<String, String> advisor : advisors) {
            String option = uniqueOption(advisorOptions, advisor.getValue(), advisor.getKey());
            advisorOptions.add(option);
            advisorIdsByOption.put(option, advisor.getKey());
        }
        configureSpinnerSafely(binding.spAsesorReportes, advisorOptions, 0);
        rebuildProjectOptions(ALL_ADVISORS, ALL_PROJECTS);
    }

    private void setupFilters() {
        configureSpinner(binding.spPeriodoReportes, periodOptions);
        configureSpinner(binding.spAsesorReportes, advisorOptions);
        configureSpinner(binding.spProyectoReportes, projectOptions);
        binding.spPeriodoReportes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!restoringFilters) {
                    renderReport(true);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        binding.spAsesorReportes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!restoringFilters) {
                    rebuildProjectOptions(selected(binding.spAsesorReportes), ALL_PROJECTS);
                    renderReport(true);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        binding.spProyectoReportes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!restoringFilters) {
                    renderReport(true);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        binding.btnLimpiarFiltrosReportes.setOnClickListener(v -> {
            restoringFilters = true;
            binding.spPeriodoReportes.setSelection(1);
            binding.spAsesorReportes.setSelection(0);
            rebuildProjectOptions(ALL_ADVISORS, ALL_PROJECTS);
            restoringFilters = false;
            renderReport(true);
        });
    }

    private void configureSpinner(android.widget.Spinner spinner, List<String> values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void configureSpinnerSafely(android.widget.Spinner spinner, List<String> values, int selectedIndex) {
        boolean previousRestoring = restoringFilters;
        restoringFilters = true;
        configureSpinner(spinner, values);
        spinner.setSelection(Math.max(0, Math.min(selectedIndex, values.size() - 1)));
        restoringFilters = previousRestoring;
    }

    private void rebuildProjectOptions(String advisorOption, String desiredProjectOption) {
        projectOptions.clear();
        projectIdsByOption.clear();
        String advisorId = advisorIdsByOption.get(advisorOption);
        if (advisorId == null) {
            projectOptions.add(ALL_PROJECTS);
            binding.spProyectoReportes.setEnabled(false);
            configureSpinnerSafely(binding.spProyectoReportes, projectOptions, 0);
            return;
        }

        Set<String> assignedProjectIds = activeProjectIdsByAdvisor.get(advisorId);
        List<String> validProjectIds = new ArrayList<>();
        if (assignedProjectIds != null) {
            for (String projectId : assignedProjectIds) {
                if (projectNames.containsKey(projectId)) {
                    validProjectIds.add(projectId);
                }
            }
        }
        if (validProjectIds.isEmpty()) {
            projectOptions.add(NO_ASSIGNED_PROJECTS);
            binding.spProyectoReportes.setEnabled(false);
            configureSpinnerSafely(binding.spProyectoReportes, projectOptions, 0);
            return;
        }

        validProjectIds.sort((first, second) -> projectNames.get(first)
                .compareToIgnoreCase(projectNames.get(second)));
        projectOptions.add(ALL_PROJECTS);
        for (String projectId : validProjectIds) {
            String option = uniqueOption(projectOptions, projectNames.get(projectId), projectId);
            projectOptions.add(option);
            projectIdsByOption.put(option, projectId);
        }
        binding.spProyectoReportes.setEnabled(true);
        configureSpinnerSafely(binding.spProyectoReportes, projectOptions,
                indexOf(projectOptions, desiredProjectOption, 0));
    }

    private String uniqueOption(List<String> existing, String label, String id) {
        String base = label == null || label.trim().isEmpty() ? "Sin identificar" : label.trim();
        if (!existing.contains(base)) {
            return base;
        }
        String suffix = id == null || id.length() < 6 ? String.valueOf(id) : id.substring(0, 6);
        return base + " (" + suffix + ")";
    }

    private void restoreLastFilter() {
        String saved = localStorage.getLastFilter(
                FILTER_SCREEN_KEY,
                PERIOD_MONTH + "|" + ALL_ADVISORS + "|" + ALL_PROJECTS
        );
        String[] parts = saved.split("\\|", -1);
        restoringFilters = true;
        binding.spPeriodoReportes.setSelection(indexOf(periodOptions, parts.length > 0 ? parts[0] : PERIOD_MONTH, 1));
        binding.spAsesorReportes.setSelection(indexOf(advisorOptions, parts.length > 1 ? parts[1] : ALL_ADVISORS, 0));
        rebuildProjectOptions(selected(binding.spAsesorReportes),
                parts.length > 2 ? parts[2] : ALL_PROJECTS);
        restoringFilters = false;
    }

    private void renderReport(boolean persist) {
        String period = selected(binding.spPeriodoReportes);
        String advisor = selected(binding.spAsesorReportes);
        String project = selected(binding.spProyectoReportes);
        String advisorId = advisorIdsByOption.get(advisor);
        String projectId = projectIdsByOption.get(project);
        if (persist) {
            localStorage.saveLastFilter(FILTER_SCREEN_KEY, period + "|" + advisor + "|"
                    + (advisorId == null ? ALL_PROJECTS : project));
        }

        long start = periodStart(period);
        List<FirebaseReportRepository.SeparationRecord> filtered = new ArrayList<>();
        for (FirebaseReportRepository.SeparationRecord record : records) {
            String advisorLabel = advisorLabel(record);
            String projectLabel = projectLabel(record);
            boolean advisorMatches = advisorId == null || advisorId.equals(record.advisorId);
            Set<String> assignedProjects = advisorId == null ? null : activeProjectIdsByAdvisor.get(advisorId);
            boolean assignedToAdvisor = advisorId == null || (assignedProjects != null
                    && assignedProjects.contains(record.projectId));
            boolean projectMatches = projectId == null || projectId.equals(record.projectId);
            long saleDate = record.paymentProcessedAt > 0 ? record.paymentProcessedAt : record.createdAt;
            boolean hasActivityInPeriod = record.createdAt >= start || (record.processed && saleDate >= start);
            if (hasActivityInPeriod && advisorMatches && assignedToAdvisor && projectMatches) {
                filtered.add(record);
            }
        }

        Map<String, Metric> byAdvisor = new LinkedHashMap<>();
        Map<String, Metric> byProject = new LinkedHashMap<>();
        double totalSales = 0d;
        int approvedSeparations = 0;
        int totalSeparations = 0;
        for (FirebaseReportRepository.SeparationRecord record : filtered) {
            Metric advisorMetric = byAdvisor.computeIfAbsent(advisorLabel(record), Metric::new);
            Metric projectMetric = byProject.computeIfAbsent(projectLabel(record), Metric::new);
            if (record.createdAt >= start) {
                advisorMetric.separations++;
                projectMetric.separations++;
                totalSeparations++;
            }
            long saleDate = record.paymentProcessedAt > 0 ? record.paymentProcessedAt : record.createdAt;
            if (record.processed && saleDate >= start) {
                double amountInPen = record.amountInPen();
                advisorMetric.sales += amountInPen;
                projectMetric.sales += amountInPen;
                totalSales += amountInPen;
                approvedSeparations++;
            }
        }

        int conversion = totalSeparations == 0 ? 0 : Math.round(approvedSeparations * 100f / totalSeparations);
        binding.tvTotalVentasReporte.setText(formatMoney(totalSales));
        binding.tvTotalSeparacionesReporte.setText(String.valueOf(totalSeparations));
        binding.tvConversionReporte.setText(conversion + "%");
        binding.tvProyectosReporte.setText(String.valueOf(byProject.size()));
        binding.tvFiltroResumenReportes.setText(period + " | " + totalSeparations
                + " separaciones | " + approvedSeparations + " aprobadas o pagadas");

        List<Metric> advisors = new ArrayList<>(byAdvisor.values());
        advisors.sort(Comparator.comparingInt((Metric metric) -> metric.separations).reversed());
        List<Metric> projects = new ArrayList<>(byProject.values());
        projects.sort(Comparator.comparingDouble((Metric metric) -> metric.sales).reversed());
        renderAdvisorBars(advisors);
        renderProjectRows(projects);
    }

    private void renderAdvisorBars(List<Metric> metrics) {
        binding.tvTopAsesoresLabel.setText(metrics.size() <= 1 ? "Detalle" : "Top " + Math.min(4, metrics.size()));
        int max = 1;
        for (Metric metric : metrics) max = Math.max(max, metric.separations);
        bindAdvisorBar(0, metrics, max, binding.groupAdvisor1, binding.tvAdvisor1Value, binding.barAdvisor1, binding.tvAdvisor1Name);
        bindAdvisorBar(1, metrics, max, binding.groupAdvisor2, binding.tvAdvisor2Value, binding.barAdvisor2, binding.tvAdvisor2Name);
        bindAdvisorBar(2, metrics, max, binding.groupAdvisor3, binding.tvAdvisor3Value, binding.barAdvisor3, binding.tvAdvisor3Name);
        bindAdvisorBar(3, metrics, max, binding.groupAdvisor4, binding.tvAdvisor4Value, binding.barAdvisor4, binding.tvAdvisor4Name);
    }

    private void bindAdvisorBar(int index, List<Metric> metrics, int max, LinearLayout group, TextView value, View bar, TextView name) {
        if (index >= metrics.size()) {
            group.setVisibility(View.INVISIBLE);
            return;
        }
        Metric metric = metrics.get(index);
        group.setVisibility(View.VISIBLE);
        value.setText(String.valueOf(metric.separations));
        name.setText(shortName(metric.name));
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bar.getLayoutParams();
        params.height = metric.separations == 0 ? 8 : Math.max(24, Math.round(metric.separations * 118f / max));
        bar.setLayoutParams(params);
    }

    private void renderProjectRows(List<Metric> metrics) {
        binding.tvTopProyectosLabel.setText(metrics.size() <= 1 ? "Detalle" : "Top " + Math.min(4, metrics.size()));
        binding.tvProjectsEmpty.setVisibility(metrics.isEmpty() ? View.VISIBLE : View.GONE);
        double max = 1d;
        for (Metric metric : metrics) max = Math.max(max, metric.sales);
        bindProjectRow(0, metrics, max, binding.groupProject1, binding.tvProject1Name, binding.tvProject1Amount, binding.progressProject1);
        bindProjectRow(1, metrics, max, binding.groupProject2, binding.tvProject2Name, binding.tvProject2Amount, binding.progressProject2);
        bindProjectRow(2, metrics, max, binding.groupProject3, binding.tvProject3Name, binding.tvProject3Amount, binding.progressProject3);
        bindProjectRow(3, metrics, max, binding.groupProject4, binding.tvProject4Name, binding.tvProject4Amount, binding.progressProject4);
    }

    private void bindProjectRow(int index, List<Metric> metrics, double max, LinearLayout group, TextView name,
                                TextView amount, ProgressBar progress) {
        if (index >= metrics.size()) {
            group.setVisibility(View.GONE);
            return;
        }
        Metric metric = metrics.get(index);
        group.setVisibility(View.VISIBLE);
        name.setText(metric.name);
        amount.setText(formatMoney(metric.sales));
        progress.setProgress((int) Math.max(metric.sales > 0 ? 4 : 0, Math.round(metric.sales * 100 / max)));
    }

    private long periodStart(String period) {
        Calendar calendar = Calendar.getInstance();
        if (PERIOD_TODAY.equals(period)) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        } else if (PERIOD_YEAR.equals(period)) {
            calendar.set(Calendar.DAY_OF_YEAR, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        } else {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }
        return calendar.getTimeInMillis();
    }

    private String selected(android.widget.Spinner spinner) {
        Object item = spinner.getSelectedItem();
        return item == null ? "" : item.toString();
    }

    private int indexOf(List<String> values, String target, int fallback) {
        int index = values.indexOf(target);
        return index >= 0 ? index : Math.min(fallback, Math.max(0, values.size() - 1));
    }

    private String shortName(String name) {
        String[] parts = name.trim().split("\\s+");
        return parts.length < 2 ? name.toUpperCase(Locale.ROOT)
                : (parts[0] + " " + parts[1].charAt(0) + ".").toUpperCase(Locale.ROOT);
    }

    private String formatMoney(double amount) {
        return "S/ " + String.format(Locale.US, "%,.2f", amount);
    }

    private String advisorLabel(FirebaseReportRepository.SeparationRecord record) {
        String resolved = advisorNames.get(record.advisorId);
        return resolved == null || resolved.trim().isEmpty() ? record.advisorName : resolved;
    }

    private String projectLabel(FirebaseReportRepository.SeparationRecord record) {
        String resolved = projectNames.get(record.projectId);
        return resolved == null || resolved.trim().isEmpty() ? record.projectName : resolved;
    }

    private static class Metric {
        final String name;
        int separations;
        double sales;

        Metric(String name) {
            this.name = name == null || name.trim().isEmpty() ? "Sin identificar" : name;
        }
    }
}
