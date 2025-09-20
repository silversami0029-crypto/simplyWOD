package com.bessadi.fitwod;
import android.content.Context;
import android.widget.TextView;

import com.bessadi.fitwod.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import java.util.Arrays;
import java.util.List;

public class YourMarkerView extends MarkerView {
    private final TextView tvDate;
    private final TextView tvValue;
    private List<String> dates;

    public YourMarkerView(Context context, int layoutResource, Object dates) {
        super(context, layoutResource);
        tvDate = findViewById(R.id.tvDate);
        tvValue = findViewById(R.id.tvValue);

        if (dates instanceof List) {
            this.dates = (List<String>) dates;
        } else if (dates instanceof String[]) {
            this.dates = Arrays.asList((String[]) dates);
        }
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        if (dates != null) {
            int index = (int) e.getX();
            if (index >= 0 && index < dates.size()) { // Now using size() for List
                tvDate.setText(dates.get(index));
            }
        }

        int totalSeconds = (int) e.getY();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String valueText = String.format("%d:%02d", minutes, seconds);
        tvValue.setText(valueText);

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}