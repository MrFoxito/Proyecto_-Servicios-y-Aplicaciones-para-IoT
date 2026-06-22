package com.example.proyecto_iot.asesor;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.proyecto_iot.AuthSessionManager;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.data.FirebaseAppointmentRepository;
import com.example.proyecto_iot.databinding.ActivityAsesorMiagendaBinding;
import com.example.proyecto_iot.databinding.ItemAsesorCalendarDayBinding;
import com.example.proyecto_iot.entity.Cita;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.ListenerRegistration;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AsesorMiAgendaActivity extends BaseAsesorActivity {

    private ActivityAsesorMiagendaBinding binding;
    private TimelineAdapter timelineAdapter;
    private final List<Cita> allCitas = new ArrayList<>();
    private final List<Object> displayItems = new ArrayList<>();

    private LocalDate selectedDate = LocalDate.now();
    private LocalDate rangeStart = LocalDate.now();
    private LocalDate rangeEnd = LocalDate.now();

    private final DateTimeFormatter monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"));
    private final DateTimeFormatter selectionLabelFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", new Locale("es", "ES"));

    private AuthSessionManager sessionManager;
    private FirebaseAppointmentRepository repository;
    private ListenerRegistration citasListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAsesorMiagendaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNavigation(R.id.navMiAgenda);

        sessionManager = AuthSessionManager.getInstance(this);
        repository = new FirebaseAppointmentRepository();

        setupCalendar();
        setupTimeline();
        setupFilters();

        loadCitasFromFirestore();

        binding.btnHistorial.setOnClickListener(v -> openScreen(AsesorHistorialCitasActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (citasListener != null) {
            citasListener.remove();
        }
    }

    private void loadCitasFromFirestore() {
        String asesorId = sessionManager.getUid();
        if (asesorId == null || asesorId.isEmpty()) return;

        citasListener = repository.listenAdvisorAppointments(asesorId, new FirebaseAppointmentRepository.AppointmentsCallback() {
            @Override
            public void onSuccess(List<Cita> citas) {
                allCitas.clear();
                allCitas.addAll(citas);
                if (!isFinishing() && !isDestroyed()) {
                    binding.calendarView.notifyCalendarChanged();
                    filterCitasByDate(selectedDate);
                }
            }

            @Override
            public void onError(String message) {
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(AsesorMiAgendaActivity.this, "Error al cargar citas: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupCalendar() {
        binding.calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, CalendarDay day) {
                container.day = day;
                ItemAsesorCalendarDayBinding itemBinding = container.binding;
                LocalDate date = day.getDate();

                itemBinding.txtDayNumber.setText(String.valueOf(date.getDayOfMonth()));

                if (day.getPosition() != DayPosition.MonthDate) {
                    itemBinding.txtDayNumber.setTextColor(Color.parseColor("#9AA3AF"));
                    itemBinding.getRoot().setAlpha(0.3f);
                    itemBinding.viewRange.setVisibility(View.GONE);
                    itemBinding.viewSelected.setVisibility(View.GONE);
                } else {
                    itemBinding.getRoot().setAlpha(1f);

                    boolean inRange = (date.isAfter(rangeStart) || date.isEqual(rangeStart)) &&
                            (date.isBefore(rangeEnd) || date.isEqual(rangeEnd));

                    itemBinding.viewRange.setVisibility(inRange ? View.VISIBLE : View.GONE);

                    boolean isSelected = date.equals(selectedDate);
                    itemBinding.viewSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

                    if (isSelected) {
                        itemBinding.txtDayNumber.setTextColor(Color.WHITE);
                        itemBinding.viewRange.setVisibility(View.GONE);
                    } else if (inRange) {
                        itemBinding.txtDayNumber.setTextColor(Color.parseColor("#7A5C0D"));
                    } else if (date.equals(LocalDate.now())) {
                        itemBinding.txtDayNumber.setTextColor(Color.parseColor("#8F7E00"));
                        itemBinding.txtDayNumber.setPaintFlags(itemBinding.txtDayNumber.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    } else {
                        itemBinding.txtDayNumber.setTextColor(Color.parseColor("#0B1D2A"));
                        itemBinding.txtDayNumber.setPaintFlags(itemBinding.txtDayNumber.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
                    }
                }
                updateDayIndicators(itemBinding, date, day.getPosition());
            }
        });

        YearMonth currentMonth = YearMonth.now();
        binding.calendarView.setup(currentMonth.minusMonths(12), currentMonth.plusMonths(12), DayOfWeek.MONDAY);
        binding.calendarView.scrollToMonth(currentMonth);

        binding.calendarView.setMonthScrollListener(calendarMonth -> {
            String title = monthTitleFormatter.format(calendarMonth.getYearMonth());
            binding.txtMonthYear.setText(title.substring(0, 1).toUpperCase() + title.substring(1));
            return null;
        });

        binding.btnPrevMonth.setOnClickListener(v ->
                binding.calendarView.smoothScrollToMonth(binding.calendarView.findFirstVisibleMonth().getYearMonth().minusMonths(1)));

        binding.btnNextMonth.setOnClickListener(v ->
                binding.calendarView.smoothScrollToMonth(binding.calendarView.findFirstVisibleMonth().getYearMonth().plusMonths(1)));
    }

    private void setupFilters() {
        binding.chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            for (int i = 0; i < group.getChildCount(); i++) {
                Chip chip = (Chip) group.getChildAt(i);
                chip.setTextColor(ContextCompat.getColor(this, R.color.app_text_primary));
            }
            if (checkedIds.isEmpty()) return;

            binding.calendarView.smoothScrollToMonth(YearMonth.now());
            int checkedId = checkedIds.get(0);
            Chip selChip = group.findViewById(checkedId);
            selChip.setTextColor(ContextCompat.getColor(this, R.color.white));
            if (checkedId == R.id.chipHoy) {
                rangeStart = LocalDate.now();
                rangeEnd = LocalDate.now();
                selectDate(LocalDate.now());
            } else if (checkedId == R.id.chipSemana) {
                rangeStart = LocalDate.now();
                rangeEnd = rangeStart.plusDays(6);
                filterCitasRange(rangeStart, rangeEnd, "ESTA SEMANA");
            } else if (checkedId == R.id.chipMes) {
                YearMonth current = YearMonth.now();
                rangeStart = current.atDay(1);
                rangeEnd = current.atEndOfMonth();
                filterCitasRange(rangeStart, rangeEnd, "ESTE MES");
            }
            binding.calendarView.notifyCalendarChanged();
        });

        binding.chipHoy.setChecked(true);
    }

    private void selectDate(LocalDate date) {
        LocalDate oldDate = selectedDate;
        selectedDate = date;
        binding.calendarView.notifyDateChanged(oldDate);
        binding.calendarView.notifyDateChanged(selectedDate);
        filterCitasByDate(date);
    }

    private void updateDayIndicators(ItemAsesorCalendarDayBinding itemBinding, LocalDate date, DayPosition position) {
        List<Cita> dayCitas = getCitasForDate(date);
        itemBinding.dotPast.setVisibility(View.GONE);
        itemBinding.dotConfirmed.setVisibility(View.GONE);
        itemBinding.dotPending.setVisibility(View.GONE);

        if (!dayCitas.isEmpty() && position == DayPosition.MonthDate) {
            for (Cita c : dayCitas) {
                String status = c.getEstado() != null ? c.getEstado().toLowerCase() : "";
                if (status.contains("pasada") || status.contains("pasado")) {
                    itemBinding.dotPast.setVisibility(View.VISIBLE);
                } else if (status.contains("confirmada")) {
                    itemBinding.dotConfirmed.setVisibility(View.VISIBLE);
                } else {
                    itemBinding.dotPending.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void setupTimeline() {
        binding.rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        timelineAdapter = new TimelineAdapter(displayItems, cita -> {
             Intent intent = new Intent(this, AsesorDetalleCitaActivity.class);
             intent.putExtra(AsesorDetalleCitaActivity.EXTRA_CITA_ID, cita.getId());
             startActivity(intent);
             overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
        binding.rvTimeline.setAdapter(timelineAdapter);
        filterCitasByDate(LocalDate.now());
    }

    private void filterCitasByDate(LocalDate date) {
        String dateStr = date.toString();
        List<Cita> filtered = allCitas.stream()
                .filter(c -> c.getFechaISO() != null && c.getFechaISO().equals(dateStr))
                .collect(Collectors.toList());
        populateTimeline(filtered, false);
        binding.txtTimelineLabel.setText("ITINERARIO DEL " + selectionLabelFormatter.format(date).toUpperCase());
    }

    private void filterCitasRange(LocalDate start, LocalDate end, String label) {
        List<Cita> filtered = allCitas.stream()
                .filter(c -> {
                    if (c.getFechaISO() == null) return false;
                    try {
                        LocalDate citaDate = LocalDate.parse(c.getFechaISO());
                        return !citaDate.isBefore(start) && !citaDate.isAfter(end);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
        populateTimeline(filtered, true);
        binding.txtTimelineLabel.setText("ITINERARIO DE " + label);
    }

    private void populateTimeline(List<Cita> citas, boolean showSeparators) {
        displayItems.clear();
        
        citas.sort((c1, c2) -> {
            int d = (c1.getFechaISO() != null && c2.getFechaISO() != null) ? c1.getFechaISO().compareTo(c2.getFechaISO()) : 0;
            if (d != 0) return d;
            return (c1.getHora() != null && c2.getHora() != null) ? c1.getHora().compareTo(c2.getHora()) : 0;
        });

        if (!showSeparators) {
            displayItems.addAll(citas);
        } else {
            String lastDate = "";
            for (Cita cita : citas) {
                String fecha = cita.getFechaISO();
                if (fecha != null && !fecha.equals(lastDate)) {
                    try {
                        LocalDate d = LocalDate.parse(fecha);
                        String header = selectionLabelFormatter.format(d);
                        displayItems.add(header.substring(0, 1).toUpperCase() + header.substring(1));
                        lastDate = fecha;
                    } catch (Exception ignored) {}
                }
                displayItems.add(cita);
            }
        }
        timelineAdapter.notifyDataSetChanged();
    }

    private List<Cita> getCitasForDate(LocalDate date) {
        String s = date.toString();
        return allCitas.stream()
                .filter(c -> c.getFechaISO() != null && c.getFechaISO().equals(s))
                .collect(Collectors.toList());
    }

    private class DayViewContainer extends ViewContainer {
        CalendarDay day;
        ItemAsesorCalendarDayBinding binding;

        DayViewContainer(View view) {
            super(view);
            binding = ItemAsesorCalendarDayBinding.bind(view);
            view.setOnClickListener(v -> {
                if (day.getPosition() == DayPosition.MonthDate) {
                    rangeStart = day.getDate();
                    rangeEnd = day.getDate();
                    selectDate(day.getDate());
                    AsesorMiAgendaActivity.this.binding.calendarView.notifyCalendarChanged();
                }
            });
        }
    }
}
