package com.kyant.backdrop.xml

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Enhanced animation controller with spring physics and custom easing functions.
 */
internal class AnimationController(
    private val onUpdate: () -> Unit
) {
    var duration: Long = 0
    var interpolatorType: InterpolatorType = InterpolatorType.LINEAR
    
    enum class InterpolatorType {
        LINEAR,
        DECELERATE,
        ACCELERATE_DECELERATE,
        OVERSHOOT,
        SPRING,
        EASE_IN_OUT,
        EASE_OUT_BACK
    }
    
    private var activeAnimator: ValueAnimator? = null

    fun animate(from: Float, to: Float, onUpdate: (Float) -> Unit) {
        if (duration <= 0) {
            onUpdate(to)
            this.onUpdate()
            return
        }
        
        activeAnimator?.cancel()
        activeAnimator = ValueAnimator.ofFloat(from, to).apply {
            duration = this@AnimationController.duration
            interpolator = getInterpolator()
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
        activeAnimator?.cancel()
        activeAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = this@AnimationController.duration
            interpolator = getInterpolator()
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
    
    /**
     * Animates with spring physics simulation.
     */
    fun animateSpring(
        from: Float,
        to: Float,
        stiffness: Float = 300f,
        damping: Float = 0.5f,
        onUpdate: (Float) -> Unit
    ) {
        if (duration <= 0) {
            onUpdate(to)
            this.onUpdate()
            return
        }
        
        activeAnimator?.cancel()
        activeAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = this@AnimationController.duration
            interpolator = SpringInterpolator(stiffness, damping)
            addUpdateListener {
                val fraction = it.animatedValue as Float
                val value = from + (to - from) * fraction
                onUpdate(value)
                this@AnimationController.onUpdate()
            }
            start()
        }
    }
    
    fun cancel() {
        activeAnimator?.cancel()
        activeAnimator = null
    }
    
    private fun getInterpolator(): TimeInterpolator {
        return when (interpolatorType) {
            InterpolatorType.LINEAR -> LinearInterpolator()
            InterpolatorType.DECELERATE -> DecelerateInterpolator()
            InterpolatorType.ACCELERATE_DECELERATE -> AccelerateDecelerateInterpolator()
            InterpolatorType.OVERSHOOT -> OvershootInterpolator(1.5f)
            InterpolatorType.SPRING -> SpringInterpolator()
            InterpolatorType.EASE_IN_OUT -> EaseInOutInterpolator()
            InterpolatorType.EASE_OUT_BACK -> EaseOutBackInterpolator()
        }
    }
    
    /**
     * Spring physics interpolator.
     */
    private class SpringInterpolator(
        private val stiffness: Float = 300f,
        private val damping: Float = 0.5f
    ) : TimeInterpolator {
        override fun getInterpolation(input: Float): Float {
            val omega = sqrt(stiffness)
            val zeta = damping
            
            return if (zeta < 1f) {
                // Underdamped spring
                val omegaD = omega * sqrt(1 - zeta * zeta)
                val exp = Math.exp((-zeta * omega * input).toDouble()).toFloat()
                1f - exp * (sin((omegaD * input).toDouble()).toFloat() / omegaD * omega + input * zeta * omega)
            } else {
                // Critically damped or overdamped
                val exp = Math.exp((-omega * input).toDouble()).toFloat()
                1f - exp * (1f + omega * input)
            }
        }
    }
    
    /**
     * Ease in-out cubic interpolator.
     */
    private class EaseInOutInterpolator : TimeInterpolator {
        override fun getInterpolation(input: Float): Float {
            return if (input < 0.5f) {
                4f * input * input * input
            } else {
                1f - (-2f * input + 2f).pow(3) / 2f
            }
        }
    }
    
    /**
     * Ease out back interpolator (slight overshoot).
     */
    private class EaseOutBackInterpolator : TimeInterpolator {
        private val c1 = 1.70158f
        private val c3 = c1 + 1f
        
        override fun getInterpolation(input: Float): Float {
            return 1f + c3 * (input - 1f).pow(3) + c1 * (input - 1f).pow(2)
        }
    }
}

