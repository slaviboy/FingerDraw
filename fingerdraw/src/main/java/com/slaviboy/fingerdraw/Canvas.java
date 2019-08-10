package com.slaviboy.fingerdraw;

import android.content.Context;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

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
 * Canvas Class(Java) is a class that extends surfaceView and is used
 * to draw current finger paths and circles. Class uses thread as looper to
 * redraw the canvas scene and uses onTouch event to get finger coordinates.
 */

public class Canvas extends SurfaceView implements SurfaceHolder.Callback, Runnable,
        View.OnTouchListener {

    public Canvas(Context context) {
        super(context);
        init(context);
    }

    public Canvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Canvas(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public Canvas(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private Path[] paths;                 // array with finger data for each finger
    private Circle[] circles;             // circles that is draw at current finger position
    private SurfaceHolder surfaceHolder;  // holder for the SurfaceView
    private Thread thread;                // thread object for the surfaceView
    private boolean isRunning;            // if thread is running
    private boolean consumeTouchEvents;   // whether or not to consume the touch event after handling
    private Timer timer;                  // timer with 100ms delay, to call fadeout methods

    private void init(Context context) {

        // on top of other surfaceViews
        setZOrderOnTop(true);
        setOnTouchListener(this);

        // translucent background
        if (surfaceHolder == null) {
            surfaceHolder = this.getHolder();
            surfaceHolder.addCallback(this);
            surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        }

        // default
        consumeTouchEvents = true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopThread();
    }


    @Override
    public void run() {

        android.graphics.Canvas canvas;

        // init paint
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setPathEffect(new CornerPathEffect(23));


        // init shapes
        if (paths == null) {
            int numFingers = 2; // default allowed number of fingers is two

            paths = new Path[numFingers];
            circles = new Circle[numFingers];
            for (int i = 0; i < paths.length; i++) {
                paths[i] = new Path();
                circles[i] = new Circle();
            }
        }

        // set timer, that calls the fadeout effect, with period of (100ms)
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();

                // fadeout shapes
                for (int i = 0; i < paths.length; i++) {
                    paths[i].fadeOut(currentTime);
                    circles[i].fadeOut(currentTime);
                }
            }
        }, 0, FadeOut.MIN_DURATION);

        // looper thread using while loop
        while (isRunning) {

            canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();

                synchronized (surfaceHolder) {
                    if (canvas != null) {

                        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);

                        // draw shapes
                        for (int i = 0; i < paths.length; i++) {
                            paths[i].draw(canvas, paint);
                            circles[i].draw(canvas, paint);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }

        if (timer != null) {
            timer.cancel();
        }
    }


    /**
     * Update path and circle for given finger, using arrayIndex, set
     * -tracking and -fading properties, and set new position coordinates -x and -y
     *
     * @param arrayIndex - current array index corresponding to the consecutive finger on screen
     * @param x          - current finger coordinates
     * @param y          - current finger coordinates
     * @param isTracking - if finger will be tracked
     * @param isFading   - if shapes will fade out
     */
    private void update(int arrayIndex, float x, float y,
                        boolean isTracking, boolean isFading) {
        Path p = paths[arrayIndex];
        p.setFading(isFading);
        p.setTracking(isTracking);
        p.addPoint(x, y); //!

        Circle c = circles[arrayIndex];
        c.setFading(isFading);
        c.setTracking(isTracking);
        c.move(x, y);
    }

    /**
     * Called on ACTION_UP || ACTION_POINTER_UP events, to set that the
     * finger is no longer being tracked and fadeout should start
     *
     * @param event        motion event from the onTouch event
     * @param arrayIndex   current array index corresponding to the consecutive finger on screen
     * @param pointerIndex current pointer index from the event
     */
    private void up(MotionEvent event, int arrayIndex, int pointerIndex) {
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        update(arrayIndex, x, y, false, true);
        paths[arrayIndex].setLastPointsSize(
                paths[arrayIndex].getPoints().size()); // array size at start of the fadeout effect
    }

    /**
     * Called on ACTION_DOWN || ACTION_POINTER_DOWN events, to set that the
     * finger is tracked, reset the array with points and stop the fadeout effect
     *
     * @param event        motion event from the onTouch event
     * @param arrayIndex   current array index corresponding to the consecutive finger on screen
     * @param pointerIndex current pointer index from the event
     */
    private void down(MotionEvent event, int arrayIndex, int pointerIndex) {
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        paths[arrayIndex].setPoints(new ArrayList<PointF>()); // reset list array
        update(arrayIndex, x, y, true, false);
    }

    /**
     * Called on ACTION_MOVE event, to add point with current finger position
     * for corresponding -path and set new coordinates for -circle
     *
     * @param event
     */
    private void move(MotionEvent event) {

        int num = event.getPointerCount();
        for (int pointerIndex = 0; pointerIndex < num; pointerIndex++) {
            int arrayIndex = event.getPointerId(pointerIndex); // id corresponding to array index

            // if it is being tracked
            if (arrayIndex < paths.length && paths[arrayIndex].isTracking()) {

                float x = event.getX(pointerIndex);
                float y = event.getY(pointerIndex);

                paths[arrayIndex].addPoint(x, y); // add new point to path
                circles[arrayIndex].move(x, y);   // move circle position
            }
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (thread == null) {
            return consumeTouchEvents;
        }

        int pointerIndex = event.getActionIndex();
        int arrayIndex = event.getPointerId(pointerIndex); // corresponds to array index, since it is const.

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                if (arrayIndex < paths.length) {
                    down(event, arrayIndex, pointerIndex);
                }
                return consumeTouchEvents;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP: {
                if (arrayIndex < paths.length) {
                    up(event, arrayIndex, pointerIndex);
                }
                return consumeTouchEvents;
            }
            case MotionEvent.ACTION_MOVE: {

                // call move state for all allowed fingers, determine if finger is moved
                move(event);
                return consumeTouchEvents;
            }
        }
        return consumeTouchEvents;
    }

    /**
     * Set array with -paths, if previous and current array sizes does not
     * match than, transfer objects from array with -circles, to new array,
     * and if new array has bigger size then add default values.
     *
     * @param paths
     */
    public void setPaths(Path[] paths) {

        // if number of path object is changed
        if (this.paths == null || this.paths.length != paths.length) {

            Circle[] newCircle = new Circle[paths.length];
            for (int i = 0; i < newCircle.length; i++) {
                if (circles != null && i < circles.length) {
                    newCircle[i] = circles[i]; // add from previous paths array
                } else {
                    newCircle[i] = new Circle(); // if new array is bigger add default paths
                }
            }
            circles = newCircle;
        }

        this.paths = paths;
    }

    /**
     * Set array with -circles, if previous and current array sizes does not
     * match than, transfer objects from array with -paths, to new array, and
     * if new array has bigger size then add default values.
     *
     * @param circles
     */
    public void setCircles(Circle[] circles) {

        // transfer circles to new array to match path size
        if (this.circles == null || this.circles.length != circles.length) {

            Path[] newPaths = new Path[circles.length];
            for (int i = 0; i < newPaths.length; i++) {
                if (paths != null && i < paths.length) {
                    newPaths[i] = paths[i]; // add from previous paths array
                } else {
                    newPaths[i] = new Path(); // if new array is bigger add default paths
                }
            }
            paths = newPaths;
        }

        this.circles = circles;
    }

    public void setConsumeTouchEvents(boolean consumeTouchEvents) {
        this.consumeTouchEvents = consumeTouchEvents;
    }

    public boolean getConsumeTouchEvents() {
        return consumeTouchEvents;
    }

    public Path[] getPaths() {
        return paths;
    }

    public Circle[] getCircles() {
        return circles;
    }

    private void startThread() {
        if (thread == null) {
            isRunning = true;
            thread = new Thread(this); // set runnable, to new thread
            thread.start();
        }
    }

    private void stopThread() {
        if (thread != null) {
            timer.cancel();
            timer = null;
            isRunning = false;
            thread.interrupt();
            thread = null;
        }
    }

    public void restart() {
        stopThread();
        startThread();
    }

    public void start() {
        startThread();
    }

    public void stop() {
        stopThread();
    }
}

