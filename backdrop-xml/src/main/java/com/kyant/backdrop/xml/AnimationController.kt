package com.kyant.backdrop.xml

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator

internal class AnimationController(
    private val onUpdate: () -> Unit
) {
    var duration: Long = 0

    fun animate(from: Float, to: Float, onUpdate: (Float) -> Unit) {
        if (duration <= 0) {
            onUpdate(to)
            this.onUpdate()
            return
        }
        ValueAnimator.ofFloat(from, to).apply {
            duration = this@AnimationController.duration
            interpolator = LinearInterpolator()
            addUpdateListener {
                onUpdate(it.animatedValue as Float)
                this@AnimationController.onUpdate()
            }
            start()
        }
    }

    fun animate(from: FloatArray, to: FloatArray, onUpdate: (FloatArray) -> Unit) {
        if (duration <= 0) {
            onUpdate(to)
            this.onUpdate()
            return
        }
        val current = FloatArray(from.size)
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = this@AnimationController.duration
            interpolator = LinearInterpolator()
            addUpdateListener {
                val fraction = it.animatedFraction
                for (i in from.indices) {
                    current[i] = from[i] + (to[i] - from[i]) * fraction
                }
                onUpdate(current)
                this@AnimationController.onUpdate()
            }
            start()
        }
    }
}
