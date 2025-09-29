package com.ali.funsol.glass.liquid.tech.catalog

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.dynamicanimation.animation.FloatPropertyCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import kotlin.math.abs

class ControlCenterLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ViewGroup(context, attrs, defStyle) {

    private var progress = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            requestLayout()
        }

    private val progressProperty = object : FloatPropertyCompat<ControlCenterLayout>("progress") {
        override fun getValue(view: ControlCenterLayout): Float = view.progress
        override fun setValue(view: ControlCenterLayout, value: Float) {
            view.progress = value
        }
    }

    private val springAnimation = SpringAnimation(this, progressProperty).apply {
        spring = SpringForce().apply {
            stiffness = SpringForce.STIFFNESS_LOW
            dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
        }
    }

    private var lastY = 0f
    private var velocityTracker: VelocityTracker? = null

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return true // Intercept all touch events
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastY = y
                velocityTracker?.clear()
                velocityTracker = velocityTracker ?: VelocityTracker.obtain()
                velocityTracker?.addMovement(event)
                springAnimation.cancel()
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = y - lastY
                val dragDistance = height.coerceAtLeast(1)
                progress += dy / dragDistance
                lastY = y
                velocityTracker?.addMovement(event)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                velocityTracker?.computeCurrentVelocity(1000)
                val velocityY = velocityTracker?.yVelocity ?: 0f
                velocityTracker?.recycle()
                velocityTracker = null

                val targetProgress = if (abs(velocityY) > 500) {
                    if (velocityY > 0) 1f else 0f
                } else {
                    if (progress > 0.5f) 1f else 0f
                }
                springAnimation.spring.finalPosition = targetProgress
                springAnimation.start()
            }
        }
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        children.forEach { child ->
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount == 0) return

        val parentWidth = width
        val itemSize = (parentWidth / 3).coerceAtMost(200)
        val largeSpacing = itemSize / 2
        val smallSpacing = 16

        // Interpolate spacing based on progress
        val currentSpacing = (smallSpacing + (largeSpacing - smallSpacing) * progress).toInt()

        val row1Y = (height / 2) - itemSize - currentSpacing
        val row2Y = height / 2
        val row3Y = (height / 2) + itemSize + currentSpacing

        // Simple grid layout logic based on progress
        children.forEachIndexed { index, child ->
            val left = when (index % 2) {
                0 -> (parentWidth / 2) - itemSize - (currentSpacing / 2)
                else -> (parentWidth / 2) + (currentSpacing / 2)
            }
            val top = when (index) {
                in 0..1 -> row1Y
                in 2..3 -> row2Y
                else -> row3Y
            }
            child.layout(left, top, left + itemSize, top + itemSize)
        }
    }
}
