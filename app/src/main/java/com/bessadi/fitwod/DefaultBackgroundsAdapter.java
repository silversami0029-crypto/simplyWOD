package com.bessadi.fitwod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bessadi.fitwod.R;

import java.util.List;

public class DefaultBackgroundsAdapter extends BaseAdapter {
    private Context context;
    private List<Integer> backgroundDrawables;
    private int selectedPosition = -1;

    public DefaultBackgroundsAdapter(Context context, List<Integer> backgroundDrawables) {
        this.context = context;
        this.backgroundDrawables = backgroundDrawables;
    }

    @Override
    public int getCount() {
        return backgroundDrawables.size();
    }

    @Override
    public Object getItem(int position) {
        return backgroundDrawables.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_background, parent, false);
            holder = new ViewHolder();
            holder.backgroundImage = convertView.findViewById(R.id.backgroundImage);
            holder.selectionIndicator = convertView.findViewById(R.id.selectionIndicator);
            holder.borderIndicator = convertView.findViewById(R.id.borderIndicator);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.backgroundImage.setImageResource(backgroundDrawables.get(position));

        // Show/hide selection indicators
        boolean isSelected = (position == selectedPosition);
        holder.selectionIndicator.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        holder.borderIndicator.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        return convertView;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public int getSelectedBackground() {
        if (selectedPosition >= 0 && selectedPosition < backgroundDrawables.size()) {
            return backgroundDrawables.get(selectedPosition);
        }
        return -1;
    }

    private static class ViewHolder {
        ImageView backgroundImage;
        ImageView selectionIndicator;
        View borderIndicator;
    }
}