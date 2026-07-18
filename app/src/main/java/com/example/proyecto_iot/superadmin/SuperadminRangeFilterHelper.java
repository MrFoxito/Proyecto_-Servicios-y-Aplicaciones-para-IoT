package com.example.proyecto_iot.superadmin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

final class SuperadminRangeFilterHelper {

    static final Locale ES_LOCALE = new Locale("es", "ES");
    private static final Locale DISPLAY_LOCALE = Locale.US;
    private static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat DISPLAY_FORMAT = new SimpleDateFormat("dd MMM yyyy", ES_LOCALE);
    private static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("dd MMM", ES_LOCALE);
    private static final SimpleDateFormat DISPLAY_INPUT_FORMAT = new SimpleDateFormat("dd MMM yyyy", DISPLAY_LOCALE);

    private SuperadminRangeFilterHelper() {
    }

    static DateRange presetRange(Preset preset, int amount) {
        Calendar end = Calendar.getInstance();
        Calendar start = (Calendar) end.clone();
        switch (preset) {
            case DAYS:
                start.add(Calendar.DAY_OF_YEAR, -amount);
                break;
            case MONTHS:
                start.add(Calendar.MONTH, -amount);
                break;
            case YEARS:
                start.add(Calendar.YEAR, -amount);
                break;
        }
        return new DateRange(start.getTime(), end.getTime());
    }

    static DateRange normalize(Date start, Date end) {
        if (start == null || end == null) {
            Date now = new Date();
            return new DateRange(now, now);
        }
        if (end.before(start)) {
            Date swap = start;
            start = end;
            end = swap;
        }
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(end);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        return new DateRange(start, endCal.getTime());
    }

    static boolean withinIsoRange(String isoDate, DateRange range) {
        Date date = parseIso(isoDate);
        return date != null && range.contains(date);
    }

    static boolean withinDisplayRange(String displayDate, DateRange range) {
        Date date = parseDisplay(displayDate);
        return date != null && range.contains(date);
    }

    static String formatRange(DateRange range) {
        return DISPLAY_FORMAT.format(range.start) + " - " + DISPLAY_FORMAT.format(range.end);
    }

    static String formatDate(Date date) {
        return DISPLAY_FORMAT.format(date);
    }

    static String formatLogTimestamp(String isoDate, String time) {
        Date date = parseIso(isoDate);
        if (date == null) {
            return time == null ? "" : time;
        }
        if (time == null || time.trim().isEmpty()) {
            return SHORT_DATE_FORMAT.format(date);
        }
        return SHORT_DATE_FORMAT.format(date) + " · " + time;
    }

    static Date parseIso(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return ISO_FORMAT.parse(value.trim());
        } catch (ParseException ignored) {
            return null;
        }
    }

    static Date parseDisplay(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return DISPLAY_INPUT_FORMAT.parse(value.trim());
        } catch (ParseException ignored) {
            try {
                return new SimpleDateFormat("dd MMM yyyy", ES_LOCALE).parse(value.trim());
            } catch (ParseException ignoredToo) {
                return null;
            }
        }
    }

    enum Preset {
        DAYS,
        MONTHS,
        YEARS
    }

    static final class DateRange {
        final Date start;
        final Date end;

        DateRange(Date start, Date end) {
            this.start = start;
            this.end = end;
        }

        boolean contains(Date date) {
            return !date.before(start) && !date.after(end);
        }
    }
}
