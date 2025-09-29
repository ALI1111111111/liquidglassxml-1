package com.ali.funsol.glass.liquid.tech.liquidglass

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

/**
 * An interactive button that extends [GlassView] to provide visual feedback on touch.
 * It animates the brightness, scale, and translation of the glass *layer* to create a fluid,
 * physics-based drag animation that mimics the original library.
 */
class GlassButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : GlassView(context, attrs, defStyle) {

    private val initialBrightness = brightness
    private var dragStartX = 0f
    private var dragStartY = 0f

    // --- Custom FloatProperties for animating our layer properties ---
    private val layerTranslationXProperty = object : FloatPropertyCompat<GlassButton>("layerTranslationX") {
        override fun getValue(view: GlassButton): Float = view.layerTranslationX
        override fun setValue(view: GlassButton, value: Float) {
            view.layerTranslationX = value
            view.invalidate() // A redraw is needed to apply the matrix change
        }
    }

    private val layerTranslationYProperty = object : FloatPropertyCompat<GlassButton>("layerTranslationY") {
        override fun getValue(view: GlassButton): Float = view.layerTranslationY
        override fun setValue(view: GlassButton, value: Float) {
            view.layerTranslationY = value
            view.invalidate()
        }
    }

    private val layerScaleXProperty = object : FloatPropertyCompat<GlassButton>("layerScaleX") {
        override fun getValue(view: GlassButton): Float = view.layerScaleX
        override fun setValue(view: GlassButton, value: Float) {
            view.layerScaleX = value
            view.invalidate()
        }
    }

    private val layerScaleYProperty = object : FloatPropertyCompat<GlassButton>("layerScaleY") {
        override fun getValue(view: GlassButton): Float = view.layerScaleY
        override fun setValue(view: GlassButton, value: Float) {
            view.layerScaleY = value
            view.invalidate()
        }
    }

    // --- Physics-based Animations ---
    private val springForce by lazy {
        SpringForce(0f).apply {
            stiffness = SpringForce.STIFFNESS_MEDIUM
            dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
        }
    }

    private val springX: SpringAnimation by lazy {
        SpringAnimation(this, layerTranslationXProperty).setSpring(springForce)
    }

    private val springY: SpringAnimation by lazy {
        SpringAnimation(this, layerTranslationYProperty).setSpring(springForce)
    }

    private val scaleSpringForce by lazy {
        SpringForce(1f).apply {
            stiffness = SpringForce.STIFFNESS_LOW
            dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        }
    }

    private val springScaleX: SpringAnimation by lazy {
        SpringAnimation(this, layerScaleXProperty).setSpring(scaleSpringForce)
    }

    private val springScaleY: SpringAnimation by lazy {
        SpringAnimation(this, layerScaleYProperty).setSpring(scaleSpringForce)
    }

    init {
        isClickable = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Use relative coordinates for layer translation
        val currentX = event.x
        val currentY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dragStartX = currentX
                dragStartY = currentY
                springX.cancel()
                springY.cancel()
                springScaleX.cancel()
                springScaleY.cancel()

                springScaleX.animateToFinalPosition(1.1f)
                springScaleY.animateToFinalPosition(1.1f)
                animateBrightness(initialBrightness, initialBrightness + 0.2f)
                setTouchHighlight(PointF(event.x, event.y), width.toFloat())
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                layerTranslationX = currentX - dragStartX
                layerTranslationY = currentY - dragStartY
                invalidate() // Manually trigger redraw for translation
                setTouchHighlight(PointF(event.x, event.y), width.toFloat())
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                springX.start()
                springY.start()
                springScaleX.start()
                springScaleY.start()

                animateBrightness(initialBrightness + 0.2f, initialBrightness)
                setTouchHighlight(PointF(-1f, -1f), 0f) // Hide highlight

                if (event.action == MotionEvent.ACTION_UP) {
                    if (Math.abs(layerTranslationX) < 5 && Math.abs(layerTranslationY) < 5) {
                        performClick()
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun animateBrightness(from: Float, to: Float) {
        val animator = ValueAnimator.ofFloat(from, to)
        animator.duration = 200
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            brightness = animation.animatedValue as Float
        }
        animator.start()
    }
}
