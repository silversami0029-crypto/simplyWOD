package com.example.fitwod;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bessadi.fitwod.R;

import java.util.List;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.ViewHolder> {
    private List<PersonalRecord> records;

    public void setRecords(List<PersonalRecord> records) {
        this.records = records;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_personal_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PersonalRecord record = records.get(position);
        holder.tvWorkoutType.setText(record.workoutType);
        holder.tvMetric.setText(record.metric);
        holder.tvValue.setText(record.value);
        holder.tvDate.setText(record.date);
    }

    @Override
    public int getItemCount() {
        return records != null ? records.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvWorkoutType, tvMetric, tvValue, tvDate;

        ViewHolder(View itemView) {
            super(itemView);
            tvWorkoutType = itemView.findViewById(R.id.tvWorkoutType);
            tvMetric = itemView.findViewById(R.id.tvMetric);
            tvValue = itemView.findViewById(R.id.tvValue);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}