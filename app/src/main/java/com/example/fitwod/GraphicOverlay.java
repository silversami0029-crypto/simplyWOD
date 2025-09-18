package com.example.fitwod;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GraphicOverlay extends View {
    private final Lock lock = new ReentrantLock();
    private final List<Graphic> graphics = new ArrayList<>();

    public GraphicOverlay(Context context) {
        super(context);
    }

    public void add(Graphic graphic) {
        lock.lock();
        try {
            graphics.add(graphic);
        } finally {
            lock.unlock();
        }
        postInvalidate();
    }

    public void clear() {
        lock.lock();
        try {
            graphics.clear();
        } finally {
            lock.unlock();
        }
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        lock.lock();
        try {
            for (Graphic graphic : graphics) {
                graphic.draw(canvas);
            }
        } finally {
            lock.unlock();
        }
    }

    public static abstract class Graphic {
        protected GraphicOverlay overlay;

        public Graphic(GraphicOverlay overlay) {
            this.overlay = overlay;
        }

        public abstract void draw(Canvas canvas);
    }

    public static class BarcodeGraphic extends Graphic {
        private RectF rect;
        private Paint paint;

        public BarcodeGraphic(GraphicOverlay overlay, Rect boundingBox) {
            super(overlay);
            this.rect = new RectF(boundingBox);
            paint = new Paint();
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4.0f);
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawRect(rect, paint);
        }
    }
}
