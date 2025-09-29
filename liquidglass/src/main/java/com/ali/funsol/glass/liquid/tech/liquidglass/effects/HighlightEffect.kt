package com.ali.funsol.glass.liquid.tech.liquidglass.effects

import android.graphics.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * An effect that draws a simulated highlight on the bitmap.
 * This is used to approximate the shader-based highlights from the original library.
 */
class HighlightEffect : Effect {

    enum class Style {
        TOUCH, AMBIENT, DIRECTIONAL, NONE
    }

    var style: Style = Style.TOUCH
    var highlightCenter: PointF = PointF(-1f, -1f)
    var highlightRadius: Float = 0f
    var highlightColor: Int = Color.argb(50, 255, 255, 255)
    var highlightAngle: Float = 45f // Angle for DIRECTIONAL style

    override fun apply(bitmap: Bitmap): Bitmap {
        if (style == Style.NONE) return bitmap

        val result = bitmap.copy(bitmap.config, true)
        val canvas = Canvas(result)

        when (style) {
            Style.TOUCH -> drawTouchHighlight(canvas)
            Style.AMBIENT -> drawAmbientHighlight(canvas)
            Style.DIRECTIONAL -> drawDirectionalHighlight(canvas)
            Style.NONE -> { /* Do nothing */ }
        }

        return result
    }

    private fun drawTouchHighlight(canvas: Canvas) {
        if (highlightRadius <= 0 || highlightCenter.x < 0 || highlightCenter.y < 0) return
        val paint = Paint()
        val hotspotColor = Color.argb((Color.alpha(highlightColor) * 1.5f).toInt().coerceAtMost(255), Color.red(highlightColor), Color.green(highlightColor), Color.blue(highlightColor))
        val gradient = RadialGradient(highlightCenter.x, highlightCenter.y, highlightRadius, intArrayOf(hotspotColor, highlightColor, Color.TRANSPARENT), floatArrayOf(0f, 0.3f, 1f), Shader.TileMode.CLAMP)
        paint.shader = gradient
        canvas.drawCircle(highlightCenter.x, highlightCenter.y, highlightRadius, paint)
    }

    private fun drawAmbientHighlight(canvas: Canvas) {
        canvas.drawColor(highlightColor)
    }

    private fun drawDirectionalHighlight(canvas: Canvas) {
        val paint = Paint()
        val angleInRadians = Math.toRadians(highlightAngle.toDouble())
        val startX = 0f
        val startY = 0f
        val endX = (cos(angleInRadians) * canvas.width).toFloat()
        val endY = (sin(angleInRadians) * canvas.height).toFloat()

        val gradient = LinearGradient(startX, startY, endX, endY, highlightColor, Color.TRANSPARENT, Shader.TileMode.CLAMP)
        paint.shader = gradient
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
    }
}
