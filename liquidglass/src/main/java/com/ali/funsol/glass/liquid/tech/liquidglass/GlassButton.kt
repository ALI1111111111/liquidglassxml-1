package com.ali.funsol.glass.liquid.tech.liquidglass

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

/**
 * An interactive button that extends [FrameLayout] to provide visual feedback on touch.
 * It contains a [GlassView] and a [TextView], and animates the glass layer to create a fluid,
 * physics-based drag animation that mimics the original library.
 */
class GlassButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val glassView: GlassView
    private val textView: TextView
    private val initialBrightness: Float
    private var dragStartX = 0f
    private var dragStartY = 0f
    private val touchSlop: Int

    // --- Custom FloatProperties for animating our layer properties ---
    private val layerTranslationXProperty = object : FloatPropertyCompat<GlassView>("layerTranslationX") {
        override fun getValue(view: GlassView): Float = view.layerTranslationX
        override fun setValue(view: GlassView, value: Float) {
            view.layerTranslationX = value
        }
    }

    private val layerTranslationYProperty = object : FloatPropertyCompat<GlassView>("layerTranslationY") {
        override fun getValue(view: GlassView): Float = view.layerTranslationY
        override fun setValue(view: GlassView, value: Float) {
            view.layerTranslationY = value
        }
    }

    private val layerScaleXProperty = object : FloatPropertyCompat<GlassView>("layerScaleX") {
        override fun getValue(view: GlassView): Float = view.layerScaleX
        override fun setValue(view: GlassView, value: Float) {
            view.layerScaleX = value
        }
    }

    private val layerScaleYProperty = object : FloatPropertyCompat<GlassView>("layerScaleY") {
        override fun getValue(view: GlassView): Float = view.layerScaleY
        override fun setValue(view: GlassView, value: Float) {
            view.layerScaleY = value
        }
    }

    private val brightnessProperty = object : FloatPropertyCompat<GlassView>("brightness") {
        override fun getValue(view: GlassView): Float = view.brightness
        override fun setValue(view: GlassView, value: Float) {
            view.brightness = value
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
        SpringAnimation(glassView, layerTranslationXProperty).setSpring(springForce)
    }

    private val springY: SpringAnimation by lazy {
        SpringAnimation(glassView, layerTranslationYProperty).setSpring(springForce)
    }

    private val scaleSpringForce by lazy {
        SpringForce(1f).apply {
            stiffness = SpringForce.STIFFNESS_LOW
            dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
        }
    }

    private val springScaleX: SpringAnimation by lazy {
        SpringAnimation(glassView, layerScaleXProperty).setSpring(scaleSpringForce)
    }

    private val springScaleY: SpringAnimation by lazy {
        SpringAnimation(glassView, layerScaleYProperty).setSpring(scaleSpringForce)
    }

    private val springBrightness: SpringAnimation by lazy {
        SpringAnimation(glassView, brightnessProperty).setSpring(
            SpringForce(initialBrightness).apply {
                stiffness = SpringForce.STIFFNESS_LOW
                dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            }
        )
    }

    // --- Public Properties for Delegation ---
    var cornerRadius: Float
        get() = glassView.cornerRadius
        set(value) {
            glassView.cornerRadius = value
        }

    var blurRadius: Float
        get() = glassView.blurRadius
        set(value) {
            glassView.blurRadius = value
        }

    var isTiltEnabled: Boolean
        get() = glassView.isTiltEnabled
        set(value) {
            glassView.isTiltEnabled = value
        }

    var strokeWidth: Float = 0f
        set(value) {
            field = value
            // In a real scenario, you'd have a stroke effect to apply this to.
            // For now, this is a placeholder.
            invalidate()
        }

    var strokeColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            // Placeholder for stroke effect
            invalidate()
        }


    init {
        isClickable = true
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop

        // Create the GlassView and pass the attributes down to it.
        // Its own init block will parse the GlassView-specific attributes.
        glassView = GlassView(context, attrs, defStyle)
        addView(glassView)

        textView = TextView(context).apply {
            gravity = Gravity.CENTER
        }
        addView(textView)

        // Now, just parse the GlassButton-specific attributes.
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.GlassButton)
            textView.text = a.getString(R.styleable.GlassButton_android_text)
            textView.setTextColor(a.getColor(R.styleable.GlassButton_android_textColor, Color.WHITE))
            a.recycle()
        }

        initialBrightness = glassView.brightness
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
                springBrightness.cancel()

                springScaleX.animateToFinalPosition(1.1f)
                springScaleY.animateToFinalPosition(1.1f)
                springBrightness.animateToFinalPosition(initialBrightness + 0.2f)
                glassView.setTouchHighlight(PointF(event.x, event.y), width.toFloat())
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                glassView.layerTranslationX = currentX - dragStartX
                glassView.layerTranslationY = currentY - dragStartY
                glassView.invalidate() // Manually trigger redraw for translation
                glassView.setTouchHighlight(PointF(event.x, event.y), width.toFloat())
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                springX.start()
                springY.start()
                springScaleX.start()
                springScaleY.start()
                springBrightness.start()

                animateBrightness(initialBrightness + 0.2f, initialBrightness)
                glassView.setTouchHighlight(PointF(-1f, -1f), 0f) // Hide highlight

                if (event.action == MotionEvent.ACTION_UP) {
                    val dx = currentX - dragStartX
                    val dy = currentY - dragStartY
                    if (dx * dx + dy * dy < touchSlop * touchSlop) {
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
            glassView.brightness = animation.animatedValue as Float
        }
        animator.start()
    }
}
