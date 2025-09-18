package com.example.fitwod;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bessadi.fitwod.R;

import java.util.List;
public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.ViewHolder> {
    private List<Achievement> achievements;

    public AchievementAdapter(List<Achievement> achievements) {
        this.achievements = achievements;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);

        holder.icon.setImageResource(achievement.getIconResId());
        holder.title.setText(achievement.getTitle());
        holder.description.setText(achievement.getDescription());

        if (achievement.isUnlocked()) {
            holder.itemView.setAlpha(1f);
            holder.lockIcon.setVisibility(View.GONE);
        } else {
            holder.itemView.setAlpha(0.5f);
            holder.lockIcon.setVisibility(View.VISIBLE);
        }
    }
    public int getItemCount() {
        return achievements.size(); // ← THIS IS THE MISSING METHOD!
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon, lockIcon;
        TextView title, description;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.ivAchievementIcon);
            lockIcon = itemView.findViewById(R.id.ivLock);
            title = itemView.findViewById(R.id.tvTitle);
            description = itemView.findViewById(R.id.tvDescription);
        }
    }
}