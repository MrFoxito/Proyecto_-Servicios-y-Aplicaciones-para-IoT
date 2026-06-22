package com.example.proyecto_iot.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.proyecto_iot.admin.model.AdminAdvisorItem;
import com.example.proyecto_iot.admin.model.AdminProjectItem;
import com.example.proyecto_iot.admin.storage.AdminLocalStorage;
import com.example.proyecto_iot.data.LocalSchemaStorage;
import com.example.proyecto_iot.databinding.ActivityAdminReportesBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Dashboard de reportes y metricas del Administrador.
 * Permite filtrar por periodo, asesor y proyecto para escalar con muchos datos.
 */
public class AdminReportesActivity extends BaseAdminActivity {

    private static final String FILTER_SCREEN_KEY = "admin_reports";
    private static final String ALL_ADVISORS = "Todos los asesores";
    private static final String ALL_PROJECTS = "Todos los proyectos";
    private static final String PERIOD_TODAY = "Diario";
    private static final String PERIOD_MONTH = "Mensual";
    private static final String PERIOD_YEAR = "Anual";

    private ActivityAdminReportesBinding binding;
    private AdminLocalStorage adminLocalStorage;
    private final List<ReportAdvisorMetric> advisorMetrics = new ArrayList<>();
    private final List<ReportProjectMetric> projectMetrics = new ArrayList<>();
    private final List<String> periodOptions = new ArrayList<>();
    private final List<String> advisorOptions = new ArrayList<>();
    private final List<String> projectOptions = new ArrayList<>();
    private boolean restoringFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminReportesBinding.inflate(getLayoutInflater());
        setContentView(binding);

