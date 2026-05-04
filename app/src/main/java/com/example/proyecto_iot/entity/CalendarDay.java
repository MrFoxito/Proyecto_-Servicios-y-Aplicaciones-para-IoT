package com.example.proyecto_iot.entity;

import java.util.Date;

public class CalendarDay {
    private Date date;
    private int dayNumber;
    private String dayOfWeek;
    private boolean isToday;
    private boolean isSelected;

    private boolean isOffset;
    private boolean hasEvents;
    private boolean hasPastEvents;
    private boolean hasConfirmedFutureEvents;

    public CalendarDay(Date date, int dayNumber, String dayOfWeek, boolean isToday) {
        this.date = date;
        this.dayNumber = dayNumber;
        this.dayOfWeek = dayOfWeek;
        this.isToday = isToday;
        this.isSelected = isToday;
    }

    public Date getDate() { return date; }
    public int getDayNumber() { return dayNumber; }
    public String getDayOfWeek() { return dayOfWeek; }
    public boolean isToday() { return isToday; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }

    public boolean isOffset() { return isOffset; }

    public void setOffset(boolean offset) { isOffset = offset; }
    public boolean isHasEvents() { return hasEvents; }
    public void setHasEvents(boolean hasEvents) { this.hasEvents = hasEvents; }
    public boolean isHasPastEvents() { return hasPastEvents; }
    public void setHasPastEvents(boolean hasPastEvents) { this.hasPastEvents = hasPastEvents; }
    public boolean isHasConfirmedFutureEvents() { return hasConfirmedFutureEvents; }
    public void setHasConfirmedFutureEvents(boolean hasConfirmedFutureEvents) { this.hasConfirmedFutureEvents = hasConfirmedFutureEvents; }
}
