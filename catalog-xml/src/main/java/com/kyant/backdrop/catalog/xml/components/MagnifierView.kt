package com.kyant.backdrop.catalog.xml.components

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.kyant.backdrop.xml.LiquidGlassView

class MagnifierView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LiquidGlassView(context, attrs, defStyleAttr) {

    init {
        onDrawBackdrop = { canvas, drawDefault ->
            canvas.save()
            canvas.scale(1.5f, 1.5f, width / 2f, height / 2f)
            drawDefault(canvas)
            canvas.restore()
        }
    }
}
