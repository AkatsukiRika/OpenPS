package com.akatsukirika.openps.utils

import android.view.Choreographer

class FrameRateObserver(private val callback: Callback, private val updateIntervalMillis: Int = 1000) : Choreographer.FrameCallback {
    interface Callback {
        fun onFrameRateChanged(fps: Double)
    }

    private var lastFrameTimeNanos = 0L
    private var frameCount = 0L

    override fun doFrame(frameTimeNanos: Long) {
        if (lastFrameTimeNanos > 0) {
            frameCount++
            val diff = (frameTimeNanos - lastFrameTimeNanos) / (1_000_000.0 * updateIntervalMillis)
            if (diff >= 1.0) {
                val fps = frameCount * (1000.0 / updateIntervalMillis) / diff
                callback.onFrameRateChanged(fps)
                frameCount = 0
                lastFrameTimeNanos = frameTimeNanos
            }
        } else {
            lastFrameTimeNanos = frameTimeNanos
        }
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun startObserve() {
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun endObserve() {
        Choreographer.getInstance().removeFrameCallback(this)
    }
}