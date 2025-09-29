package com.ali.funsol.glass.liquid.tech.liquidglass

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

class GlassContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    override fun dispatchDraw(canvas: Canvas) {
        val glassViews = mutableListOf<GlassView>()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is GlassView) {
                glassViews.add(child)
            }
        }

        for (i in 0 until glassViews.size) {
            val currentGlass = glassViews[i]
            var previousGlass: GlassView? = null
            if (i > 0) {
                previousGlass = glassViews[i - 1]
            }

            currentGlass.inputBackdrop = previousGlass?.outputBitmap
        }

        super.dispatchDraw(canvas)
    }
}
