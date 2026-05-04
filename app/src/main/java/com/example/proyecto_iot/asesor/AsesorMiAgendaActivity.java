package com.example.proyecto_iot.asesor;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.entity.CalendarDay;
import com.example.proyecto_iot.entity.Cita;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AsesorMiAgendaActivity extends BaseAsesorActivity {

    private RecyclerView rvCalendar, rvTimeline;
    private CalendarAdapter calendarAdapter;
    private TimelineAdapter timelineAdapter;
    private List<CalendarDay> calendarDays;
    private List<Cita> allCitas;
    private List<Object> displayItems; // Usamos Object para manejar Citas y Strings (separadores)
    private ImageButton btnHistorial;
    private TextView txtMonthYear, filterHoy, filterSemana, filterMes;
    private Calendar currentCalendar;
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_miagenda);

        setupBottomNavigation(R.id.navMiAgenda);

        // Inicializar Vistas
        btnHistorial = findViewById(R.id.btnHistorial);
        txtMonthYear = findViewById(R.id.txtMonthYear);
        filterHoy = findViewById(R.id.filterHoy);
        filterSemana = findViewById(R.id.filterSemana);
        filterMes = findViewById(R.id.filterMes);
        rvCalendar = findViewById(R.id.rvCalendar);
        rvTimeline = findViewById(R.id.rvTimeline);

        currentCalendar = Calendar.getInstance();
        
        loadHardcodedCitas();
        setupCalendar();
        setupTimeline();
        setupFilters();

        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        txtMonthYear.setOnClickListener(v -> showMonthYearPicker());
        btnHistorial.setOnClickListener(v -> openScreen(AsesorHistorialCitasActivity.class));
    }

    private void loadHardcodedCitas() {
        allCitas = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        String todayStr = sdf.format(cal.getTime());
        
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterdayStr = sdf.format(cal.getTime());
        
        cal.add(Calendar.DAY_OF_YEAR, 2);
        String tomorrowStr = sdf.format(cal.getTime());

        // Citas de ejemplo (desordenadas para probar el ordenamiento)
        allCitas.add(new Cita("2", "Inversiones Valero", "Penthouse El Cielo", "11:15 AM", todayStr, "En Camino"));
        allCitas.add(new Cita("1", "Alicia Velarde", "Penthouse Altos del Bosque", "08:30 AM", todayStr, "Confirmada"));
        allCitas.add(new Cita("3", "Julian Ortega", "Residencia Brisa", "02:45 PM", todayStr, "Pendiente"));
        allCitas.add(new Cita("4", "Maria Garcia", "Condominio Pacifico", "10:00 AM", yesterdayStr, "Pasada"));
        allCitas.add(new Cita("5", "Roberto Carlos", "Villa del Mar", "04:30 PM", tomorrowStr, "Confirmada"));
        
        // Ordenar inicialmente
        sortCitas(allCitas);
    }

    private void setupCalendar() {
        rvCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        calendarDays = new ArrayList<>();
        updateCalendar();
    }

    private void updateCalendar() {
        calendarDays.clear();
        String monthName = monthYearFormat.format(currentCalendar.getTime());
        txtMonthYear.setText(monthName.substring(0, 1).toUpperCase() + monthName.substring(1));

        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); 
        int offset = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - 2;

        cal.add(Calendar.DAY_OF_MONTH, -offset);

        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 0; i < 42; i++) {
            boolean isToday = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                              cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
            
            CalendarDay day = new CalendarDay(cal.getTime(), cal.get(Calendar.DAY_OF_MONTH), "", isToday);
            day.setOffset(cal.get(Calendar.MONTH) != currentCalendar.get(Calendar.MONTH)
                    || cal.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR));

            String dateStr = sdf.format(cal.getTime());
            for (Cita c : allCitas) {
                if (c.getDate().equals(dateStr)) {
                    day.setHasEvents(true);
                    if ("Pasada".equalsIgnoreCase(c.getStatus())) {
                        day.setHasPastEvents(true);
                    } else if ("Confirmada".equalsIgnoreCase(c.getStatus())) {
                        day.setHasConfirmedFutureEvents(true);
                    }
                }
            }
            calendarDays.add(day);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        calendarAdapter = new CalendarAdapter(calendarDays, day -> {
            for (CalendarDay d : calendarDays) d.setSelected(false);
            day.setSelected(true);
            calendarAdapter.notifyDataSetChanged();
            filterCitasByDate(day.getDate());
        });
        rvCalendar.setAdapter(calendarAdapter);
    }

    private void showMonthYearPicker() {
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            currentCalendar.set(Calendar.YEAR, year);
            currentCalendar.set(Calendar.MONTH, month);
            updateCalendar();
        }, currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), 1);
        dialog.show();
    }

    private void setupTimeline() {
        rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        displayItems = new ArrayList<>();
        timelineAdapter = new TimelineAdapter(displayItems, cita -> openScreen(AsesorDetalleCitaActivity.class));
        rvTimeline.setAdapter(timelineAdapter);
        
        filterCitasByDate(Calendar.getInstance().getTime());
    }

    private void filterCitasByDate(java.util.Date date) {
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
        List<Cita> filtered = new ArrayList<>();
        for (Cita c : allCitas) {
            if (c.getDate().equals(dateStr)) {
                filtered.add(c);
            }
        }
        populateTimeline(filtered, false); // Sin separadores para un solo día
        
        TextView txtTimelineLabel = findViewById(R.id.txtTimelineLabel);
        String formattedDate = new SimpleDateFormat("dd 'de' MMMM", new Locale("es", "ES")).format(date);
        txtTimelineLabel.setText("ITINERARIO DEL " + formattedDate.toUpperCase());
    }

    private void setupFilters() {
        filterHoy.setOnClickListener(v -> {
            setActiveFilter(filterHoy);
            Calendar today = Calendar.getInstance();
            filterCitasByDate(today.getTime());
            selectDayInCalendar(today);
        });

        filterSemana.setOnClickListener(v -> {
            setActiveFilter(filterSemana);
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.DAY_OF_YEAR, 7);
            
            List<Cita> filtered = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            for (Cita c : allCitas) {
                try {
                    java.util.Date citaDate = sdf.parse(c.getDate());
                    if (citaDate != null && !citaDate.before(start.getTime()) && !citaDate.after(end.getTime())) {
                        filtered.add(c);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
            populateTimeline(filtered, true); // Con separadores para múltiples días
            TextView txtTimelineLabel = findViewById(R.id.txtTimelineLabel);
            txtTimelineLabel.setText("ITINERARIO DE ESTA SEMANA");
        });

        filterMes.setOnClickListener(v -> {
            setActiveFilter(filterMes);
            int targetMonth = currentCalendar.get(Calendar.MONTH);
            int targetYear = currentCalendar.get(Calendar.YEAR);

            List<Cita> filtered = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Calendar citaCal = Calendar.getInstance();
            for (Cita c : allCitas) {
                try {
                    java.util.Date citaDate = sdf.parse(c.getDate());
                    if (citaDate != null) {
                        citaCal.setTime(citaDate);
                        if (citaCal.get(Calendar.MONTH) == targetMonth && citaCal.get(Calendar.YEAR) == targetYear) {
                            filtered.add(c);
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
            populateTimeline(filtered, true); // Con separadores
            TextView txtTimelineLabel = findViewById(R.id.txtTimelineLabel);
            txtTimelineLabel.setText("ITINERARIO DE ESTE MES");
        });
    }

    private void populateTimeline(List<Cita> citas, boolean showSeparators) {
        displayItems.clear();
        if (citas.isEmpty()) {
            timelineAdapter.notifyDataSetChanged();
            return;
        }

        // 1. Ordenar cronológicamente (Fecha y luego Hora)
        sortCitas(citas);

        // 2. Construir lista con separadores
        if (!showSeparators) {
            displayItems.addAll(citas);
        } else {
            String lastDate = "";
            SimpleDateFormat sdfInput = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat sdfOutput = new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("es", "ES"));

            for (Cita cita : citas) {
                if (!cita.getDate().equals(lastDate)) {
                    try {
                        String formattedDate = sdfOutput.format(sdfInput.parse(cita.getDate()));
                        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
                        displayItems.add(formattedDate);
                    } catch (Exception e) {
                        displayItems.add(cita.getDate());
                    }
                    lastDate = cita.getDate();
                }
                displayItems.add(cita);
            }
        }
        timelineAdapter.notifyDataSetChanged();
    }

    private void sortCitas(List<Cita> citas) {
        Collections.sort(citas, (c1, c2) -> {
            int dateCompare = c1.getDate().compareTo(c2.getDate());
            if (dateCompare != 0) return dateCompare;
            
            // Ordenar por hora convirtiendo a formato 24h
            return parseTimeSortable(c1.getTime()).compareTo(parseTimeSortable(c2.getTime()));
        });
    }

    private String parseTimeSortable(String time) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.US);
            return outputFormat.format(inputFormat.parse(time));
        } catch (Exception e) {
            return time;
        }
    }

    private void selectDayInCalendar(Calendar target) {
        for (CalendarDay d : calendarDays) {
            Calendar dCal = Calendar.getInstance();
            dCal.setTime(d.getDate());
            d.setSelected(dCal.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                         dCal.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR));
        }
        calendarAdapter.notifyDataSetChanged();
    }

    private void setActiveFilter(TextView active) {
        filterHoy.setBackgroundResource(R.drawable.as_chip_light);
        filterHoy.setTextColor(Color.parseColor("#746D4A"));
        filterSemana.setBackgroundResource(R.drawable.as_chip_light);
        filterSemana.setTextColor(Color.parseColor("#746D4A"));
        filterMes.setBackgroundResource(R.drawable.as_chip_light);
        filterMes.setTextColor(Color.parseColor("#746D4A"));

        active.setBackgroundResource(R.drawable.as_chip_dark);
        active.setTextColor(Color.WHITE);
    }
}
