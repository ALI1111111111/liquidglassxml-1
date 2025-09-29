package com.ali.funsol.glass.liquid.tech.liquidglass

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import kotlin.math.roundToInt

class LiquidSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : GlassView(context, attrs, defStyle) {

    var value: Float = 0.5f
        set(value) {
            field = value.coerceIn(0f, 1f)
            onValueChangedListener?.onValueChanged(field)
            invalidate()
        }

    var onValueChangedListener: OnValueChangedListener? = null

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(100, 255, 255, 255)
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
    }

    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    private var thumbRadius = 20f
    private var trackY = 0f

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.LiquidSlider)
            value = a.getFloat(R.styleable.LiquidSlider_value, value)
            a.recycle()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        trackY = h / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw track
        canvas.drawLine(thumbRadius, trackY, width - thumbRadius, trackY, trackPaint)

        // Draw thumb
        val thumbX = thumbRadius + (width - 2 * thumbRadius) * value
        canvas.drawCircle(thumbX, trackY, thumbRadius, thumbPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val newValue = (event.x - thumbRadius) / (width - 2 * thumbRadius)
                this.value = newValue
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    interface OnValueChangedListener {
        fun onValueChanged(value: Float)
    }
}
