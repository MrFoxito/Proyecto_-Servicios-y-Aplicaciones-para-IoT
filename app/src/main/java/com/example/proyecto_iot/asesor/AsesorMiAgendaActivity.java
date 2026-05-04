package com.example.proyecto_iot.asesor;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
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
import java.util.List;
import java.util.Locale;

public class AsesorMiAgendaActivity extends BaseAsesorActivity {

    private RecyclerView rvCalendar, rvTimeline;
    private CalendarAdapter calendarAdapter;
    private TimelineAdapter timelineAdapter;
    private List<CalendarDay> calendarDays;
    private List<Cita> allCitas;
    private List<Cita> displayCitas;
    private TextView txtMonthYear, filterHoy, filterSemana, filterMes;
    private Calendar currentCalendar;
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asesor_miagenda);

        setupBottomNavigation(R.id.navMiAgenda);

        // Inicializar Vistas
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

        // Selección de Mes/Año al clickear el título
        txtMonthYear.setOnClickListener(v -> showMonthYearPicker());

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

        // Citas de ejemplo
        allCitas.add(new Cita("1", "Alicia Velarde", "Penthouse Altos del Bosque", "08:30 AM", todayStr, "Confirmada", R.drawable.as_property_01));
        allCitas.add(new Cita("2", "Inversiones Valero", "Penthouse El Cielo", "11:15 AM", todayStr, "En Camino", R.drawable.as_property_02));
        allCitas.add(new Cita("3", "Julian Ortega", "Residencia Brisa", "02:45 PM", todayStr, "Pendiente", R.drawable.as_property_03));
        allCitas.add(new Cita("4", "Maria Garcia", "Condominio Pacifico", "10:00 AM", yesterdayStr, "Pasada", R.drawable.as_property_04));
        allCitas.add(new Cita("5", "Roberto Carlos", "Villa del Mar", "04:30 PM", tomorrowStr, "Confirmada", R.drawable.as_property_05));
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
        
        // Calcular el día de la semana del primer día del mes (Lunes = 0 en nuestro grid L-D)
        // Calendar.SUNDAY = 1, MONDAY = 2, ...
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK); 
        int offset = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - 2;

        // Retroceder cal para incluir días del mes anterior para completar la semana
        cal.add(Calendar.DAY_OF_MONTH, -offset);

        // Llenar 42 celdas (6 semanas) para un calendario realista y exacto
        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 0; i < 42; i++) {
            boolean isToday = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                              cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
            
            CalendarDay day = new CalendarDay(cal.getTime(), cal.get(Calendar.DAY_OF_MONTH), "", isToday);
            
            // Lógica de indicadores (puntos)
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
            cal.add(Calendar.DAY_OF_MONTH, 1); // Avanzar al siguiente día
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
        displayCitas = new ArrayList<>();
        timelineAdapter = new TimelineAdapter(displayCitas, cita -> openScreen(AsesorDetalleCitaActivity.class));
        rvTimeline.setAdapter(timelineAdapter);
        
        filterCitasByDate(Calendar.getInstance().getTime());
    }

    private void filterCitasByDate(java.util.Date date) {
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
        displayCitas.clear();
        for (Cita c : allCitas) {
            if (c.getDate().equals(dateStr)) {
                displayCitas.add(c);
            }
        }
        timelineAdapter.notifyDataSetChanged();
        
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
            filterCitasInRange(start, end);
//            findViewById(R.id.txtTimelineLabel).setText("ITINERARIO DE LA SEMANA");
        });

        filterMes.setOnClickListener(v -> {
            setActiveFilter(filterMes);
            displayCitas.clear();
            displayCitas.addAll(allCitas);
            timelineAdapter.notifyDataSetChanged();
//            findViewById(R.id.txtTimelineLabel).setText("ITINERARIO DEL MES");
        });
    }

    private void filterCitasInRange(Calendar start, Calendar end) {
        displayCitas.clear();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (Cita c : allCitas) {
            try {
                java.util.Date citaDate = sdf.parse(c.getDate());
                if (citaDate != null && !citaDate.before(start.getTime()) && !citaDate.after(end.getTime())) {
                    displayCitas.add(c);
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
        timelineAdapter.notifyDataSetChanged();
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
