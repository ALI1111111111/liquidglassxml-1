package com.kyant.backdrop.xml

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.annotation.ColorInt

/**
 * Defines a shadow to be drawn behind the backdrop.
 */
interface Shadow {
    /**
     * Draws the shadow onto the canvas.
     * @param canvas The canvas to draw on.
     * @param path The path defining the shape of the shadow.
     */
    fun draw(canvas: Canvas, path: Path)

    companion object {
        /** A shadow that draws nothing. */
        val None: Shadow = object : Shadow {
            override fun draw(canvas: Canvas, path: Path) {}
        }
    }
}

/**
 * The default implementation of a [Shadow].
 * @property elevation The blur radius of the shadow.
 * @property color The color of the shadow.
 * @property offsetX The horizontal offset of the shadow.
 * @property offsetY The vertical offset of the shadow.
 */
data class DefaultShadow(
    val elevation: Float = 0f,
    @ColorInt val color: Int = 0xFF000000.toInt(),
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
) : Shadow {
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    init {
        if (elevation > 0f) {
            shadowPaint.color = color
            shadowPaint.maskFilter = BlurMaskFilter(elevation, BlurMaskFilter.Blur.NORMAL)
        }
    }

    override fun draw(canvas: Canvas, path: Path) {
        if (elevation > 0f) {
            canvas.save()
            canvas.translate(offsetX, offsetY)
            canvas.drawPath(path, shadowPaint)
            canvas.restore()
        }
    }
}
