package com.example.proyecto_iot.asesor;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proyecto_iot.R;
import com.example.proyecto_iot.entity.CalendarDay;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private List<CalendarDay> days;
    private OnDayClickListener listener;

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }

    public CalendarAdapter(List<CalendarDay> days, OnDayClickListener listener) {
        this.days = days;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asesor_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        CalendarDay day = days.get(position);
        holder.txtDayNumber.setText(String.valueOf(day.getDayNumber()));

        if (day.isSelected()) {
            holder.viewSelected.setVisibility(View.VISIBLE);
            holder.txtDayNumber.setTextColor(Color.WHITE);
        } else {
            holder.viewSelected.setVisibility(View.GONE);
            holder.txtDayNumber.setTextColor(day.isOffset() ? Color.parseColor("#9AA3AF") : Color.parseColor("#0B1D2A"));
            holder.txtDayNumber.setTextColor(day.isToday() ? Color.parseColor("#8F7E00") : Color.parseColor("#0B1D2A"));
        }

        holder.dotPast.setVisibility(day.isHasPastEvents() ? View.VISIBLE : View.GONE);
        holder.dotConfirmed.setVisibility(day.isHasConfirmedFutureEvents() ? View.VISIBLE : View.GONE);
        holder.dotPending.setVisibility(day.isHasEvents() && !day.isHasPastEvents() && !day.isHasConfirmedFutureEvents() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> listener.onDayClick(day));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView txtDayNumber;
        View viewSelected, dotPast, dotConfirmed, dotPending;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDayNumber = itemView.findViewById(R.id.txtDayNumber);
            viewSelected = itemView.findViewById(R.id.viewSelected);
            dotPast = itemView.findViewById(R.id.dotPast);
            dotConfirmed = itemView.findViewById(R.id.dotConfirmed);
            dotPending = itemView.findViewById(R.id.dotPending);
        }
    }
}
