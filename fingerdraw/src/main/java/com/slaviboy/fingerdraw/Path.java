package com.slaviboy.fingerdraw;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

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
 * Path Class(Java) that hold values, for current finger states, that includes
 * array with -points the finger has passed trough, -tracking value that
 * shows whether or not the finger is being tracked. If path is -fading out.
 * Current segmental -opacity, and -stroke width, path -color and -type.
 */

public class Path extends FadeOut {

    // path stroke thickness at edges
    public static final int SAME = 0;
    public static final int BIGGER_NEAR_FINGER = 1;
    public static final int SMALLER_NEAR_FINGER = 2;

    private List<PointF> points;      // array with points, through which the finger has passed
    private int opacity;              // current opacity for segmental paths
    private int strokeWidth;          // current stroke width for segmental paths
    private int maxStrokeWidth;       // maximum stroke width
    private int pathType;             // path type that determines id path should be -thick or -thin at the end and the start of the path
    private int color;                // current path color
    private int maxNumSegments;       // maximum number of path segments that will be drawn
    private int lastPointsSize;       // points array size, when fingers is up, used in fade out to detect how many new points are added

    public Path() {

        // default
        this(Color.BLACK, SMALLER_NEAR_FINGER, 4, 70, 200, 25);
    }

    public Path(int color, int pathType, int opacity, int strokeWidth,
                int fadeOutDuration, int maxNumSegments) {

        this.color = color;
        this.pathType = pathType;
        this.opacity = opacity;
        this.strokeWidth = strokeWidth;
        this.maxStrokeWidth = strokeWidth;
        this.fadeOutDuration = fadeOutDuration;
        this.maxNumSegments = maxNumSegments;

        tracking = false;
        points = new ArrayList<>();
        lastPointsSize = 0;

        setDivisible(maxNumSegments);
    }

    /**
     * Add new point to array with points, trough which
     * the finger has passed by, and reset stroke width
     *
     * @param x - coordinate
     * @param y - coordinate
     */
    public void addPoint(float x, float y) {
        points.add(new PointF(x, y));

        // reset to maximum values
        strokeWidth = maxStrokeWidth;
    }

    /**
     * Fade out path, by decreasing the opacity, if opacity bigger than zero
     */
    @Override
    public void onFadeOut() {
        if (!tracking && fading) {

            if (points.size() - lastPointsSize < maxNumSegments) {
                // add the last point again
                for (int i = 0; i < fadeOutStepsPerCall; i++) {
                    points.add(points.get(points.size() - 1));
                }
            } else {
                fading = false;
            }
        }
    }

    /**
     * Draw multiple paths, starting from the current finger position,
     * and with each new path, a previous point is added to the path,
     * that way the transparency is slowly losing its effect since
     * the same path with added new points is redrawn from the
     * head towards the tail of the path.
     * -(head is where the finger is)
     * -(tail are previous points)
     *
     * @param canvas
     * @param paint
     */
    public void draw(android.graphics.Canvas canvas, Paint paint) {

        // at least two point in existence
        if (points.size() < 2 || opacity <= 0) {
            return;
        }

        android.graphics.Path path = new android.graphics.Path();

        // how many point to draw
        int numSegments = Math.min(maxNumSegments, points.size());

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setAlpha(4); // opacity set after color is set, otherwise not working !!!BUG

        // last point
        PointF lastPoint = points.get(points.size() - 1);
        path.moveTo(lastPoint.x, lastPoint.y);

        // loop trough last -numFragments point from the array with points
        for (int i = points.size() - 2; i >= points.size() - numSegments; i--) {

            if (i > points.size() || i < 0) {
                break;
            }

            PointF point = points.get(i);
            path.lineTo(point.x, point.y);

            // set stroke width to decreases or increase depending on -pathType
            if (pathType == SMALLER_NEAR_FINGER) {
                //smaller near the head and increase towards the tail
                strokeWidth = (int) ((points.size() - 1 - i) *
                        ((double) maxStrokeWidth / numSegments));
            } else if (pathType == BIGGER_NEAR_FINGER) {
                // bigger near the head and decreases towards the tail
                strokeWidth = (int) ((numSegments - (points.size() - 1 - i)) *
                        ((double) maxStrokeWidth / numSegments));
            } else {
                // same size towards the tail and head
                strokeWidth = maxStrokeWidth;
            }

            // set real opacity on last finishing draw
            if (i == points.size() - numSegments) {
                paint.setAlpha(opacity);
            }

            paint.setStrokeWidth(strokeWidth);
            canvas.drawPath(path, paint);
        }
    }

    /**
     * Builder class for simple and easy path creation, with setter methods
     * for each custom property. Use it when you want to create paths using
     * mostly default properties and set changes to only some of them.
     */
    public static class Builder {

        private int color;
        private int pathType;
        private int opacity;
        private int strokeWidth;
        private int fadeOutDuration;
        private int maxNumSegments;

        public Builder() {

            // default values
            color = Color.BLACK;
            pathType = SMALLER_NEAR_FINGER;
            opacity = 4;
            strokeWidth = 70;
            fadeOutDuration = 200;
            maxNumSegments = 25;
        }

        public Builder withColor(int color) {
            this.color = color;
            return this;
        }

        public Builder withPathType(int pathType) {
            this.pathType = pathType;
            return this;
        }

        public Builder withOpacity(int opacity) {
            this.opacity = opacity;
            return this;
        }

        public Builder withStrokeWidth(int strokeWidth) {
            this.strokeWidth = strokeWidth;
            return this;
        }

        public Builder withMaxNumSegments(int maxNumSegments) {
            this.maxNumSegments = maxNumSegments;
            return this;
        }

        public Builder withFadeOutDuration(int fadeOutDuration) {
            this.fadeOutDuration = fadeOutDuration;
            return this;
        }

        public Path build() {
            return new Path(color, pathType, opacity, strokeWidth,
                    fadeOutDuration, maxNumSegments);
        }
    }


    public void setPoints(List<PointF> points) {
        this.points = points;
    }

    public List<PointF> getPoints() {
        return points;
    }

    public void setLastPointsSize(int lastPointsSize) {
        this.lastPointsSize = lastPointsSize;
    }

    public int getOpacity() {
        return opacity;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
        this.maxStrokeWidth = strokeWidth;
    }

    public int getPathType() {
        return pathType;
    }

    public void setPathType(int pathType) {
        this.pathType = pathType;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getMaxNumSegments() {
        return maxNumSegments;
    }

    public void setMaxNumSegments(int maxNumSegments) {
        this.maxNumSegments = maxNumSegments;
        setDivisible(maxNumSegments);
    }

    @Override
    public void setFadeOutDuration(int fadeOutDuration) {
        super.setFadeOutDuration(fadeOutDuration);
        setDivisible(maxNumSegments);
    }
}
