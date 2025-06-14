package com.example.veggiecoach;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private List<CalendarActivity.ScheduleItem> scheduleList;

    public ScheduleAdapter(List<CalendarActivity.ScheduleItem> scheduleList) {
        this.scheduleList = scheduleList;
    }

    @NonNull
    @Override
    public ScheduleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleAdapter.ViewHolder holder, int position) {
        CalendarActivity.ScheduleItem item = scheduleList.get(position);
        holder.tvContent.setText(item.getContent());
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(android.R.id.text1);
        }
    }

    public void updateList(List<CalendarActivity.ScheduleItem> newList) {
        this.scheduleList = newList;
        notifyDataSetChanged();
    }
}
