package com.ali.funsol.glass.liquid.tech.catalog

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import com.ali.funsol.glass.liquid.tech.liquidglass.GlassView

class MagnifierView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : GlassView(context, attrs, defStyle) {

    private val magnificationMatrix = Matrix()
    private var magnificationScale = 1.5f

    init {
        isClickable = true // To receive touch events
    }

    override fun onDrawBackdrop(canvas: Canvas, backdrop: android.graphics.Bitmap) {
        magnificationMatrix.setScale(magnificationScale, magnificationScale, width / 2f, height / 2f)
        canvas.matrix = magnificationMatrix
        super.onDrawBackdrop(canvas, backdrop)
    }

    private var dX = 0f
    private var dY = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dX = x - event.rawX
                dY = y - event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                animate()
                    .x(event.rawX + dX)
                    .y(event.rawY + dY)
                    .setDuration(0)
                    .start()
            }
        }
        return super.onTouchEvent(event)
    }
}
