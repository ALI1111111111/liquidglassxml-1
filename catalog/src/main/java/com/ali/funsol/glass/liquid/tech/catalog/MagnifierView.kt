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

    private val matrix = Matrix()
    private var dragStartX = 0f
    private var dragStartY = 0f

    init {
        onDrawBackdrop = { canvas, backdrop ->
            matrix.setScale(1.5f, 1.5f, width / 2f, height / 2f)
            canvas.drawBitmap(backdrop, matrix, null)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dragStartX = event.rawX - translationX
                dragStartY = event.rawY - translationY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                translationX = event.rawX - dragStartX
                translationY = event.rawY - dragStartY
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
