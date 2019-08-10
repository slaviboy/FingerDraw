package com.slaviboy.fingerdraw;

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
 * FadeOut Class is an abstract class, that is used for the fadeout effect it
 * calculates -delay time and how many step per fadeout method call, should be made.
 */

public abstract class FadeOut {

    public static final int MIN_DURATION = 100;
    public static final int MAX_DURATION = 5000;

    protected boolean tracking;         // if finger is tracked
    protected boolean fading;           // if object is currently fading out
    protected int fadeOutDelay;         // delay time after which the fadeOut() method should be called
    protected int fadeOutStepsPerCall;  // how many fade out steps should be made per one fadeOut() method call
    protected int fadeOutDuration;      // total fade out duration in (ms)
    protected long fadeOutLastTime;     // previously detected system time, from last fadeOut() method call

    /**
     * Call onFadeOut() method if delay time has passed and
     * fadeout steps should be applied.
     *
     * @param currentTime - current system time in (ms)
     */
    public void fadeOut(long currentTime) {
        if (currentTime - fadeOutLastTime >= fadeOutDelay) {
            onFadeOut();
            fadeOutLastTime = currentTime;
        }
    }

    /**
     * Set divisible and use its value to calculate delay time
     * and steps per method call.
     *
     * @param divisible
     */
    protected void setDivisible(int divisible) {

        // get current (duration/100ms) and limit to range [MIN_DURATION,MAX_DURATION]
        double currentDuration;
        if (fadeOutDuration >= MIN_DURATION && fadeOutDuration < MAX_DURATION) {
            currentDuration = (double) fadeOutDuration / MIN_DURATION;
        } else if (fadeOutDuration >= MAX_DURATION) {
            currentDuration = (double) MAX_DURATION / MIN_DURATION;
        } else {
            currentDuration = 1;  // MIN_DURATION/MIN_DURATION
        }

        // get delay and steps per method call
        double delay = currentDuration / divisible;
        if (delay > 1) {
            fadeOutDelay = (int) currentDuration * MIN_DURATION;
            fadeOutStepsPerCall = 1; // 1 step per fadeOut method call
        } else {
            fadeOutDelay = MIN_DURATION;
            fadeOutStepsPerCall = (int) Math.round(((double) divisible / currentDuration));
        }
    }

    protected abstract void onFadeOut();

    public boolean isTracking() {
        return tracking;
    }

    public void setTracking(boolean tracking) {
        this.tracking = tracking;
    }

    public boolean isFading() {
        return fading;
    }

    public void setFading(boolean fading) {
        this.fading = fading;
    }

    public int getFadeOutDuration() {
        return fadeOutDuration;
    }

    public void setFadeOutDuration(int fadeOutDuration) {
        this.fadeOutDuration = fadeOutDuration;
    }
}
