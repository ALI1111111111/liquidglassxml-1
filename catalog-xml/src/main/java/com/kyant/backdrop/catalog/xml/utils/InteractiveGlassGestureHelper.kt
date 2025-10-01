package com.kyant.backdrop.catalog.xml.utils

import android.animation.ValueAnimator
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.view.GestureDetectorCompat
import kotlin.math.atan2
import kotlin.math.tanh

/**
 * Helper class for handling interactive glass effect gestures on views.
 * Provides spring-like animations and momentum-based interactions.
 */
class InteractiveGlassGestureHelper(
    private val view: View,
    private val config: Config = Config()
) {

    data class Config(
        val enableScale: Boolean = true,
        val enableTranslation: Boolean = true,
        val maxScale: Float = 0.1f,
        val maxOffset: Float = 100f,
        val springStiffness: Float = 300f,
        val springDamping: Float = 0.5f,
        val animationDuration: Long = 200L
    )

    private var pressStartX = 0f
    private var pressStartY = 0f
    private var currentOffsetX = 0f
    private var currentOffsetY = 0f
    
    private val scaleAnimator = ValueAnimator().apply {
        duration = config.animationDuration
        interpolator = AccelerateDecelerateInterpolator()
    }
    
    private val offsetXAnimator = ValueAnimator().apply {
        duration = config.animationDuration
        interpolator = OvershootInterpolator()
    }
    
    private val offsetYAnimator = ValueAnimator().apply {
        duration = config.animationDuration
        interpolator = OvershootInterpolator()
    }

    /**
     * Callback invoked when the animation state changes.
     * Parameters: (progress: Float, offsetX: Float, offsetY: Float, scaleX: Float, scaleY: Float)
     */
    var onAnimationUpdate: ((Float, Float, Float, Float, Float) -> Unit)? = null

    private val gestureDetector = GestureDetectorCompat(view.context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            pressStartX = e.x
            pressStartY = e.y
            currentOffsetX = 0f
            currentOffsetY = 0f
            animatePress(true)
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (config.enableTranslation && e1 != null) {
                currentOffsetX = e2.x - pressStartX
                currentOffsetY = e2.y - pressStartY
                updateTransform()
            }
            return true
        }
    })

    fun onTouchEvent(event: MotionEvent): Boolean {
        val handled = gestureDetector.onTouchEvent(event)
        
        when (event.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                animatePress(false)
            }
        }
        
        return handled
    }

    private fun animatePress(pressed: Boolean) {
        val targetProgress = if (pressed) 1f else 0f
        val targetScale = 1f + (if (pressed) config.maxScale else 0f)
        
        scaleAnimator.cancel()
        scaleAnimator.setFloatValues(view.scaleX, targetScale)
        scaleAnimator.addUpdateListener { animation ->
            val scale = animation.animatedValue as Float
            if (config.enableScale) {
                view.scaleX = scale
                view.scaleY = scale
            }
            
            val progress = if (pressed) {
                (scale - 1f) / config.maxScale
            } else {
                1f - ((scale - 1f) / config.maxScale).coerceAtLeast(0f)
            }
            
            updateTransform(progress)
        }
        scaleAnimator.start()

        if (!pressed && config.enableTranslation) {
            // Animate back to center
            offsetXAnimator.cancel()
            offsetYAnimator.cancel()
            
            offsetXAnimator.setFloatValues(currentOffsetX, 0f)
            offsetXAnimator.addUpdateListener {
                currentOffsetX = it.animatedValue as Float
                updateTransform()
            }
            offsetXAnimator.start()
            
            offsetYAnimator.setFloatValues(currentOffsetY, 0f)
            offsetYAnimator.addUpdateListener {
                currentOffsetY = it.animatedValue as Float
                updateTransform()
            }
            offsetYAnimator.start()
        }
    }

    private fun updateTransform(progress: Float = scaleAnimator.animatedFraction) {
        val width = view.width.toFloat()
        val height = view.height.toFloat()
        
        if (width == 0f || height == 0f) return

        // Apply tanh function for smooth bounded translation
        val initialDerivative = 0.05f
        val translationX = config.maxOffset * tanh(initialDerivative * currentOffsetX / config.maxOffset)
        val translationY = config.maxOffset * tanh(initialDerivative * currentOffsetY / config.maxOffset)

        // Calculate directional scaling based on drag direction
        val offsetAngle = atan2(currentOffsetY, currentOffsetX)
        val maxDragScale = 0.1f
        val scaleX = 1f + maxDragScale * kotlin.math.abs(kotlin.math.cos(offsetAngle) * currentOffsetX / width.coerceAtLeast(1f)) *
                (width / height).coerceAtMost(1f)
        val scaleY = 1f + maxDragScale * kotlin.math.abs(kotlin.math.sin(offsetAngle) * currentOffsetY / height.coerceAtLeast(1f)) *
                (height / width).coerceAtMost(1f)

        onAnimationUpdate?.invoke(progress, translationX, translationY, scaleX, scaleY)
    }

    fun cleanup() {
        scaleAnimator.cancel()
        offsetXAnimator.cancel()
        offsetYAnimator.cancel()
    }
}
