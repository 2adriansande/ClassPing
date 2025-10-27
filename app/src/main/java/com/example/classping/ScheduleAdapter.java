package com.example.classping;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private List<Schedule> schedules;
    private ScheduleManager manager;
    private Runnable refreshCallback;

    public ScheduleAdapter(List<Schedule> schedules, ScheduleManager manager, Runnable refreshCallback) {
        this.schedules = schedules;
        this.manager = manager;
        this.refreshCallback = refreshCallback;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
        return new ScheduleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        Schedule s = schedules.get(position);
        holder.tvSubject.setText(s.getSubject());
        holder.tvSection.setText("Section: " + (s.getProgram() == null || s.getProgram().isEmpty() ? "TBA" : s.getProgram()));
        holder.tvTime.setText(formatTime(s.getStartTime()) + " - " + formatTime(s.getEndTime()));
        holder.tvRoom.setText("Room: " + (s.getRoom() == null || s.getRoom().isEmpty() ? "TBA" : s.getRoom()));

        // Edit schedule
        holder.btnEdit.setOnClickListener(v ->
                DialogHelper.showScheduleDialog(holder.itemView.getContext(), s, updatedSchedule -> {
                    manager.updateSchedule(s.getId(), updatedSchedule);
                    refreshCallback.run();
                })
        );

        // Delete schedule
        holder.btnDelete.setOnClickListener(v -> {
            manager.deleteSchedule(s.getId());
            refreshCallback.run();
        });
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    public void updateSchedules(List<Schedule> newSchedules) {
        this.schedules = newSchedules;
        notifyDataSetChanged();
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubject, tvSection, tvTime, tvRoom;
        ImageButton btnEdit, btnDelete;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvSection = itemView.findViewById(R.id.tvSection);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvRoom = itemView.findViewById(R.id.tvRoom);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private String formatTime(String hhmm) {
        try {
            String[] p = hhmm.split(":");
            int hh = Integer.parseInt(p[0]);
            int mm = Integer.parseInt(p[1]);
            String ampm = (hh >= 12) ? "PM" : "AM";
            int disH = (hh % 12 == 0) ? 12 : hh % 12;
            return String.format("%d:%02d %s", disH, mm, ampm);
        } catch (Exception ex) {
            return hhmm;
        }
    }
}
