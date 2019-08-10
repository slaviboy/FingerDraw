package com.slaviboy.fingerdraw;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

/**
 * Copyright (c) 2019 Stanislav Georgiev. (MIT License)
 * https://github.com/slaviboy
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * - The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * - The Software is provided "as is", without warranty of any kind, express or
 * implied, including but not limited to the warranties of merchantability,
 * fitness for a particular purpose and noninfringement. In no event shall the
 * authors or copyright holders be liable for any claim, damages or other
 * liability, whether in an action of contract, tort or otherwise, arising from,
 * out of or in connection with the Software or the use or other dealings in the
 * Software.
 * <p>
 * <p>
 * Circle Class(Java) that creates a circle object, and is drawn right under
 * current finger position. The class has properties fill and stroke -color,
 * -radius, -center position, -stroke width and -opacity.
 */

public class Circle extends FadeOut {

    private int strokeColor;          // stroke color
    private int fillColor;            // fill color
    private int strokeWidth;          // stroke width
    private int opacity;              // current opacity
    private int radius;               // circle radius
    private float cx;                 // circle center x coordinate
    private float cy;                 // circle center y coordinate
    private int maxOpacity;           // maximum opacity, value used to reset current opacity after fade out

    public Circle() {
        // default
        this(Color.BLACK, Color.WHITE, 1, 25, 0, 200);
    }

    public Circle(int strokeColor, int fillColor, int strokeWidth, int radius,
                  int opacity, int fadeOutDuration) {

        this.strokeColor = strokeColor;
        this.fillColor = fillColor;
        this.strokeWidth = strokeWidth;
        this.radius = radius;
        this.maxOpacity = opacity;
        this.opacity = opacity;
        this.fadeOutDuration = fadeOutDuration;

        setDivisible(maxOpacity);
    }


    /**
     * Change circle -center coordinates and reset opacity to
     * maximum allowed value
     *
     * @param x
     * @param y
     */
    public void move(float x, float y) {
        cx = x;
        cy = y;

        opacity = maxOpacity;
    }

    /**
     * Draw the circle with -radius and center coordinates -cx and -cy,
     * on canvas with paint. Set fill and stroke -color, also -opacity and
     * -stroke width.
     *
     * @param canvas
     * @param paint
     */
    public void draw(android.graphics.Canvas canvas, Paint paint) {

        if (opacity <= 0) {
            return;
        }

        // fill
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fillColor);
        paint.setAlpha(opacity);
        canvas.drawCircle(cx, cy, radius, paint);

        // stroke
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(strokeColor);
        paint.setStrokeWidth(strokeWidth);
        paint.setAlpha(opacity);                   // set -alpha after -color is set, otherwise alpha wont be set !!!BUG
        canvas.drawCircle(cx, cy, radius, paint);
    }

    @Override
    protected void onFadeOut() {

        // decrease opacity when method is called
        if (!tracking && fading) {
            if (opacity - fadeOutStepsPerCall >= 0) {
                opacity -= fadeOutStepsPerCall;
            } else {
                opacity = 0;
                fading = false;
            }
        }
    }

    /**
     * Builder class for simple and easy circle creation, with setter methods
     * for each custom property. Use it when you want to create circles using
     * mostly default properties and set changes to only some of them.
     */
    public static class Builder {

        private int strokeColor;          // stroke color
        private int fillColor;            // fill color
        private int strokeWidth;          // stroke width
        private int opacity;              // current opacity
        private int radius;               // circle radius
        private int fadeOutDuration;      // duration for fadeout effect

        public Builder() {

            // default values
            strokeColor = Color.BLACK;
            fillColor = Color.WHITE;
            strokeWidth = 1;
            opacity = 255;
            radius = 25;
            fadeOutDuration = 200;
        }

        public Builder withStrokeColor(int strokeColor) {
            this.strokeColor = strokeColor;
            return this;
        }

        public Builder withFillColor(int fillColor) {
            this.fillColor = fillColor;
            return this;
        }

        public Builder withStrokeWidth(int strokeWidth) {
            this.strokeWidth = strokeWidth;
            return this;
        }

        public Builder withOpacity(int opacity) {
            this.opacity = opacity;
            return this;
        }

        public Builder withRadius(int radius) {
            this.radius = radius;
            return this;
        }

        public Builder withFadeOutDuration(int fadeOutDuration) {
            this.fadeOutDuration = fadeOutDuration;
            return this;
        }

        public Circle build() {
            return new Circle(strokeColor, fillColor, strokeWidth,
                    radius, opacity, fadeOutDuration);
        }
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public int getOpacity() {
        return opacity;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
        this.maxOpacity = opacity;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public float getCx() {
        return cx;
    }

    public void setCx(float cx) {
        this.cx = cx;
    }

    public float getCy() {
        return cy;
    }

    public void setCy(float cy) {
        this.cy = cy;
    }

    @Override
    public void setFadeOutDuration(int fadeOutDuration) {
        super.setFadeOutDuration(fadeOutDuration);
        setDivisible(maxOpacity);
    }
}