        adminLocalStorage = new AdminLocalStorage(this);
        setupBottomNavigation();
        setupBackButton();
        buildReportData();
        setupFilters();
        restoreLastFilter();
    }

    private void buildReportData() {
        LocalSchemaStorage storage = new LocalSchemaStorage(this);
        List<AdminAdvisorItem> advisors = storage.getAdminAdvisors();
        List<AdminProjectItem> projects = storage.getAdminProjects();

        projectMetrics.clear();
        for (int i = 0; i < projects.size(); i++) {
            AdminProjectItem project = projects.get(i);
            long baseSales = getProjectBaseSales(i);
            int baseSeparations = getProjectBaseSeparations(i);
            projectMetrics.add(new ReportProjectMetric(project.getTitle(), baseSales, baseSeparations));
        }

        advisorMetrics.clear();
        for (int i = 0; i < advisors.size(); i++) {
            AdminAdvisorItem advisor = advisors.get(i);
            List<String> assignedProjects = new ArrayList<>(advisor.getProjects());
            long baseSales = 0L;
            int baseSeparations = 0;

            for (ReportProjectMetric project : projectMetrics) {
                if (containsProject(assignedProjects, project.name)) {
                    baseSales += project.baseSales;
                    baseSeparations += project.baseSeparations;
                }
            }

            if (baseSeparations == 0 && !projectMetrics.isEmpty()) {
                ReportProjectMetric fallback = projectMetrics.get(i % projectMetrics.size());
                assignedProjects.add(fallback.name);
                baseSales = fallback.baseSales;
                baseSeparations = fallback.baseSeparations;
            }

            advisorMetrics.add(new ReportAdvisorMetric(advisor.getName(), assignedProjects, baseSales, baseSeparations));
        }

        periodOptions.clear();
        periodOptions.add(PERIOD_TODAY);
        periodOptions.add(PERIOD_MONTH);
        periodOptions.add(PERIOD_YEAR);

        advisorOptions.clear();
        advisorOptions.add(ALL_ADVISORS);
        for (ReportAdvisorMetric advisor : advisorMetrics) {
            advisorOptions.add(advisor.name);
        }

        projectOptions.clear();
        projectOptions.add(ALL_PROJECTS);
        for (ReportProjectMetric project : projectMetrics) {
            projectOptions.add(project.name);
        }
    }

    private void setupFilters() {
        configureSpinner(binding.spPeriodoReportes, periodOptions);
        configureSpinner(binding.spAsesorReportes, advisorOptions);
        configureSpinner(binding.spProyectoReportes, projectOptions);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!restoringFilters) {
                    renderReport(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Spinners always keep a selected value.
            }
        };

        binding.spPeriodoReportes.setOnItemSelectedListener(listener);
        binding.spAsesorReportes.setOnItemSelectedListener(listener);
        binding.spProyectoReportes.setOnItemSelectedListener(listener);

        binding.btnLimpiarFiltrosReportes.setOnClickListener(v -> {
            restoringFilters = true;
            binding.spPeriodoReportes.setSelection(1);
            binding.spAsesorReportes.setSelection(0);
            binding.spProyectoReportes.setSelection(0);
            restoringFilters = false;
            renderReport(true);
        });
    }

    private void configureSpinner(android.widget.Spinner spinner, List<String> values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                values
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void restoreLastFilter() {
        String filter = adminLocalStorage.getLastFilter(FILTER_SCREEN_KEY, PERIOD_MONTH + "|" + ALL_ADVISORS + "|" + ALL_PROJECTS);
        String[] parts = filter.split("\\|", -1);

        restoringFilters = true;
        binding.spPeriodoReportes.setSelection(indexOf(periodOptions, parts.length > 0 ? parts[0] : PERIOD_MONTH, 1));
        binding.spAsesorReportes.setSelection(indexOf(advisorOptions, parts.length > 1 ? parts[1] : ALL_ADVISORS, 0));
        binding.spProyectoReportes.setSelection(indexOf(projectOptions, parts.length > 2 ? parts[2] : ALL_PROJECTS, 0));
        restoringFilters = false;
        renderReport(false);
    }

    private void renderReport(boolean persistFilter) {
        String period = selected(binding.spPeriodoReportes);
        String advisor = selected(binding.spAsesorReportes);
        String project = selected(binding.spProyectoReportes);
        double periodFactor = periodFactor(period);

        if (persistFilter) {
            adminLocalStorage.saveLastFilter(FILTER_SCREEN_KEY, period + "|" + advisor + "|" + project);
        }

        List<ReportProjectMetric> filteredProjects = getFilteredProjects(project, advisor, periodFactor);
        List<ReportAdvisorMetric> filteredAdvisors = getFilteredAdvisors(advisor, project, periodFactor);

        long totalSales = 0L;
        int totalSeparations = 0;
        Set<String> visibleProjectNames = new LinkedHashSet<>();
        for (ReportProjectMetric metric : filteredProjects) {
            totalSales += metric.sales;
            totalSeparations += metric.separations;
            visibleProjectNames.add(metric.name);
        }

        int conversion = totalSeparations == 0 ? 0 : Math.min(96, 58 + (totalSeparations * 3));
        binding.tvTotalVentasReporte.setText(formatSoles(totalSales));
        binding.tvTotalSeparacionesReporte.setText(String.valueOf(totalSeparations));
        binding.tvConversionReporte.setText(conversion + "%");
        binding.tvProyectosReporte.setText(String.valueOf(visibleProjectNames.size()));
        binding.tvFiltroResumenReportes.setText(buildFilterSummary(period, advisor, project, filteredAdvisors.size(), visibleProjectNames.size()));

        renderAdvisorBars(filteredAdvisors);
        renderInventory(totalSeparations, visibleProjectNames.size());
        renderProjectRows(filteredProjects);
    }

    private List<ReportProjectMetric> getFilteredProjects(String selectedProject, String selectedAdvisor, double periodFactor) {
        Set<String> advisorProjects = new LinkedHashSet<>();
        if (!ALL_ADVISORS.equals(selectedAdvisor)) {
            for (ReportAdvisorMetric advisor : advisorMetrics) {
                if (advisor.name.equals(selectedAdvisor)) {
                    advisorProjects.addAll(advisor.projectNames);
                    break;
                }
            }
        }

        List<ReportProjectMetric> filtered = new ArrayList<>();
        for (ReportProjectMetric base : projectMetrics) {
            boolean matchesProject = ALL_PROJECTS.equals(selectedProject) || base.name.equals(selectedProject);
            boolean matchesAdvisor = ALL_ADVISORS.equals(selectedAdvisor) || advisorProjects.contains(base.name);
            if (matchesProject && matchesAdvisor) {
                filtered.add(base.scaled(periodFactor));
            }
        }

        Collections.sort(filtered, (a, b) -> Long.compare(b.sales, a.sales));
        return filtered;
    }

    private List<ReportAdvisorMetric> getFilteredAdvisors(String selectedAdvisor, String selectedProject, double periodFactor) {
        List<ReportAdvisorMetric> filtered = new ArrayList<>();
        for (ReportAdvisorMetric base : advisorMetrics) {
            boolean matchesAdvisor = ALL_ADVISORS.equals(selectedAdvisor) || base.name.equals(selectedAdvisor);
            boolean matchesProject = ALL_PROJECTS.equals(selectedProject) || base.projectNames.contains(selectedProject);
            if (matchesAdvisor && matchesProject) {
                filtered.add(base.scaled(periodFactor, selectedProject));
            }
        }

        Collections.sort(filtered, Comparator.comparingInt((ReportAdvisorMetric item) -> item.separations).reversed());
        return filtered;
    }

    private void renderAdvisorBars(List<ReportAdvisorMetric> advisors) {
        binding.tvTopAsesoresLabel.setText(advisors.size() <= 1 ? "Detalle" : "Top " + Math.min(4, advisors.size()));
        int max = 1;
        for (ReportAdvisorMetric advisor : advisors) {
            max = Math.max(max, advisor.separations);
        }

        bindAdvisorBar(0, advisors, max, binding.groupAdvisor1, binding.tvAdvisor1Value, binding.barAdvisor1, binding.tvAdvisor1Name);
        bindAdvisorBar(1, advisors, max, binding.groupAdvisor2, binding.tvAdvisor2Value, binding.barAdvisor2, binding.tvAdvisor2Name);
        bindAdvisorBar(2, advisors, max, binding.groupAdvisor3, binding.tvAdvisor3Value, binding.barAdvisor3, binding.tvAdvisor3Name);
        bindAdvisorBar(3, advisors, max, binding.groupAdvisor4, binding.tvAdvisor4Value, binding.barAdvisor4, binding.tvAdvisor4Name);
    }

    private void bindAdvisorBar(int index, List<ReportAdvisorMetric> advisors, int max, LinearLayout group, TextView value, View bar, TextView name) {
        if (index >= advisors.size()) {
            group.setVisibility(View.INVISIBLE);
            return;
        }

        ReportAdvisorMetric advisor = advisors.get(index);
        group.setVisibility(View.VISIBLE);
        value.setText(String.valueOf(advisor.separations));
        name.setText(shortName(advisor.name));

        int height = advisor.separations == 0 ? 8 : Math.max(24, Math.round((advisor.separations * 118f) / max));
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bar.getLayoutParams();
        params.height = height;
        bar.setLayoutParams(params);
    }

    private void renderInventory(int totalSeparations, int projectCount) {
        int totalUnits = Math.max(24, projectCount * 28);
        int occupied = Math.min(88, totalSeparations * 3);
        int available = Math.max(12, 100 - occupied);

        binding.tvInventarioTotal.setText(totalUnits + " unidades evaluadas");
        binding.progressDisponible.setProgress(available);
        binding.progressReservado.setProgress(occupied);
        binding.tvDisponiblePct.setText(available + "%");
        binding.tvOcupadoPct.setText(occupied + "%");
    }

    private void renderProjectRows(List<ReportProjectMetric> projects) {
        binding.tvTopProyectosLabel.setText(projects.size() <= 1 ? "Detalle" : "Top " + Math.min(4, projects.size()));
        binding.tvProjectsEmpty.setVisibility(projects.isEmpty() ? View.VISIBLE : View.GONE);

        long max = 1L;
        for (ReportProjectMetric project : projects) {
            max = Math.max(max, project.sales);
        }

        bindProjectRow(0, projects, max, binding.groupProject1, binding.tvProject1Name, binding.tvProject1Amount, binding.progressProject1);
        bindProjectRow(1, projects, max, binding.groupProject2, binding.tvProject2Name, binding.tvProject2Amount, binding.progressProject2);
        bindProjectRow(2, projects, max, binding.groupProject3, binding.tvProject3Name, binding.tvProject3Amount, binding.progressProject3);
        bindProjectRow(3, projects, max, binding.groupProject4, binding.tvProject4Name, binding.tvProject4Amount, binding.progressProject4);
    }

    private void bindProjectRow(int index, List<ReportProjectMetric> projects, long max, LinearLayout group, TextView name, TextView amount, ProgressBar progress) {
        if (index >= projects.size()) {
            group.setVisibility(View.GONE);
            return;
        }

        ReportProjectMetric project = projects.get(index);
        group.setVisibility(View.VISIBLE);
        name.setText(project.name);
        amount.setText(formatSoles(project.sales));
        progress.setProgress((int) Math.max(4, (project.sales * 100L) / max));
    }

    private String buildFilterSummary(String period, String advisor, String project, int advisors, int projects) {
        String scopeAdvisor = ALL_ADVISORS.equals(advisor) ? advisors + " asesores" : advisor;
        String scopeProject = ALL_PROJECTS.equals(project) ? projects + " proyectos" : project;
        return period + " | " + scopeAdvisor + " | " + scopeProject;
    }

    private long getProjectBaseSales(int index) {
        long[] values = {2100000L, 1500000L, 1200000L, 900000L, 600000L, 420000L};
        return values[index % values.length];
    }

    private int getProjectBaseSeparations(int index) {
        int[] values = {12, 9, 7, 6, 4, 3};
        return values[index % values.length];
    }

    private double periodFactor(String period) {
        if (PERIOD_TODAY.equals(period)) {
            return 0.12;
        }
        if (PERIOD_YEAR.equals(period)) {
            return 1.0;
        }
        return 0.48;
    }

    private String selected(android.widget.Spinner spinner) {
        Object value = spinner.getSelectedItem();
        return value == null ? "" : String.valueOf(value);
    }

    private int indexOf(List<String> values, String target, int fallback) {
        int index = values.indexOf(target);
        return index >= 0 ? index : fallback;
    }

    private boolean containsProject(List<String> projects, String projectName) {
        for (String project : projects) {
            if (projectName.equalsIgnoreCase(project)) {
                return true;
            }
        }
        return false;
    }

    private String shortName(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].toUpperCase(Locale.ROOT);
        }
        return (parts[0] + " " + parts[1].charAt(0) + ".").toUpperCase(Locale.ROOT);
    }

    private String formatSoles(long amount) {
        if (amount >= 1000000L) {
            double millions = amount / 1000000d;
            return String.format(Locale.US, "S/ %.1fM", millions);
        }
        if (amount >= 1000L) {
            return "S/ " + (amount / 1000L) + "K";
        }
        return "S/ " + amount;
    }

    private static class ReportAdvisorMetric {
        final String name;
        final List<String> projectNames;
        final long baseSales;
        final int baseSeparations;
        final long sales;
        final int separations;

        ReportAdvisorMetric(String name, List<String> projectNames, long baseSales, int baseSeparations) {
            this(name, projectNames, baseSales, baseSeparations, baseSales, baseSeparations);
        }

        ReportAdvisorMetric(String name, List<String> projectNames, long baseSales, int baseSeparations, long sales, int separations) {
            this.name = name;
            this.projectNames = projectNames;
            this.baseSales = baseSales;
            this.baseSeparations = baseSeparations;
            this.sales = sales;
            this.separations = separations;
        }

        ReportAdvisorMetric scaled(double factor, String selectedProject) {
            int projectCount = Math.max(1, projectNames.size());
            double projectShare = ALL_PROJECTS.equals(selectedProject) ? 1.0 : 1.0 / projectCount;
            long scaledSales = Math.round(baseSales * factor * projectShare);
            int scaledSeparations = Math.max(scaledSales > 0 ? 1 : 0, (int) Math.round(baseSeparations * factor * projectShare));
            return new ReportAdvisorMetric(name, projectNames, baseSales, baseSeparations, scaledSales, scaledSeparations);
        }
    }

    private static class ReportProjectMetric {
        final String name;
        final long baseSales;
        final int baseSeparations;
        final long sales;
        final int separations;

        ReportProjectMetric(String name, long baseSales, int baseSeparations) {
            this(name, baseSales, baseSeparations, baseSales, baseSeparations);
        }

        ReportProjectMetric(String name, long baseSales, int baseSeparations, long sales, int separations) {
            this.name = name;
            this.baseSales = baseSales;
            this.baseSeparations = baseSeparations;
            this.sales = sales;
            this.separations = separations;
        }

        ReportProjectMetric scaled(double factor) {
            long scaledSales = Math.round(baseSales * factor);
            int scaledSeparations = Math.max(scaledSales > 0 ? 1 : 0, (int) Math.round(baseSeparations * factor));
            return new ReportProjectMetric(name, baseSales, baseSeparations, scaledSales, scaledSeparations);
        }
    }
}
