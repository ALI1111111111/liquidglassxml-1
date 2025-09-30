package com.kyant.backdrop.xml

import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.annotation.ColorInt

/**
 * Defines an inner shadow to be drawn inside the backdrop.
 */
interface InnerShadow {
    /**
     * Draws the inner shadow onto the canvas.
     * @param canvas The canvas to draw on.
     * @param path The path defining the shape of the inner shadow.
     * @param width The width of the view.
     * @param height The height of the view.
     */
    fun draw(canvas: Canvas, path: Path, width: Float, height: Float)

    companion object {
        /** An inner shadow that draws nothing. */
        val None: InnerShadow = object : InnerShadow {
            override fun draw(canvas: Canvas, path: Path, width: Float, height: Float) {}
        }
    }
}

/**
 * The default implementation of an [InnerShadow].
 * @property elevation The blur radius of the inner shadow.
 * @property color The color of the inner shadow.
 * @property offsetX The horizontal offset of the inner shadow.
 * @property offsetY The vertical offset of the inner shadow.
 */
data class DefaultInnerShadow(
    val elevation: Float = 0f,
    @ColorInt val color: Int = 0xFF000000.toInt(),
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
) : InnerShadow {
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    init {
        if (elevation > 0f) {
            shadowPaint.color = color
            shadowPaint.maskFilter = BlurMaskFilter(elevation, BlurMaskFilter.Blur.NORMAL)
        }
    }

    override fun draw(canvas: Canvas, path: Path, width: Float, height: Float) {
        if (elevation > 0f) {
            canvas.save()
            // Clip to the original shape
            canvas.clipPath(path)

            // Create a larger path for the shadow
            val shadowPath = Path(path)
            val largerRect = Path()
            largerRect.addRect(
                -elevation,
                -elevation,
                width + elevation,
                height + elevation,
                Path.Direction.CW
            )
            shadowPath.op(largerRect, Path.Op.DIFFERENCE)

            canvas.translate(offsetX, offsetY)
            canvas.drawPath(shadowPath, shadowPaint)
            canvas.restore()
        }
    }
}
